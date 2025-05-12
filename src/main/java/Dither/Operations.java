package Dither;

import FileManager.PngReader;
import FileManager.PngSaver;

import Windows.ImageViewer;

import java.awt.image.BufferedImage;

public class Operations {
    private final int scale;
    private final int colorLevels;
    private final double spread;
    private final TYPE operation;
    private final boolean rangeQ;
    private final boolean grayscale;
    private final int bayerDitherSize = 8;
    
    /**
     * Initializes the Operations instance with the desired configuration.
     *
     * @param colorLevels Number of quantization color levels.
     * @param scale Scaling factor (for downscaling/upscaling).
     * @param spread Error diffusion spread factor.
     * @param rangeQ Whether to apply range quantization.
     * @param operation Dithering algorithm type to use.
     * @param grayscale Whether to convert the image to grayscale before processing.
     */
    public Operations(int colorLevels, int scale, double spread, boolean rangeQ, TYPE operation, boolean grayscale) {
        this.colorLevels = colorLevels;
        this.scale = scale;
        this.spread = spread;
        this.operation = operation;
        this.rangeQ = rangeQ;
        this.grayscale = grayscale;
    }
    
    /**
     * Executes the full image processing pipeline.
     *
     * @param filePath Path to the image file to be processed.
     */
    public void processFile(String filePath) {
        BufferedImage image = measureTime("Reading File", () -> readImage(filePath));
        
        if (scale > 1) {
            final BufferedImage downInput = image;
            image = measureTime("Scaling Image Down", () -> new Scaler().scaleDown(downInput, scale));
        }
        
        final BufferedImage ditherInput = image;
        measureTime("Applying Dither Pattern: " + operation, () -> applyDithering(ditherInput));

        if (scale > 1) {
            final BufferedImage upInput = image;
            image = measureTime("Scaling Image Up", () -> new Scaler().scaleUp(upInput, scale));
        }
        
        new ImageViewer(image, filePath, this);
    }

    private BufferedImage readImage(String path) {
        return new PngReader().readPNG(path, grayscale);
    }
    
    private void applyDithering(BufferedImage image) {
        switch (operation) {
            case Bayer8x8 -> {
                new OrderedDithering(bayerDitherSize).applyDither(image, spread);
                new Quantization().applyQuantization(image, colorLevels, rangeQ);
            }
            case Floyd_Steinberg -> {
                new DiffusionDithering(colorLevels, rangeQ, spread).applyFloydSteinberg(image);
            }
            case JJN -> {
                new DiffusionDithering(colorLevels, rangeQ, spread).applyJarvisJudiceNinke(image);
            }
            case Stucki -> {
                new DiffusionDithering(colorLevels, rangeQ, spread).applyStucki(image);
            }
            case Atkinson -> {
                new DiffusionDithering(colorLevels, rangeQ, spread).applyAtkinson(image);
            }
            case Burkes -> {
                new DiffusionDithering(colorLevels, rangeQ, spread).applyBurkes(image);
            }
            case Sierra -> {
                new DiffusionDithering(colorLevels, rangeQ, spread).applySierra(image);
            }
            case Two_Row_Sierra -> {
                new DiffusionDithering(colorLevels, rangeQ, spread).applyTwoRowSierra(image);
            }
            case Sierra_Lite -> {
                new DiffusionDithering(colorLevels, rangeQ, spread).applySierraLite(image);
            }
            default -> {
                new Quantization().applyQuantization(image, colorLevels, rangeQ);
            }
        }
    }
    
    /**
     * Saves the processed image to a file with a name that reflects the applied
     * parameters.
     *
     * @param image The image to be saved.
     * @param filePath The original file path used as a base for the saved file.
     */
    public void saveImage(BufferedImage image, String filePath) {
        String name = "Quantize[" + operation + "," + colorLevels + "," + scale + "," + spread + "]";
        
        new PngSaver().saveToFile(name, filePath, image);
    }

    private BufferedImage measureTime(String label, Timeable<BufferedImage> action) {
        long start = System.currentTimeMillis();
        
        System.out.println(label);
        BufferedImage result = action.execute();
        
        long end = System.currentTimeMillis();
        System.out.println("TIME: " + (end - start) + "ms");
        
        return result;
    }

    private void measureTime(String label, Runnable action) {
        long start = System.currentTimeMillis();
        
        System.out.println(label);
        action.run();
        
        long end = System.currentTimeMillis();
        System.out.println("TIME: " + (end - start) + "ms");
    }

    @FunctionalInterface
    private interface Timeable<T> {
        T execute();
    }
}