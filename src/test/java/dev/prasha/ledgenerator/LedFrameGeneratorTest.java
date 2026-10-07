package dev.prasha.ledgenerator;

import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

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

        assertFalse(Arrays.equals(pixels(first), pixels(second)));
    }

    @Test
    void createsDistinctAnimatedPixelScenes() {
        LedFrameGenerator generator = new LedFrameGenerator();
        for (String scene : List.of("planet", "synthwave", "raffle", "banner")) {
            List<BufferedImage> frames = generator.createFrames("DEVOXX BELGIUM", 24, scene);
            assertEquals(24, frames.size());
            assertEquals(64, frames.getFirst().getWidth());
            assertEquals(64, frames.getFirst().getHeight());
            assertFalse(Arrays.equals(pixels(frames.getFirst()), pixels(frames.get(6))), scene);
        }
        assertNotEquals(generator.createFrames("LED", 1, "planet").getFirst().getRGB(32, 20),
                generator.createFrames("LED", 1, "synthwave").getFirst().getRGB(32, 20));
        assertThrows(IllegalArgumentException.class, () -> generator.createFrames("LED", 0, "raffle"));
        assertThrows(IllegalArgumentException.class, () -> generator.createFrames("LED", 1, "unknown"));
    }

    private int[] pixels(BufferedImage image) {
        return image.getRGB(0, 0, 64, 64, null, 0, 64);
    }
}