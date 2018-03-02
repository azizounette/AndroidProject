package com.example.alarvet.seguin_larvet_androidproject;

import android.graphics.Bitmap;
import android.graphics.Color;

/**
 * Created by Aziza on 03/02/2018.
 */

public class Contrast extends Filter {

    public Contrast(Bitmap bmp){
        super(bmp);
    }

    /* Histogram equalizer for contrasts in black and white */
    private int[] histogram(Bitmap bmp) {
        int[] res = new int[256];
        Bitmap bmCopy = bmp.copy(bmp.getConfig(), true);
        Colour colour = new Colour(bmCopy);
        colour.toGray(); //TODO Can we do better?

        int pixels[] = new int[width*height];
        bmCopy.getPixels(pixels, 0, width, 0, 0, width, height);
        for (int i = 0; i < width*height; i++)
            res[Color.red(pixels[i])]++;
        return res;
    }

    private int[] cumulativeHist(int[] hist) {
        int[] res = new int[256];
        res[0] = hist[0];
        for (int i = 1; i < 256; i++)
            res[i] = res[i - 1] + hist[i];
        return res;
    }

    public void equalizeGray() {
        Bitmap bmp = this.getBmp();
        int[] cumulativeH = cumulativeHist(histogram(bmp));
        int n = width * height;
        int pixels[] = new int[n];
        bmp.getPixels(pixels, 0, width, 0, 0, width, height);
        for (int i = 0; i < width*height; i++) {
            int r = (cumulativeH[Color.red(pixels[i])] * 255 / n);
            pixels[i] = Color.rgb(r, r, r);
        }
        bmp.setPixels(pixels, 0, width, 0, 0, width, height);
    }
    /*End of histogram equalizer in black and white */


    /* Histogram equalizer for colored pictures */
    public void equalizeColors() {
        Bitmap bmp = this.getBmp();
        int[] cumulativeH = cumulativeHist(histogram(bmp));
        int n = width * height;

        int pixels[] = new int[n];
        bmp.getPixels(pixels, 0, width, 0, 0, width, height);
        for (int i = 0; i < width*height; i++) {
            int r = (cumulativeH[Color.red(pixels[i])] * 255 / n);
            int g = (cumulativeH[Color.green(pixels[i])] * 255 / n);
            int b = (cumulativeH[Color.blue(pixels[i])] * 255 / n);
            pixels[i] = Color.rgb(r, g, b);
        }
        bmp.setPixels(pixels, 0, width, 0, 0, width, height);
    }

    //TODO Value between -128 and 128
    public void contrastChange (float value) {
        Bitmap bmp = this.getBmp();
        int[] pixels = new int[width * height];
        bmp.getPixels(pixels, 0, width, 0, 0, width, height);
        float k = 259*(value+255)/255/(259-value);
        int Red, Green, Blue;
        for (int i = 0; i < height*width; i++) {
            Red = Math.max(Math.min((int) (k * (Color.red(pixels[i])-128) + 128), 255),0);
            Green = Math.max(Math.min((int) (k * (Color.green(pixels[i])-128) + 128), 255),0);
            Blue = Math.max(Math.min((int) (k * (Color.blue(pixels[i])-128) + 128), 255),0);
            pixels[i] = Color.rgb(Red, Green, Blue);
        }
        bmp.setPixels(pixels, 0, width,  0, 0, width, height);
    }
    //TODO Value between -64 and 64
    public void warmthChange (float value) {
        Bitmap bmp = this.getBmp();
        int[] pixels = new int[width * height];
        bmp.getPixels(pixels, 0, width, 0, 0, width, height);
        int Red, Blue;
        float k = 258*(value+255)/255/(258-value);
        float v = 258*(-value+255)/255/(258+value);
        for (int i = 0; i < height*width; i++) {
            Red = Math.max(Math.min((int) (v * (Color.red(pixels[i])-128) + 128), 255),0);
            Blue = Math.max(Math.min((int) (k * (Color.blue(pixels[i])-128) + 128), 255),0);
            pixels[i] = Color.rgb(Red, Color.green(pixels[i]), Blue);
        }
        bmp.setPixels(pixels, 0, width,  0, 0, width, height);
    }

    public void magicWand (int value) {
        Bitmap bmp = this.getBmp();
        int[] pixels = new int[width * height];
        bmp.getPixels(pixels, 0, width, 0, 0, width, height);
        int Red, Blue;
        for (int i = 0; i < height*width; i++) {
            Blue = Math.max(0,Color.blue(pixels[i])*(100+Math.min(value, 0)))/100;
            Red = Math.max(0,Color.red(pixels[i])*(100-Math.max(value, 0))/100);
            pixels[i] = Color.rgb(Red, Color.green(pixels[i]), Blue);
        }
        bmp.setPixels(pixels, 0, width,  0, 0, width, height);
    }
}
