package com.sintef.featureserver.util;

import java.awt.image.BufferedImage;

public class ImageRenderer {

    public ImageRenderer(){} // Should not be instantiated.

    //The height or width of the resulting image (whichever dimension is the smallest) will be this big
    final static int goalSize = 256;

    public static BufferedImage render(final double[][] rawData, boolean forceSquare) {

        final int dataWidth = rawData[0].length;
        final int dataHeight = rawData.length;

        double minValue = 32767;
        double maxValue = -32768;


        for (int y = 0; y < dataHeight; y++) {
            for (int x = 0; x < dataWidth; x++) {
                double value = rawData[y][x];

                if (value != 0) {
                    if (value > maxValue)
                        maxValue = value;

                    if (value < minValue)
                        minValue = value;
                }
            }
        }


        double aspectRatio = (double)dataWidth/dataHeight;
        int imageWidth, imageHeight;
        if(aspectRatio == 1.0 || forceSquare){
            imageWidth = goalSize;
            imageHeight = goalSize;
        }else if (aspectRatio < 1.0){
            imageWidth = goalSize;
            imageHeight = (int)(goalSize * aspectRatio);
        }else{
            imageWidth = (int)(goalSize * aspectRatio);
            imageHeight = goalSize;
        }

        double xScale = (double)imageWidth/dataWidth;
        double yScale = (double)imageHeight/dataHeight;

        final BufferedImage image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < imageHeight; y++) {
            for (int x = 0; x < imageWidth; x++) {
                int dataX = (int)((imageHeight-1 - y)/yScale);
                int dataY = (int)(x/xScale);
                double value = rawData[dataX][dataY];

                if (Double.isNaN(value)) {
                    //image.setRGB(x, y, 0xff00ff00);       // coloring all landspots to green
                    image.setRGB(x, y, 0x00000000);         // coloring all landspots to transparent
                } else {
                    short a = 255;
                    short r = (short) (255.0 * normalize(minValue, maxValue, value));
                    short g = (short) (255.0 * normalize(minValue, maxValue, value));
                    short b = (short) (255.0 * normalize(minValue, maxValue, value));

                    int argb = (int) ((a << 24) | (r << 16) | (g << 8) | (b));
                    image.setRGB(x, y, argb);
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
