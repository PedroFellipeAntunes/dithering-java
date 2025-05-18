package Dither.Util;

import FileManager.Grayscale;
import java.awt.image.BufferedImage;

public class LuminanceRangeCalculator {
    /**
     * Computes a symmetric luminance range [min, max] around the average
     * luminance so that min and max are equidistant from the mean.
     *
     * @param image the BufferedImage from which luminance is measured
     * @return a double array of size 2: [min, max]
     */
    public static double[] compute(BufferedImage image) {
        int width       = image.getWidth();
        int height      = image.getHeight();
        int totalPixels = width * height;

        double minLum = Double.MAX_VALUE;
        double maxLum = -Double.MAX_VALUE;
        double sumLum = 0.0;

        DataConverter dc = new DataConverter();
        Grayscale gs = new Grayscale();
        
        // Find the min/avg/max of image
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int[] rgba = dc.convertFromIntegerToArray(image.getRGB(x, y));
                double lum = gs.bt709(rgba)[1];

                minLum = Math.min(minLum, lum);
                maxLum = Math.max(maxLum, lum);
                sumLum += lum;
            }
        }

        // Build symmetric range around mean
        double avgLum = sumLum / totalPixels;
        double d1     = avgLum - minLum;
        double d2     = maxLum - avgLum;
        double d      = Math.min(d1, d2);

        double newMin = avgLum - d;
        double newMax = avgLum + d;

        return new double[]{ newMin, newMax };
    }
}