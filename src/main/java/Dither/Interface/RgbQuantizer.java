package Dither.Interface;

import Dither.RgbQuantization;

import Dither.Util.LuminanceRangeCalculator;

import java.awt.image.BufferedImage;

public class RgbQuantizer implements ColorQuantizer {
    private final RgbQuantization base = new RgbQuantization();
    private double min, max;

    @Override
    public void prepare(BufferedImage image, int levels) {
        double[] range = LuminanceRangeCalculator.compute(image, false);
        min = range[0];
        max = range[1];
    }

    @Override
    public int[] quantize(int[] rgba, int levels, boolean rangeQ) {
        int a = rgba[0], r = rgba[1], g = rgba[2], b = rgba[3];
        
        if (rangeQ) {
            r = base.quantizeWithRange(r, levels, min, max);
            g = base.quantizeWithRange(g, levels, min, max);
            b = base.quantizeWithRange(b, levels, min, max);
        } else {
            r = base.quantizeChannel(r, levels);
            g = base.quantizeChannel(g, levels);
            b = base.quantizeChannel(b, levels);
        }
        
        return new int[]{ a, r, g, b };
    }

    @Override
    public void quantizeImage(BufferedImage image, int levels, boolean rangeQ) {
        base.applyQuantization(image, levels, rangeQ);
    }
}