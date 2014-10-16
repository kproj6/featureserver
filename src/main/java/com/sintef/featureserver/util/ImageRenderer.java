package com.sintef.featureserver.util;

import java.awt.image.BufferedImage;

public class ImageRenderer {

    public ImageRenderer(){} // Should not be instantiated.

    //private static final short minValue = -32768;
    //private static final short maxValue = 32767;

    //the recommended min and max values suggested by Finn Olav for salinity
    //private static final short minValue = (short)((26 - 50) / 0.0015259255f);
    //private static final short maxValue = (short)((30 - 50) / 0.0015259255f);


    public static BufferedImage render(final double[][] rawData) {


        final int width = rawData[0].length;
        final int height = rawData.length;
        final BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        double minValue = 32767;
        double maxValue = -32768;


        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                double value = rawData[y][x];

                if(value != 0) {
                    if (value > maxValue)
                        maxValue = value;

                    if(value < minValue)
                        minValue = value;
                }
            }
        }

        System.out.println(minValue);
        System.out.println(maxValue);


        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                double value = rawData[y][x];

                if (value == 0) {
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
    private static double normalize(final double min, final double max, final double value) {
        final double divisor = max - min;
        if (divisor <= 0.0) {
            System.out.println("invalid min and max!");
            return 0.0f;
        }
        return (value - min) / divisor;
    }
}
