package Dither;

import Dither.Util.DataConverter;
import Dither.Util.BayerCalculator;

import Dither.Interface.ColorQuantizer;

import java.awt.image.BufferedImage;

public class OrderedDithering {
    private final int[][] bayerMatrix;
    private final double[][] normalizedBayer;
    private final ColorQuantizer quantizer;
    private final DataConverter cd = new DataConverter();
    
    private final int bitValue;
    private final boolean rangeQ;
    private final double spread;

    /**
     * Constructs an OrderedDithering instance with a custom Bayer matrix size.
     *
     * @param n the dimension (n × n) of the Bayer threshold matrix to generate
     * @param bitValue the number of discrete color levels for quantization
     * (e.g., 2, 4, 8…)
     * @param rangeQ true to apply dynamic‐range quantization, false for uniform
     * quantization
     * @param spread the strength of the ordered‐dither effect to add per pixel
     */
    public OrderedDithering(ColorQuantizer quantizer, int n, int bitValue, boolean rangeQ, double spread) {
        BayerCalculator bc = new BayerCalculator();
        this.bayerMatrix = bc.computeBayerMatrix(n);
        this.normalizedBayer = normalizeBayer(this.bayerMatrix);
        
        this.quantizer = quantizer;
        this.bitValue = bitValue;
        this.rangeQ = rangeQ;
        this.spread = spread;
    }

    /**
     * Constructs an OrderedDithering instance using a default 2×2 Bayer matrix.
     *
     * @param bitValue the number of discrete color levels for quantization
     * (e.g., 2, 4, 8…)
     * @param rangeQ true to apply dynamic‐range quantization, false for uniform
     * quantization
     * @param spread the strength of the ordered‐dither effect to add per pixel
     */
    public OrderedDithering(ColorQuantizer quantizer, int bitValue, boolean rangeQ, double spread) {
        this(quantizer, 2, bitValue, rangeQ, spread);
    }
    
    private double[][] normalizeBayer(int[][] mat) {
        int n = mat.length;
        double n2 = n * n;
        double[][] norm = new double[n][n];
        
        for (int y = 0; y < n; y++) {
            for (int x = 0; x < n; x++) {
                norm[y][x] = mat[y][x] / n2;
            }
        }
        
        double max = Double.NEGATIVE_INFINITY;
        
        for (int y = 0; y < n; y++) {
            for (int x = 0; x < n; x++) {
                if (norm[y][x] > max) {
                    max = norm[y][x];
                }
            }
        }
        
        double shift = 0.5 * max;
        
        for (int y = 0; y < n; y++) {
            for (int x = 0; x < n; x++) {
                norm[y][x] -= shift;
            }
        }
        
        return norm;
    }
    
    /**
     * Applies ordered dithering and quantization to the provided image in
     * place.
     *
     * @param image the BufferedImage to be processed
     */
    public void applyDither(BufferedImage image) {        
        if (rangeQ) {
            quantizer.prepare(image, bitValue);
        }
        
        int w = image.getWidth(), h = image.getHeight();
        int n = normalizedBayer.length;
        
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int[] rgba = cd.convertFromIntegerToArray(image.getRGB(x, y));
                
                double[] ch = new double[4];
                
                for (int i = 1; i <= 3; i++) {
                    ch[i] = rgba[i] / 255.0;
                }
                
                double d = normalizedBayer[y % n][x % n] * spread;
                
                for (int i = 1; i <= 3; i++) {
                    double v = ch[i] + d;
                    v = Math.min(1.0, Math.max(0.0, v));
                    int raw = (int) (v * 255);
                    
                    int[] temp = rgba.clone();
                    
                    temp[i] = raw;
                    
                    int[] qPixel = quantizer.quantize(temp, bitValue, rangeQ);
                    
                    rgba[i] = qPixel[i];
                }
                
                image.setRGB(x, y, cd.convertFromArrayToInteger(rgba));
            }
        }
    }
}