package dev.prasha.ledgenerator;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

final class LedFrameGenerator {
    static final int SIZE = 64;

    List<BufferedImage> createFrames(String message, int frameCount) {
        return createFrames(message, frameCount, "banner");
    }

    List<BufferedImage> createFrames(String message, int frameCount, String scene) {
        if (frameCount < 1) {
            throw new IllegalArgumentException("Frame count must be positive");
        }
        if (!List.of("banner", "planet", "synthwave", "raffle").contains(scene)) {
            throw new IllegalArgumentException("Scene must be banner, planet, synthwave, or raffle");
        }
        List<BufferedImage> frames = new ArrayList<>(frameCount);
        for (int index = 0; index < frameCount; index++) {
            frames.add(scene.equals("banner") ? createFrame(message, index, frameCount)
                    : createScene(message, index, frameCount, scene));
        }
        return frames;
    }

    // ponytail: procedural Java2D pixel art; use sprite assets only for exact reference replicas.
    private BufferedImage createScene(String message, int index, int count, String scene) {
        BufferedImage image = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        double phase = 2 * Math.PI * index / count;
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
            g.setColor(new Color(5, 5, 15));
            g.fillRect(0, 0, SIZE, SIZE);
            if (scene.equals("synthwave")) {
                paintSynthwave(g, phase);
            }
            paintStars(g, phase);
            switch (scene) {
                case "planet" -> paintPlanet(image, g, phase);
                case "synthwave" -> { }
                case "raffle" -> paintRaffle(g, message, phase);
                default -> throw new IllegalArgumentException("Unknown scene: " + scene);
            }
        } finally {
            g.dispose();
        }
        return image;
    }

    private void paintStars(Graphics2D g, double phase) {
        for (int i = 0; i < 35; i++) {
            int brightness = (int) (150 + 100 * Math.sin(phase + i));
            g.setColor(new Color(brightness, brightness, Math.min(255, brightness + 35)));
            g.fillRect((i * 29 + 3) % SIZE, (i * 17 + 1) % SIZE, 1, 1);
        }
    }

    private void paintPlanet(BufferedImage image, Graphics2D g, double phase) {
        paintRings(g, phase, false);
        for (int y = 14; y < 49; y++) {
            for (int x = 15; x < 50; x++) {
                double dx = (x - 32) / 17.0, dy = (y - 31) / 17.0;
                double radius = dx * dx + dy * dy;
                if (radius <= 1) {
                    double z = Math.sqrt(1 - radius);
                    boolean land = Math.sin(dx * 6 + phase) + Math.cos(dy * 8 + dx * 3) > 0.8;
                    Color base = land ? new Color(40, 175, 88) : new Color(28, 105, 240);
                    float light = (float) Math.max(0.15, Math.min(1, 0.35 + 0.65 * z - 0.3 * dx - 0.2 * dy));
                    image.setRGB(x, y, mix(Color.BLACK, base, light).getRGB());
                }
            }
        }
        paintRings(g, phase, true);
    }

    private void paintRings(Graphics2D g, double phase, boolean front) {
        int[] colors = {0x4285f4, 0xea4335, 0xfbbc04, 0x34a853};
        for (int ring = 0; ring < colors.length; ring++) {
            double radius = 23 + ring * 2;
            for (int step = 0; step < 180; step++) {
                double angle = step * Math.PI / 90;
                if ((Math.sin(angle) >= 0) != front) continue;
                int x = (int) Math.round(32 + radius * Math.cos(angle));
                int y = (int) Math.round(32 + radius * 0.36 * Math.sin(angle) - radius * 0.4 * Math.cos(angle));
                g.setColor(new Color(colors[ring]));
                g.fillRect(x, y, 2, 1);
            }
            double angle = phase + ring * Math.PI / 2;
            if ((Math.sin(angle) >= 0) == front) {
                int x = (int) Math.round(32 + radius * Math.cos(angle));
                int y = (int) Math.round(32 + radius * 0.36 * Math.sin(angle) - radius * 0.4 * Math.cos(angle));
                g.setColor(new Color(colors[ring]));
                g.fillOval(x - 2, y - 2, 5, 5);
                g.setColor(new Color(0xffffff));
                g.fillRect(x - 1, y - 1, 1, 1);
            }
        }
    }

    private void paintSynthwave(Graphics2D g, double phase) {
        for (int y = 0; y < 41; y++) {
            g.setColor(mix(new Color(9, 4, 26), new Color(76, 10, 85), y / 40f));
            g.drawLine(0, y, 63, y);
        }
        for (int y = 11; y < 39; y++) {
            double dy = (y - 25) / 15.0;
            int half = (int) (15 * Math.sqrt(Math.max(0, 1 - dy * dy)));
            if (y > 23 && y % 6 < 2) continue;
            g.setColor(mix(new Color(255, 242, 75), new Color(255, 55, 109), (y - 11) / 28f));
            g.drawLine(32 - half, y, 32 + half, y);
        }
        for (int layer = 0; layer < 2; layer++) {
            g.setColor(new Color(layer == 0 ? 0xff32c7 : 0x4de4ff));
            for (int x = 0; x < SIZE; x++) {
                int y = (int) (34 + layer * 4 + 3 * Math.sin(x * 0.3 + phase + layer));
                g.drawLine(x, y, x, 40);
            }
        }
        g.setColor(new Color(8, 2, 20));
        g.fillRect(0, 41, 64, 23);
        g.setColor(new Color(0x00c8ed));
        for (int x = -96; x <= 160; x += 32) g.drawLine(32, 40, x, 63);
        g.setColor(new Color(0xf325ca));
        g.drawLine(0, 40, 63, 40);
        for (int row = 0; row < 8; row++) {
            double distance = (row + phase / (2 * Math.PI)) / 8;
            int y = 41 + (int) (23 * distance * distance);
            g.drawLine(0, y, 63, y);
        }
    }

    private void paintRaffle(Graphics2D g, String message, double phase) {
        g.setColor(new Color(0x9292a2));
        g.fillRect(11, 2, 2, 21);
        int[] flag = {0x242428, 0xffdf27, 0xd92c38};
        for (int x = 13; x < 52; x++) {
            int wave = (int) Math.round(2 * Math.sin(x * 0.22 - phase));
            g.setColor(new Color(flag[(x - 13) / 13]));
            g.fillRect(x, 3 + wave, 1, 17);
        }
        int[] colors = {0x4285f4, 0xea4335, 0xfbbc04, 0x34a853};
        for (int x = 0; x < 64; x += 3) {
            g.setColor(new Color(colors[(x / 3) % 4]));
            g.fillRect(x, 25, 3, 1);
            g.fillRect(x, 35, 3, 1);
        }
        g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 9));
        int width = g.getFontMetrics().stringWidth(message);
        int textX = width <= 64 ? (64 - width) / 2 : -(int) Math.round((width - 60) * (1 - Math.cos(phase)) / 2);
        g.setColor(new Color(0xffedb2));
        g.drawString(message, textX, 33);
        for (int i = 0; i < 4; i++) {
            int y = 40 + (int) Math.round(Math.sin(phase + i));
            g.setColor(new Color(colors[i]));
            g.fillOval(18 + i * 8, y, 5, 6);
            g.setColor(new Color(0xffedb2));
            g.fillRect(19 + i * 8, y + 1, 1, 1);
        }
        g.setColor(new Color(0xbc8032));
        g.fillRoundRect(15, 46, 34, 17, 3, 3);
        g.setColor(new Color(0xf1b85d));
        for (int x = 17; x < 48; x += 6) g.fillRect(x, 47, 1, 15);
        for (int y = 47; y < 63; y += 5) g.fillRect(16, y, 32, 1);
        g.setColor(new Color(0x90521c));
        for (int x = 18; x < 47; x += 6) {
            for (int y = 48; y < 62; y += 5) g.fillRect(x, y, 4, 3);
        }
    }

    BufferedImage createFrame(String message, int frameIndex, int frameCount) {
        BufferedImage image = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        try {
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            paintBackground(graphics, frameIndex, frameCount);
            paintGrid(glowOverlay(image));
            paintText(graphics, message, frameIndex, frameCount);
            paintAccent(graphics, frameIndex, frameCount);
        } finally {
            graphics.dispose();
        }
        return image;
    }

    private void paintBackground(Graphics2D graphics, int frameIndex, int frameCount) {
        float progress = (float) frameIndex / Math.max(1, frameCount - 1);
        for (int y = 0; y < SIZE; y++) {
            float blend = (float) y / (SIZE - 1);
            Color color = mix(new Color(10, 12, 28), mix(new Color(8, 187, 255), new Color(255, 106, 0), progress), blend);
            graphics.setColor(color);
            graphics.drawLine(0, y, SIZE, y);
        }
    }

    private void paintText(Graphics2D graphics, String message, int frameIndex, int frameCount) {
        graphics.setFont(new Font(Font.MONOSPACED, Font.BOLD, 13));
        FontMetrics metrics = graphics.getFontMetrics();
        int totalWidth = metrics.stringWidth(message);
        int travel = SIZE + totalWidth + 12;
        double cycle = (double) frameIndex / Math.max(1, frameCount);
        int x = SIZE - (int) Math.round(cycle * travel);
        int y = 34;

        graphics.setColor(new Color(255, 245, 180));
        graphics.drawString(message, x, y);
        graphics.setColor(new Color(255, 255, 255, 90));
        graphics.drawString(message, x + 1, y + 1);
    }

    private void paintAccent(Graphics2D graphics, int frameIndex, int frameCount) {
        double progress = (double) frameIndex / Math.max(1, frameCount);
        int radius = 8 + (int) Math.round(6 * Math.sin(progress * Math.PI * 2));
        int centerX = 12 + (int) Math.round(40 * progress);
        int centerY = 16 + (int) Math.round(10 * Math.sin(progress * Math.PI * 4));
        graphics.setColor(new Color(255, 255, 255, 120));
        graphics.fillOval(centerX - radius / 2, centerY - radius / 2, radius, radius);
        graphics.setColor(new Color(255, 60, 120, 190));
        graphics.fillOval(centerX - radius / 3, centerY - radius / 3, Math.max(4, radius / 2), Math.max(4, radius / 2));
    }

    private void paintGrid(BufferedImage image) {
        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                Color source = new Color(image.getRGB(x, y));
                int red = clamp((int) Math.round(source.getRed() * 0.92));
                int green = clamp((int) Math.round(source.getGreen() * 0.92));
                int blue = clamp((int) Math.round(source.getBlue() * 0.92));
                if ((x + y) % 2 == 0) {
                    red = clamp(red + 12);
                    green = clamp(green + 12);
                    blue = clamp(blue + 12);
                }
                image.setRGB(x, y, new Color(red, green, blue).getRGB());
            }
        }
    }

    private BufferedImage glowOverlay(BufferedImage image) {
        return image;
    }

    private Color mix(Color left, Color right, float ratio) {
        float clamped = Math.max(0, Math.min(1, ratio));
        int red = clamp(Math.round(left.getRed() * (1 - clamped) + right.getRed() * clamped));
        int green = clamp(Math.round(left.getGreen() * (1 - clamped) + right.getGreen() * clamped));
        int blue = clamp(Math.round(left.getBlue() * (1 - clamped) + right.getBlue() * clamped));
        return new Color(red, green, blue);
    }

    private int clamp(int value) {
        return Math.max(0, Math.min(255, value));
    }
}