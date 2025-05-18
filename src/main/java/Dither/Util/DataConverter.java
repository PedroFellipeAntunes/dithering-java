package Dither.Util;

import java.awt.Color;

public class DataConverter {
    /**
     * Packs a 4-element [A,R,G,B] array into a single 32-bit ARGB integer.
     *
     * @param rgba an array of length 4 containing alpha, red, green, blue
     * (0–255)
     * @return the packed ARGB integer (0xAARRGGBB)
     */
    public int convertFromArrayToInteger(int[] rgba) {
        return (rgba[0] << 24) | (rgba[1] << 16) | (rgba[2] << 8) | rgba[3];
    }
    
    /**
     * Unpacks a 32-bit ARGB integer into a 4-element array [A,R,G,B].
     *
     * @param rgba the packed ARGB integer (0xAARRGGBB)
     * @return an array of length 4 with components alpha, red, green, blue
     * (0–255)
     */
    public int[] convertFromIntegerToArray(int rgba) {
        int[] result = new int[4];
        result[0] = (rgba >> 24) & 0xFF; // Alpha
        result[1] = (rgba >> 16) & 0xFF; // Red
        result[2] = (rgba >> 8) & 0xFF;  // Green
        result[3] = rgba & 0xFF;         // Blue
        
        return result;
    }
    
    /**
     * Converts an HSB color plus alpha into a packed ARGB integer.
     *
     * @param hsb a float array [hue, saturation, brightness] each in [0–1]
     * @param alpha alpha component (0–255)
     * @return the packed ARGB integer representing the HSB color with the given
     * alpha
     */
    public int convertFromHsbToRgb(float[] hsb, int alpha) {
        int rgbBits = Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]);
        int rgb = rgbBits & 0x00FFFFFF;
        
        return (alpha << 24) | rgb;
    }
}