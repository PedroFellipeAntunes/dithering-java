package Dither;

import java.awt.image.BufferedImage;

public class OrderedDithering {
    private final int[][] bayerMatrix;
    private final double[][] normalizedBayer;
    private final Quantization quantizer = new Quantization();
    private final ConvertRgbaInteger converter = new ConvertRgbaInteger();
    
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
    public OrderedDithering(int n, int bitValue, boolean rangeQ, double spread) {
        BayerCalculator bc = new BayerCalculator();
        this.bayerMatrix = bc.computeBayerMatrix(n);
        this.normalizedBayer = normalizeBayer(this.bayerMatrix);
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
    public OrderedDithering(int bitValue, boolean rangeQ, double spread) {
        this(2, bitValue, rangeQ, spread);
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
        double min = 0, max = 255;
        
        if (rangeQ) {
            double[] range = quantizer.computeSymmetricLuminanceRange(image);
            min = range[0];
            max = range[1];
        }
        
        int w = image.getWidth(), h = image.getHeight();
        int n = normalizedBayer.length;
        
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int[] rgba = converter.convertFromIntegerToArray(image.getRGB(x, y));
                
                double[] ch = new double[4];
                
                for (int i = 1; i <= 3; i++) {
                    ch[i] = rgba[i] / 255.0;
                }
                
                double d = normalizedBayer[y % n][x % n] * spread;
                
                for (int i = 1; i <= 3; i++) {
                    double v = ch[i] + d;
                    v = Math.min(1.0, Math.max(0.0, v));
                    int raw = (int)(v * 255);
                    
                    if (rangeQ) {
                        rgba[i] = quantizer.quantizeWithRange(raw, bitValue, min, max);
                    } else {
                        rgba[i] = quantizer.quantizeChannel(raw, bitValue);
                    }
                }
                
                image.setRGB(x, y, converter.convertFromArrayToInteger(rgba));
            }
        }
    }
}