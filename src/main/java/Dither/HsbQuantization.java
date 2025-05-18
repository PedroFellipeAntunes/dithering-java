package Dither;

import Dither.Util.DataConverter;
import Dither.Util.LuminanceRangeCalculator;

import java.awt.Color;
import java.awt.image.BufferedImage;

public class HsbQuantization {
    private final DataConverter dc = new DataConverter();
    
    private final int minLevels = 2;
    private final int maxLevels = 256;

    /**
     * Applies brightness quantization in HSB space, choosing between simple
     * uniform quantization and dynamic‐range quantization based on rangeQ.
     *
     * @param image  the BufferedImage to process
     * @param levels the number of discrete brightness levels (2–256)
     * @param rangeQ true for dynamic‐range quantization, false for uniform
     * @throws IllegalArgumentException if levels is outside [2,256]
     */
    public void applyQuantization(BufferedImage image, int levels, boolean rangeQ) {
        if (levels < minLevels || levels > maxLevels) {
            throw new IllegalArgumentException(
                "Brightness levels must be between " + minLevels + " and " + maxLevels
            );
        }

        if (rangeQ) {
            brightnessQuantizationWithRange(image, levels);
        } else {
            brightnessQuantization(image, levels);
        }
    }

    private void brightnessQuantization(BufferedImage image, int levels) {
        int w = image.getWidth();
        int h = image.getHeight();

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                Color orig = new Color(image.getRGB(x, y), true);
                
                float[] hsb = Color.RGBtoHSB(orig.getRed(), orig.getGreen(), orig.getBlue(), null);

                hsb[2] = quantizeFloatChannel(hsb[2], levels);

                image.setRGB(x, y, dc.convertFromHsbToRgb(hsb, orig.getAlpha()));
            }
        }
    }

    private void brightnessQuantizationWithRange(BufferedImage image, int levels) {
        int w = image.getWidth();
        int h = image.getHeight();

        double[] range = LuminanceRangeCalculator.compute(image, true);
        double minB = range[0], maxB = range[1];

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                Color orig = new Color(image.getRGB(x, y), true);
                
                float[] hsb = Color.RGBtoHSB(orig.getRed(), orig.getGreen(), orig.getBlue(), null);

                hsb[2] = quantizeFloatWithRange(hsb[2], levels, minB, maxB);

                image.setRGB(x, y, dc.convertFromHsbToRgb(hsb, orig.getAlpha()));
            }
        }
    }
    
    /**
     * Maps a brightness value (0–1) to the nearest of the given number of
     * uniform steps, rounding to the nearest level center.
     *
     * @param value the original brightness (0–1)
     * @param levels the number of quantization levels (must be ≥2)
     * @return the quantized brightness (0–1)
     */
    public float quantizeFloatChannel(float value, int levels) {
        float step = levels - 1;
        float q = (float)(Math.floor(value * step + 0.5) / step);
        
        return clamp01(q);
    }
    
    /**
     * Quantizes a brightness value within the specified [min,max] interval into
     * the given number of levels, then remaps it back to [0–1].
     *
     * @param value the original brightness (0–1)
     * @param levels the number of quantization levels
     * @param min the lower bound of the brightness interval
     * @param max the upper bound of the brightness interval
     * @return the quantized and remapped brightness (0–1)
     */
    public float quantizeFloatWithRange(float value, int levels, double min, double max) {
        double v = Math.max(min, Math.min(max, value));
        
        double norm = (v - min) / (max - min);
        double qNorm = Math.floor(norm * (levels - 1) + 0.5) / (levels - 1);
        
        double quantized = qNorm * (max - min) + min;
        
        return clamp01((float)quantized);
    }
    
    private float clamp01(float v) {
        return v < 0f ? 0f : (v > 1f ? 1f : v);
    }
}