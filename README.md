# led-generator

Small Java 21 + Maven project that generates 64x64 LED-style visuals for a Pixoo 64 and optionally sends a preview image to the device using the official [Jixoo](https://github.com/glaforge/jixoo) release binary.

## What it does

- Generates 64x64 pixel-art animations: a ringed planet, synthwave sunset, or Belgian raffle with a waving flag and waffle.
- Keeps the original scrolling banner as an optional scene.
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
5. Scene: `raffle` (default), `planet`, `synthwave`, or `banner`

## Pixel-art scenes

These are procedural interpretations of the reference images, not exact copies. No image assets or extra dependencies are needed.

```bash
java -jar target/led-generator-1.0.0-all.jar "DEVOXX BELGIUM" output/raffle 48 80 raffle
java -jar target/led-generator-1.0.0-all.jar "" output/planet 48 80 planet
java -jar target/led-generator-1.0.0-all.jar "" output/synthwave 48 80 synthwave
```

Each run writes both `preview.png` and a looping `preview.gif` to its output directory. Running without arguments generates the raffle scene. Choose `banner` to use the original scrolling text.

## Send to your Pixoo 64

```bash
export PIXOO_HOST="192.168.1.100"
java -jar target/led-generator-1.0.0-all.jar "DEVOXX" output 28 80
```

If `PIXOO_HOST` is not set, the project still works locally and only generates PNG/GIF output.

When `PIXOO_HOST` is set, the app auto-downloads the matching Jixoo binary into `.tools/jixoo/` on first run and uses it for upload.

## Why Jixoo

This project integrates with the official Jixoo project from Guillaume Laforge for Pixoo 64 device communication. The upload step uses Jixoo's upstream `pixoo-cli` release artifact because the `jixoo64` Maven artifact is not published on Maven Central.