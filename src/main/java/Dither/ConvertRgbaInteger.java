package Dither;

public class ConvertRgbaInteger {
    public int convertFromArrayToInteger(int[] rgba) {
        return (rgba[0] << 24) | (rgba[1] << 16) | (rgba[2] << 8) | rgba[3];
    }
    
    public int[] convertFromIntegerToArray(int rgba) {
        int[] result = new int[4];
        result[0] = (rgba >> 24) & 0xFF; // Alpha
        result[1] = (rgba >> 16) & 0xFF; // Red
        result[2] = (rgba >> 8) & 0xFF;  // Green
        result[3] = rgba & 0xFF;         // Blue
        
        return result;
    }
}