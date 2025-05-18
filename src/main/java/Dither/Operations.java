package Dither;

import Dither.Util.Scaler;
import Dither.Util.TYPE;
import Dither.Interface.HsbQuantizer;
import Dither.Interface.ColorQuantizer;
import Dither.Interface.RgbQuantizer;

import FileManager.PngReader;
import FileManager.PngSaver;

import Windows.ImageViewer;

import java.awt.image.BufferedImage;

public class Operations {
    private final boolean useHsb;
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
     * @param useHsb Wheter to use RGB or HSB.
     * @param operation Dithering algorithm type to use.
     * @param grayscale Whether to convert the image to grayscale before processing.
     */
    public Operations(int colorLevels, int scale, double spread, boolean rangeQ, boolean useHsb, TYPE operation, boolean grayscale) {
        this.colorLevels = colorLevels;
        this.scale = scale;
        this.spread = spread;
        this.operation = operation;
        this.rangeQ = rangeQ;
        this.useHsb = useHsb;
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
        ColorQuantizer cq;
        
        if (useHsb) {
            cq = new HsbQuantizer();
        } else {
            cq = new RgbQuantizer();
        }
        
        switch (operation) {
            case Bayer8x8 -> {
                new OrderedDithering(cq, bayerDitherSize, colorLevels, rangeQ, spread).applyDither(image);
            }
            case Floyd_Steinberg -> {
                new DiffusionDithering(cq, colorLevels, rangeQ, spread).applyFloydSteinberg(image);
            }
            case JJN -> {
                new DiffusionDithering(cq, colorLevels, rangeQ, spread).applyJarvisJudiceNinke(image);
            }
            case Stucki -> {
                new DiffusionDithering(cq, colorLevels, rangeQ, spread).applyStucki(image);
            }
            case Atkinson -> {
                new DiffusionDithering(cq, colorLevels, rangeQ, spread).applyAtkinson(image);
            }
            case Burkes -> {
                new DiffusionDithering(cq, colorLevels, rangeQ, spread).applyBurkes(image);
            }
            case Sierra -> {
                new DiffusionDithering(cq, colorLevels, rangeQ, spread).applySierra(image);
            }
            case Two_Row_Sierra -> {
                new DiffusionDithering(cq, colorLevels, rangeQ, spread).applyTwoRowSierra(image);
            }
            case Sierra_Lite -> {
                new DiffusionDithering(cq, colorLevels, rangeQ, spread).applySierraLite(image);
            }
            default -> {
                cq.quantizeImage(image, colorLevels, rangeQ);
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