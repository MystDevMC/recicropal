package com.mystdev.recicropal.common;

import java.awt.*;

public class ColorUtils {

    public static float[] convertToHSB(int colorInt) {
        var color = new Color(colorInt);
        return Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
    }

    public static int convertToRGB(float[] hsbArr) {
        return Color.HSBtoRGB(hsbArr[0], hsbArr[1], hsbArr[2]);
    }


    public static int clampToBrightness(int colorInt, int brightness) {
        var bFloat = (float) brightness / 255;
        var hsb = convertToHSB(colorInt);
        hsb[2] = Math.max(hsb[2], bFloat);
        return convertToRGB(hsb);
    }

}
