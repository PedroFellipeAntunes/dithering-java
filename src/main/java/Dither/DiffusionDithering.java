package Dither;

import java.awt.image.BufferedImage;

public class DiffusionDithering {
    private final Quantization b = new Quantization();
    private final ConvertRgbaInteger cri = new ConvertRgbaInteger();
    
    private final boolean rangeQ;
    private final int bitValue;
    private final double spread;
    
    /**
     * Constructs a DiffusionDithering instance with the specified number of
     * quantization levels and whether to use dynamic‑range quantization.
     *
     * @param bitValue the number of discrete color levels for quantization
     * (e.g., 2, 4, 8, …)
     * @param rangeQ true to apply dynamic‑range quantization, false for uniform
     * quantization
     * @param spread double value defines how much dither will be applied
     */
    public DiffusionDithering(int bitValue, boolean rangeQ, double spread) {
        this.bitValue = bitValue;
        this.rangeQ = rangeQ;
        this.spread = spread;
    }
    
    /**
     * Applies the Floyd–Steinberg error‑diffusion dithering algorithm to the
     * image.
     *
     * @param image the BufferedImage to be dithered in place
     */
    public void applyFloydSteinberg(BufferedImage image) {
        double[][] floydSteinberg = {
            {0, 0, 7.0/16},
            {3.0/16, 5.0/16, 1.0/16}
        };
        
        dither(image, floydSteinberg);
    }
    
    /**
     * Applies the Jarvis–Judice–Ninke (JJN) error‑diffusion dithering algorithm
     * to the image.
     *
     * @param image the BufferedImage to be dithered in place
     */
    public void applyJarvisJudiceNinke(BufferedImage image) {
        double[][] jjn = {
            {0, 0, 0, 7.0/48, 5.0/48},
            {3.0/ 48, 5.0/48, 7.0/48, 5.0/48, 3.0/48},
            {1.0/48, 3.0/48, 5.0/48, 3.0/48, 1.0/48}
        };
        
        dither(image, jjn);
    }
    
    /**
     * Applies the Stucki error‑diffusion dithering algorithm to the image.
     *
     * @param image the BufferedImage to be dithered in place
     */
    public void applyStucki(BufferedImage image) {
        double[][] stucki = {
            {0, 0, 0, 8.0/42, 4.0/42},
            {2.0/42, 4.0/42, 8.0/42, 4.0/42, 2.0/42},
            {1.0/42, 2.0/42, 4.0/42, 2.0/42, 1.0/42}
        };
        
        dither(image, stucki);
    }
    
    /**
     * Applies the Atkinson error‑diffusion dithering algorithm to the image.
     *
     * @param image the BufferedImage to be dithered in place
     */
    public void applyAtkinson(BufferedImage image) {
        // Added extra 0 to make it a 5 collumn matrix so it can actually work with my generic code for error diffusion
        double[][] atkinson = {
            {0, 0, 0, 1.0/8, 1.0/8},
            {0, 1.0/8, 1.0/8, 1.0/8, 0},
            {0, 0, 1.0/8, 0, 0}
        };
        
        dither(image, atkinson);
    }
    
    /**
     * Applies the Burkes error‑diffusion dithering algorithm to the image.
     *
     * @param image the BufferedImage to be dithered in place
     */
    public void applyBurkes(BufferedImage image) {
        double[][] burkes = {
            {0, 0, 0, 8.0/32, 4.0/32},
            {2.0/32, 4.0/32, 8.0/32, 4.0/32, 2.0/32}
        };
        
        dither(image, burkes);
    }
    
    /**
     * Applies the full Sierra error‑diffusion dithering algorithm to the image.
     *
     * @param image the BufferedImage to be dithered in place
     */
    public void applySierra(BufferedImage image) {
        double[][] sierra = {
            {0, 0, 0, 5.0/32, 3.0/32},
            {2.0/32, 4.0/32, 5.0/32, 4.0/32, 2.0/32},
            {0, 2.0/32, 3.0/32, 2.0/32, 0}
        };
        
        dither(image, sierra);
    }
    
    /**
     * Applies the Two‑Row Sierra error‑diffusion dithering algorithm to the
     * image.
     *
     * @param image the BufferedImage to be dithered in place
     */
    public void applyTwoRowSierra(BufferedImage image) {
        double[][] twoRowSierra = {
            {0, 0, 0, 4.0/16, 3.0/16},
            {1.0/16, 2.0/16, 3.0/16, 2.0/16, 1.0/16}
        };
        
        dither(image, twoRowSierra);
    }
    
    /**
     * Applies the Sierra Lite error‑diffusion dithering algorithm to the image.
     *
     * @param image the BufferedImage to be dithered in place
     */
    public void applySierraLite(BufferedImage image) {
        double[][] sierraLite = {
            {0, 0, 2.0/4},
            {1.0/4, 1.0/4, 0}
        };
        
        dither(image, sierraLite);
    }
    
    // Based on the wikipedia pseudo code
    private void dither(BufferedImage image, double[][] diffusion) {
        int matrixHeight = diffusion.length;
        int matrixWidth = diffusion[0].length;
        int matrixCenterX = matrixWidth / 2;
        
        double max = 255, min = 0; // Default values to compile
        
        if (rangeQ) {
            double[] range = b.computeSymmetricLuminanceRange(image);
            min = range[0];
            max = range[1];
        }
        
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int[] oldRGBA = cri.convertFromIntegerToArray(image.getRGB(x, y));
                
                int[] newRGBA = oldRGBA.clone();
                
                for (int c = 1; c <= 3; c++) {
                    if (rangeQ) {
                        newRGBA[c] = b.quantizeWithRange(oldRGBA[c], this.bitValue, min, max);
                    } else {
                        newRGBA[c] = b.quantizeChannel(oldRGBA[c], this.bitValue);
                    }
                }
                
                image.setRGB(x, y, cri.convertFromArrayToInteger(newRGBA));
                
                int[] error = new int[oldRGBA.length];
                
                for (int i = 0; i < oldRGBA.length; i++) {
                    error[i] = oldRGBA[i] - newRGBA[i];
                }
                
                // Diffuse error value to adjacent pixels
                for (int dy = 0; dy < matrixHeight; dy++) {
                    for (int dx = 0; dx < matrixWidth; dx++) {
                        int targetX = x + dx - matrixCenterX;
                        int targetY = y + dy;
                        
                        double factor = diffusion[dy][dx] * spread;

                        if (factor != 0.0) {
                            diffuseError(image, targetX, targetY, error, factor);
                        }
                    }
                }
            }
        }
    }
    
    private void diffuseError(BufferedImage image, int x, int y, int[] error, double factor) {
        // Out of bounds
        if (x < 0 || x >= image.getWidth() || y < 0 || y >= image.getHeight()) {
            return;
        }
        
        int[] rgba = cri.convertFromIntegerToArray(image.getRGB(x, y));
        
        // Ignore alpha
        for (int i = 1; i < rgba.length; i++) {
            rgba[i] = Math.max(0, Math.min(255, rgba[i] + (int) (error[i] * factor)));
        }
        
        image.setRGB(x, y, cri.convertFromArrayToInteger(rgba));
    }
}