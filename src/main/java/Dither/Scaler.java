package Dither;

import java.awt.image.BufferedImage;

public class Scaler {

    public BufferedImage scaleDown(BufferedImage image, int scale) {
        if (scale <= 1) {
            return image;
        }

        int scaledWidth = image.getWidth() / scale;
        int scaledHeight = image.getHeight() / scale;

        BufferedImage scaledImage = new BufferedImage(scaledWidth, scaledHeight, image.getType());

        for (int y = 0; y < scaledHeight; y++) {
            for (int x = 0; x < scaledWidth; x++) {
                int origX = x * scale;
                int origY = y * scale;

                int rgb = image.getRGB(origX, origY);
                scaledImage.setRGB(x, y, rgb);
            }
        }

        return scaledImage;
    }

    public BufferedImage scaleUp(BufferedImage image, int scale) {
        if (scale <= 1) {
            return image;
        }

        int scaledWidth = image.getWidth() * scale;
        int scaledHeight = image.getHeight() * scale;

        BufferedImage scaledImage = new BufferedImage(scaledWidth, scaledHeight, image.getType());

        for (int y = 0; y < scaledHeight; y++) {
            for (int x = 0; x < scaledWidth; x++) {
                int origX = x / scale;
                int origY = y / scale;

                int rgb = image.getRGB(origX, origY);
                scaledImage.setRGB(x, y, rgb);
            }
        }

        return scaledImage;
    }
}