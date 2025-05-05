package Dither;

public class TST {
    public static void main(String[] args) {
        String imagePath = "gradient.png";
        
        int bitValue = 1;
        int scale = 1;
        boolean gray = true;
        boolean rangeQ = false;
        
        Operations ops = new Operations();
        
        for (TYPE type : TYPE.values()) {
            ops.processFile(imagePath, bitValue, scale, rangeQ, type, gray);
        }
    }
}
