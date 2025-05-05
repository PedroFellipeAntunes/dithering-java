package Dither;

import FileManager.PngReader;
import FileManager.PngSaver;
import Windows.ImageViewer;
import java.awt.image.BufferedImage;

public class Operations {
    public static TYPE operation;
    public static int scale, colorLevels;
    int bayerDitherSize = 8;
    
    public void processFile(String filePath, int colorLevels, int scale, boolean rangeQ, TYPE operation, boolean gray) {
        Operations.operation = operation;
        Operations.scale = scale;
        Operations.colorLevels = colorLevels;
        
        PngReader imageToPixelList = new PngReader();
        
        //Get 2D matrix of pixels
        BufferedImage image = imageToPixelList.readPNG(filePath, gray);
        
        Scaler sc = new Scaler();
        image = sc.scaleDown(image, scale);
        
        long startTime = System.currentTimeMillis();
        
        // Dithering of image
        System.out.println(operation);
        switch (operation) {
            case Bayer8x8 -> {
                    OrderedDithering od = new OrderedDithering(bayerDitherSize);
                    od.applyDither(image);
                    Quantization band = new Quantization();
                    band.applyQuantization(image, colorLevels, rangeQ);
                }
            case Floyd_Steinberg -> {
                DiffusionDithering dd = new DiffusionDithering(colorLevels, rangeQ);
                dd.applyFloydSteinberg(image);
            }
            case JJN -> {
                DiffusionDithering dd = new DiffusionDithering(colorLevels, rangeQ);
                dd.applyJarvisJudiceNinke(image);
            }
            case Stucki -> {
                DiffusionDithering dd = new DiffusionDithering(colorLevels, rangeQ);
                dd.applyStucki(image);
            }
            case Atkinson -> {
                DiffusionDithering dd = new DiffusionDithering(colorLevels, rangeQ);
                dd.applyAtkinson(image);
            }
            case Burkes -> {
                DiffusionDithering dd = new DiffusionDithering(colorLevels, rangeQ);
                dd.applyBurkes(image);
            }
            case Sierra -> {
                DiffusionDithering dd = new DiffusionDithering(colorLevels, rangeQ);
                dd.applySierra(image);
            }
            case Two_Row_Sierra -> {
                DiffusionDithering dd = new DiffusionDithering(colorLevels, rangeQ);
                dd.applyTwoRowSierra(image);
            }
            case Sierra_Lite -> {
                DiffusionDithering dd = new DiffusionDithering(colorLevels, rangeQ);
                dd.applySierraLite(image);
            }
            default -> {
                    Quantization band = new Quantization();
                    band.applyQuantization(image, colorLevels, rangeQ);
                }
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        System.out.println("TIME: " + duration + "ms");
        
        image = sc.scaleUp(image, scale);
        
        //View images before saving
        ImageViewer viewer = new ImageViewer(image, filePath);
    }
    
    //Save files
    public static void saveImage(BufferedImage image, String filePath) {
        PngSaver listToImage = new PngSaver();
        
        listToImage.saveToFile("Quantize[" + operation + "," + colorLevels + "color," + scale + "]", filePath, image);
    }
}