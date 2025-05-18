package Dither.Interface;

import Dither.HsbQuantization;

import Dither.Util.DataConverter;
import Dither.Util.LuminanceRangeCalculator;

import java.awt.Color;
import java.awt.image.BufferedImage;

public class HsbQuantizer implements ColorQuantizer {
    private final DataConverter dc = new DataConverter();
    private final HsbQuantization base = new HsbQuantization();
    
    private double minB, maxB;

    @Override
    public void prepare(BufferedImage image, int levels) {
        double[] range = LuminanceRangeCalculator.compute(image, true);
        minB = range[0];
        maxB = range[1];
    }

    @Override
    public int[] quantize(int[] rgba, int levels, boolean rangeQ) {
        Color c = new Color(rgba[1], rgba[2], rgba[3], rgba[0]);
        
        float[] hsb = Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null );

        if (rangeQ) {
            hsb[2] = base.quantizeFloatWithRange(hsb[2], levels, minB, maxB);
        } else {
            hsb[2] = base.quantizeFloatChannel(hsb[2], levels);
        }
        
        return dc.convertFromIntegerToArray(dc.convertFromHsbToRgb(hsb, rgba[0]));
    }
    
    @Override
    public void quantizeImage(BufferedImage image, int levels, boolean rangeQ) {
        base.applyQuantization(image, levels, rangeQ);
    }
}