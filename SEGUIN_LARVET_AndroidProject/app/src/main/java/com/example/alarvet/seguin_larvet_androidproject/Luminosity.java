package com.example.alarvet.seguin_larvet_androidproject;

import android.graphics.Bitmap;
import android.graphics.Color;

/**
 * Created by Aziza on 03/02/2018.
 */

public class Luminosity extends Filter {

    public Luminosity(Bitmap bmp){
        super(bmp);
    }

    public void overexposure() {
        Bitmap bmp = this.getBmp();
        double multiplier = 1.5; //TODO Pas trop grand???

        int w = bmp.getWidth();
        int h = bmp.getHeight();
        int[] pixels = new int[w*h];
        bmp.getPixels(pixels, 0, w, 0, 0, w, h);
        for (int i = 0; i < w*h; i++) {
            float[] hsv = new float[3];
            Color.RGBToHSV(Color.red(pixels[i]), Color.green(pixels[i]), Color.blue(pixels[i]), hsv);
            hsv[2] *= multiplier;
            pixels[i] = Color.HSVToColor(hsv);
        }
        bmp.setPixels(pixels, 0, w, 0, 0, w, h);
    }

    private void luminosityChange(Bitmap in, Bitmap out, float luminosityBarProgress) {
        int outh = in.getHeight();
        int outw = in.getWidth();
        int[] pixels = new int[outw * outh];
        in.getPixels(pixels, 0, outw, 0, 0, outw, outh);
        float k = 1 + luminosityBarProgress/100;
        int Red, Green, Blue;
        for (int i = 0; i < outh*outw; i++) {
            Red = Math.min((int)((Color.red(pixels[i]))*k), 255);
            Green = Math.min((int)((Color.green(pixels[i]))*k), 255);
            Blue = Math.min((int)((Color.blue(pixels[i]))*k), 255);
            pixels[i] = Color.rgb(Red, Green, Blue);
        }
        out.setPixels(pixels, 0, outw,  0, 0, outw, outh);
    }
}
