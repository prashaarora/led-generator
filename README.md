# led-generator

Small Java 21 + Maven project that generates 64x64 LED-style visuals for a Pixoo 64 and optionally sends a preview image to the device using the official [Jixoo](https://github.com/glaforge/jixoo) release binary.

## What it does

- Generates a 64x64 animated LED-style banner locally.
- Exports a static preview as `output/preview.png`.
- Exports an animated preview as `output/preview.gif`.
- Uses the official `pixoo-cli` release from Jixoo to send the generated PNG to a Pixoo 64 when `PIXOO_HOST` is set.

## Requirements

- Java 21
- Maven 3.8+

## Run locally

```bash
mvn test
mvn package
java -jar target/led-generator-1.0.0-all.jar "GOOGLE RAFFLE" output 28 80
```

Arguments:

1. Message text
2. Output directory
3. Frame count
4. Frame delay in milliseconds

## Send to your Pixoo 64

```bash
export PIXOO_HOST="192.168.1.100"
java -jar target/led-generator-1.0.0-all.jar "DEVOXX" output 28 80
```

If `PIXOO_HOST` is not set, the project still works locally and only generates PNG/GIF output.

When `PIXOO_HOST` is set, the app auto-downloads the matching Jixoo binary into `.tools/jixoo/` on first run and uses it for upload.

## Why Jixoo

This project integrates with the official Jixoo project from Guillaume Laforge for Pixoo 64 device communication. The upload step uses Jixoo's upstream `pixoo-cli` release artifact because the `jixoo64` Maven artifact is not published on Maven Central.