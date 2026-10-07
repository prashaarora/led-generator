package dev.prasha.ledgenerator;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFilePermission;
import java.time.Duration;
import java.util.EnumSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

final class PixooPublisher {
    private static final String VERSION = "v0.3.0";
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .connectTimeout(Duration.ofSeconds(20))
            .build();

    void publishImage(String host, Path imagePath) throws IOException, InterruptedException {
        Path cliPath = ensureCliInstalled();
        ProcessBuilder processBuilder = new ProcessBuilder(
                cliPath.toString(),
                "-H",
                host,
                "image",
                imagePath.toAbsolutePath().toString()
        );
        processBuilder.inheritIO();

        Process process = processBuilder.start();
        int exitCode;
        try {
            exitCode = process.waitFor();
        } catch (InterruptedException exception) {
            process.destroyForcibly();
            throw exception;
        }
        if (exitCode != 0) {
            throw new IOException("pixoo-cli exited with code " + exitCode);
        }
    }

    private Path ensureCliInstalled() throws IOException, InterruptedException {
        Path toolsDir = Path.of(".tools", "jixoo");
        Files.createDirectories(toolsDir);
        String binaryName = binaryName();
        Path binaryPath = toolsDir.resolve(binaryName);
        if (Files.isRegularFile(binaryPath)) {
            return binaryPath;
        }

        Path archivePath = toolsDir.resolve(assetName());
        downloadAsset(archivePath);
        unzipBinary(archivePath, binaryName, toolsDir);
        setExecutable(binaryPath);
        return binaryPath;
    }

    private void downloadAsset(Path archivePath) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(assetUri())
                .timeout(Duration.ofMinutes(2))
                .GET()
                .build();
        HttpResponse<Path> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofFile(archivePath));
        if (response.statusCode() >= 400) {
            throw new IOException("Failed to download Jixoo release asset: HTTP " + response.statusCode());
        }
    }

    private void unzipBinary(Path archivePath, String binaryName, Path targetDir) throws IOException {
        try (InputStream inputStream = Files.newInputStream(archivePath);
             ZipInputStream zipInputStream = new ZipInputStream(inputStream)) {
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    continue;
                }
                String entryName = Path.of(entry.getName()).getFileName().toString();
                if (!entryName.equals(binaryName)) {
                    continue;
                }
                Files.copy(zipInputStream, targetDir.resolve(binaryName), StandardCopyOption.REPLACE_EXISTING);
                return;
            }
        }

        throw new IOException("Could not find " + binaryName + " inside Jixoo archive");
    }

    private void setExecutable(Path binaryPath) throws IOException {
        try {
            Set<PosixFilePermission> permissions = EnumSet.of(
                    PosixFilePermission.OWNER_READ,
                    PosixFilePermission.OWNER_WRITE,
                    PosixFilePermission.OWNER_EXECUTE,
                    PosixFilePermission.GROUP_READ,
                    PosixFilePermission.GROUP_EXECUTE,
                    PosixFilePermission.OTHERS_READ,
                    PosixFilePermission.OTHERS_EXECUTE
            );
            Files.setPosixFilePermissions(binaryPath, permissions);
        } catch (UnsupportedOperationException ignored) {
        }
    }

    private URI assetUri() {
        return URI.create("https://github.com/glaforge/jixoo/releases/download/" + VERSION + "/" + assetName());
    }

    private String assetName() {
        String osName = System.getProperty("os.name").toLowerCase();
        String architecture = System.getProperty("os.arch").toLowerCase();

        if (osName.contains("mac") && (architecture.contains("aarch64") || architecture.contains("arm64"))) {
            return "pixoo-cli-macos-aarch64.zip";
        }
        if (osName.contains("linux") && architecture.contains("64")) {
            return "pixoo-cli-linux-amd64.zip";
        }
        if (osName.contains("win") && architecture.contains("64")) {
            return "pixoo-cli-windows-amd64.zip";
        }

        throw new IllegalStateException("Unsupported OS/architecture for automatic Jixoo download: " + osName + " / " + architecture);
    }

    private String binaryName() {
        return System.getProperty("os.name").toLowerCase().contains("win") ? "pixoo-cli.exe" : "pixoo-cli";
    }
}