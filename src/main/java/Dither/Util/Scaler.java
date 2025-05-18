package Dither.Util;

import java.awt.image.BufferedImage;

public class Scaler {
    /**
     * Scales the given image down by the specified integer factor using
     * nearest-neighbor sampling.
     *
     * @param image the original BufferedImage to be downscaled
     * @param scale the integer factor (>1) by which to reduce width and height
     * @return a new BufferedImage scaled down by the factor, or the original if
     * scale ≤ 1
     */
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
    
    /**
     * Scales the given image up by the specified integer factor using
     * nearest-neighbor sampling.
     *
     * @param image the original BufferedImage to be upscaled
     * @param scale the integer factor (>1) by which to multiply width and
     * height
     * @return a new BufferedImage scaled up by the factor, or the original if
     * scale ≤ 1
     */
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