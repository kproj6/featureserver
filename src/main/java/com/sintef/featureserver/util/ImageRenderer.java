package com.sintef.featureserver.util;

import com.sintef.featureserver.netcdf.Feature;

import java.awt.*;
import java.awt.image.BufferedImage;

public class ImageRenderer {

    public ImageRenderer(){} // Should not be instantiated.

    //The height or width of the resulting image (whichever dimension is the smallest) will be this big
    final static int goalSize = 256;

    public static BufferedImage render(final double[][] rawData, Feature feature, boolean forceSquare) {

        final int dataWidth = rawData[0].length;
        final int dataHeight = rawData.length;

        double minValue = 32767;
        double maxValue = -32768;
        Color minColor = new Color(0xffffff);
        Color maxColor = new Color(0x000000);

        switch (feature){
            case SALINITY:
                minValue = 26;
                maxValue = 36;
                minColor = new Color(0xffffff);
                maxColor = new Color(0x0048ff);
                break;
            case TEMPERATURE:
                minValue = 0;
                maxValue = 20;
                minColor = new Color(0x0048ff);
                maxColor = new Color(0xff2321);
                break;
            case CURRENT_MAGNITUDE:
                minValue = 0;
                maxValue = 1;
                minColor = new Color(0x0048ff);
                maxColor = new Color(0xff2321);
                break;
            case CURRENT_DIRECTION:
                break;
            case DEPTH:
                minValue = 0;
                maxValue = 1000;
                minColor = new Color(0xffffff);
                maxColor = new Color(0x0048ff);
                break;
        }

        final double aspectRatio = (double)dataWidth/dataHeight;
        final int imageWidth;
        final int imageHeight;
        if (aspectRatio == 1.0 || forceSquare) {
            imageWidth = goalSize;
            imageHeight = goalSize;
        } else if (aspectRatio < 1.0) {
            imageWidth = goalSize;
            imageHeight = (int)(goalSize * aspectRatio);
        } else {
            imageWidth = (int)(goalSize * aspectRatio);
            imageHeight = goalSize;
        }

        final double xScale = (double)imageWidth / dataWidth;
        final double yScale = (double)imageHeight / dataHeight;

        final BufferedImage image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < imageHeight; y++) {
            for (int x = 0; x < imageWidth; x++) {
                final int dataX = (int)((imageHeight-1 - y)/yScale);
                final int dataY = (int)(x/xScale);
                final double value = rawData[dataX][dataY];

                if (Double.isNaN(value)) {
                    image.setRGB(x, y, 0x00000000);         // coloring all landspots to transparent
                } else {

                    final double fraction = normalize(minValue, maxValue, value);
                    final int r = lerp(minColor.getRed(),   maxColor.getRed(),   fraction);
                    final int g = lerp(minColor.getGreen(), maxColor.getGreen(), fraction);
                    final int b = lerp(minColor.getBlue(),  maxColor.getBlue(),  fraction);
                    final int a = lerp(minColor.getAlpha(), maxColor.getAlpha(), fraction);

                    final Color color = new Color(r,g,b,a);
                    image.setRGB(x, y, color.getRGB());
                }
            }
        }
        return image;
    }

    // returns 0.0 if value is min, 1.0 if value is max.
    // interpolates and extrapolates for other values
    private static double normalize(final double min, final double max, final double value) {
        final double divisor = max - min;
        if (divisor <= 0.0) {
            System.out.println("invalid min and max!");
            return 0.0f;
        }
        return (value - min) / divisor;
    }

    // fraction gets clamped to [0, 1]
    private static int lerp(final int i1, final int i2, double fraction){
        if (fraction > 1.0) {
            fraction = 1.0;
        } else if (fraction < 0.0) {
            fraction = 0.0;
        }

        return (int)((1.0 - fraction)*i1 + fraction*i2 + 0.5);
    }
}



