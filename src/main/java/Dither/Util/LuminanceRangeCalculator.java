package Dither.Util;

import FileManager.Grayscale;
import java.awt.Color;
import java.awt.image.BufferedImage;

public class LuminanceRangeCalculator {
    /**
     * Computes a symmetric luminance range [min, max] around the average
     * luminance so that min and max are equidistant from the mean.
     *
     * @param image the BufferedImage from which luminance is measured
     * @param useHsb whether to use brightness instead of luminance
     * @return a double array of size 2: [min, max]
     */
    public static double[] compute(BufferedImage image, boolean useHsb) {
        int width = image.getWidth();
        int height = image.getHeight();
        int totalPixels = width * height;

        double minValue = Double.MAX_VALUE;
        double maxValue = Double.MIN_VALUE;
        double sumValue = 0.0;

        DataConverter dc = new DataConverter();
        Grayscale gs = new Grayscale();
        
        // Find the min/avg/max of image
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                double value;
                
                if (useHsb) {
                    Color c = new Color(image.getRGB(x, y));
                    float[] hsb = Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null);
                    value = hsb[2];
                } else {
                    int[] rgba = dc.convertFromIntegerToArray(image.getRGB(x, y));
                    value = gs.bt709(rgba)[1];
                }

                minValue = Math.min(minValue, value);
                maxValue = Math.max(maxValue, value);
                sumValue += value;
            }
        }

        // Build symmetric range around mean
        double avgValue = sumValue / totalPixels;
        double d1 = avgValue - minValue;
        double d2 = maxValue - avgValue;
        double d = Math.min(d1, d2);

        double newMin = avgValue - d;
        double newMax = avgValue + d;

        return new double[]{ newMin, newMax };
    }
}