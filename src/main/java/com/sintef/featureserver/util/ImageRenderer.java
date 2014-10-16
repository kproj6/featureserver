package com.sintef.featureserver.util;

import java.awt.image.BufferedImage;

public class ImageRenderer {

    public ImageRenderer(){} // Should not be instantiated.

    private static final short minValue = -32768;
    private static final short maxValue = 32767;


    public static BufferedImage render(final short[][] rawData) {

        final int width = rawData[0].length;
        final int height = rawData.length;
        final BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                short value = rawData[y][x];

                if (value == -32768) {
                    image.setRGB(x, y, 0x00ff00);        // coloring all landspots green
                } else {
                    short r = (short) (256.0 * normalize(minValue, maxValue, value));
                    short g = (short) (256.0 * normalize(minValue, maxValue, value));
                    short b = (short) (256.0 * normalize(minValue, maxValue, value));

                    int rgb = (int) (r << 16 | g << 8 | b);
                    image.setRGB(x, y, rgb);
                }
            }
        }
        return image;

    }

    // returns 0 if value is min, 1 if value is max
    private static float normalize(final int min, final int max, final int value) {
        final float divisor = max - min;
        if (divisor <= 0.0) {
            System.out.println("invalid min and max!");
            return 0.0f;
        }
        return (value - min) / divisor;
    }
}
