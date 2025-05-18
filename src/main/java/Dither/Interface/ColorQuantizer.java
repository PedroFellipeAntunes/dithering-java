package Dither.Interface;

import java.awt.image.BufferedImage;

public interface ColorQuantizer {
    /**
     * Prepare any per-image state needed before quantization (e.g., compute
     * dynamic range).
     *
     * @param image the BufferedImage to be quantized
     * @param levels the number of quantization levels
     */
    default void prepare(BufferedImage image, int levels) { }
    
    /**
     * Quantize a single pixel provided as an [A,R,G,B] array.
     *
     * @param rgba array of components [alpha, red, green, blue] (0–255)
     * @param levels number of quantization levels
     * @param rangeQ true for dynamic-range quantization, false for uniform
     * @return the quantized pixel as an [A,R,G,B] array
     */
    int[] quantize(int[] rgba, int levels, boolean rangeQ);
    
    /**
     * Applies this quantizer to every pixel in the given image, in place.
     *
     * @param image the BufferedImage to process
     * @param levels number of quantization levels
     * @param rangeQ true for dynamic-range quantization, false for uniform
     */
    void quantizeImage(BufferedImage image, int levels, boolean rangeQ);
}