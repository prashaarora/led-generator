package dev.prasha.ledgenerator;

import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class LedFrameGeneratorTest {
    @Test
    void createsExpectedFrameCountAndSize() {
        LedFrameGenerator generator = new LedFrameGenerator();

        List<BufferedImage> frames = generator.createFrames("HELLO", 12);

        assertEquals(12, frames.size());
        assertEquals(64, frames.getFirst().getWidth());
        assertEquals(64, frames.getFirst().getHeight());
    }

    @Test
    void createsAnimatedFramesThatDiffer() {
        LedFrameGenerator generator = new LedFrameGenerator();

        BufferedImage first = generator.createFrame("LED", 0, 24);
        BufferedImage second = generator.createFrame("LED", 12, 24);

        assertNotEquals(first.getRGB(10, 10), second.getRGB(10, 10));
    }
}