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
        List<BufferedImage> frames = new ArrayList<>(frameCount);
        for (int index = 0; index < frameCount; index++) {
            frames.add(createFrame(message, index, frameCount));
        }
        return frames;
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