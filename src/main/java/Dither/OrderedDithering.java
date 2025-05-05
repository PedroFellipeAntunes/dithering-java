package Dither;

import java.awt.image.BufferedImage;

public class OrderedDithering {
    private final int[][] originalBayer;
    
    public OrderedDithering(int n) {
        BayerCalculator bc = new BayerCalculator();
        this.originalBayer = bc.computeBayerMatrix(n);
    }
    
    public OrderedDithering() {
        BayerCalculator bc = new BayerCalculator();
        this.originalBayer = bc.computeBayerMatrix(2);
    }
    
    // Normalize and convert to (-1 <-> 1)
    private double[][] normalizeBayer() {
        int n = this.originalBayer.length;
        double n2 = n * n;
        
        double[][] normalizedBayer = new double[n][n];
        
        for (int y = 0; y < n; y++) {
            for (int x = 0; x < n; x++) {
                normalizedBayer[y][x] = this.originalBayer[y][x] / n2;
            }
        }
        
        double maxNormal = Double.NEGATIVE_INFINITY;
        
        // Heighest value of matrix
        for (int y = 0; y < n; y++) {
            for (int x = 0; x < n; x++) {
                if (normalizedBayer[y][x] > maxNormal) {
                    maxNormal = normalizedBayer[y][x];
                }
            }
        }
        
        // Subtract 1/2 * max from each element
        for (int y = 0; y < n; y++) {
            for (int x = 0; x < n; x++) {
                normalizedBayer[y][x] -= 0.5 * maxNormal;
            }
        }
        
        return normalizedBayer;
    }
    
    public void applyDither(BufferedImage image) {
        double[][] bayer = normalizeBayer();
        ConvertRgbaInteger cri = new ConvertRgbaInteger();

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int[] rgba = cri.convertFromIntegerToArray(image.getRGB(x, y));
                
                // Normalize rgba
                double[] channels = new double[4];
                for (int i = 1; i < 4; i++) {
                    channels[i] = rgba[i] / 255.0;
                }
                
                // Get the equivalent index in the bayer matrix
                double dither = bayer[y % bayer.length][x % bayer.length];
                
                // Apply dithering (clamp result between 0 and 1)
                for (int i = 1; i < 4; i++) {
                    channels[i] = Math.min(1.0, Math.max(0.0, channels[i] + dither));
                    rgba[i] = (int) (channels[i] * 255);
                }

                int newRGBA = cri.convertFromArrayToInteger(rgba);
                image.setRGB(x, y, newRGBA);
            }
        }
    }
}