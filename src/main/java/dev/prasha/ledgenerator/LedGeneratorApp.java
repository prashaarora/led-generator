package dev.prasha.ledgenerator;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import javax.imageio.ImageIO;

public final class LedGeneratorApp {
    private LedGeneratorApp() {
    }

    public static void main(String[] args) throws Exception {
        Config config = Config.from(args);
        LedFrameGenerator generator = new LedFrameGenerator();
        List<BufferedImage> frames = generator.createFrames(config.message(), config.frameCount());

        Files.createDirectories(config.outputDir());
        Path pngPath = config.outputDir().resolve("preview.png");
        Path gifPath = config.outputDir().resolve("preview.gif");

        ImageIO.write(frames.getFirst(), "png", pngPath.toFile());
        writeGif(frames, gifPath, config.delayMillis());

        System.out.println("Generated PNG: " + pngPath.toAbsolutePath());
        System.out.println("Generated GIF: " + gifPath.toAbsolutePath());

        String host = config.host();
        if (host != null && !host.isBlank()) {
            new PixooPublisher().publishImage(host, pngPath);
            System.out.println("Sent preview to Pixoo host: " + host);
        } else {
            System.out.println("PIXOO_HOST not set, skipped device upload.");
        }
    }

    private static void writeGif(List<BufferedImage> frames, Path gifPath, int delayMillis) throws IOException {
        try (GifSequenceWriter writer = new GifSequenceWriter(gifPath, BufferedImage.TYPE_INT_RGB, delayMillis, true)) {
            for (BufferedImage frame : frames) {
                writer.writeFrame(frame);
            }
        }
    }

    record Config(String message, Path outputDir, int frameCount, int delayMillis, String host) {
        static Config from(String[] args) {
            String message = args.length > 0 && !args[0].isBlank() ? args[0] : "DEVOXX LED";
            Path outputDir = args.length > 1 && !args[1].isBlank() ? Path.of(args[1]) : Path.of("output");
            int frameCount = args.length > 2 ? Integer.parseInt(args[2]) : 24;
            int delayMillis = args.length > 3 ? Integer.parseInt(args[3]) : 90;
            String host = System.getenv("PIXOO_HOST");
            return new Config(message, outputDir, frameCount, delayMillis, host);
        }
    }
}