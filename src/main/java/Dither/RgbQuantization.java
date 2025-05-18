package Dither;

import Dither.Util.DataConverter;
import Dither.Util.LuminanceRangeCalculator;

import java.awt.image.BufferedImage;

public class RgbQuantization {
    private final DataConverter dc = new DataConverter();
    private final int min = 2, max = 256;
    
    /**
     * Applies color quantization to the given image, choosing between simple
     * quantization and dynamic‐range quantization based on the rangeQ flag.
     *
     * @param image the BufferedImage to process
     * @param levels the number of discrete color levels (must be between 2 and
     * 256)
     * @param rangeQ true to use dynamic range quantization, false for uniform
     * color quantization
     * @throws IllegalArgumentException if levels is outside the valid range
     */
    public void applyQuantization(BufferedImage image, int levels, boolean rangeQ) {
        if (levels < min || levels > max) {
            throw new IllegalArgumentException(
                    "Brightness levels must be between " + min + " and " + max
            );
        }
        
        if (rangeQ) {
            dynamicRangeQuantization(image, levels);
        } else {
            colorQuantization(image, levels);
        }
    }
    
    private void colorQuantization(BufferedImage image, int levels) {
        int width = image.getWidth();
        int height = image.getHeight();
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int[] rgba = dc.convertFromIntegerToArray(image.getRGB(x, y));

                rgba[1] = quantizeChannel(rgba[1], levels); // R
                rgba[2] = quantizeChannel(rgba[2], levels); // G
                rgba[3] = quantizeChannel(rgba[3], levels); // B
//                rgba[0] = quantizeChannel(rgba[0], levels); // A

                int newRGBA = dc.convertFromArrayToInteger(rgba);
                image.setRGB(x, y, newRGBA);
            }
        }
    }
    
    /**
     * Maps a single 0–255 channel value to the nearest of the given number of
     * levels, rounding to the nearest level center.
     *
     * @param value the original channel intensity (0–255)
     * @param levels the number of quantization levels (must be ≥2)
     * @return the quantized channel value in the range 0–255
     */
    public int quantizeChannel(int value, int levels) {
        double color = value / 255.0;
        color = (Math.floor(color * (levels - 1) + 0.5)) / (levels - 1);
        
        return (int) (color * 255);
    }
    
    private void dynamicRangeQuantization(BufferedImage image, int levels) {
        int width = image.getWidth();
        int height = image.getHeight();
        
        double[] range = LuminanceRangeCalculator.compute(image, false);
        double newMin = range[0];
        double newMax = range[1];
        
        // Quantize
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int[] rgba = dc.convertFromIntegerToArray(image.getRGB(x, y));
                
                for (int c = 1; c <= 3; c++) {
                    rgba[c] = quantizeWithRange(rgba[c], levels, newMin, newMax);
                }
                
                image.setRGB(x, y, dc.convertFromArrayToInteger(rgba));
            }
        }
    }
    
    /**
     * Quantizes a channel value within the specified [min, max] interval into
     * the given number of levels, then remaps it back to the 0–255 range.
     *
     * @param value the original channel intensity
     * @param levels the number of quantization levels
     * @param min the lower bound of the luminance interval
     * @param max the upper bound of the luminance interval
     * @return the quantized and remapped channel intensity (0–255)
     */
    public int quantizeWithRange(int value, int levels, double min, double max) {
        // Clamp
        double v = Math.max(min, Math.min(max, value));
        
        // Normalize
        double norm = (v - min) / (max - min);
        
        // Quantize
        double qNorm = Math.floor(norm * (levels - 1) + 0.5) / (levels - 1);
        double quantized = qNorm * (max - min) + min;
        
        // Back to 0-255
        return (int) Math.round((quantized - min) / (max - min) * 255);
    }
}