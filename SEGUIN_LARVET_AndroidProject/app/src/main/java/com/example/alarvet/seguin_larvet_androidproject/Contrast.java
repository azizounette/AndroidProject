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
        int w = bmp.getWidth();
        int h = bmp.getHeight();
        Bitmap bmCopy = bmp.copy(bmp.getConfig(), true);
        Colour colour = new Colour(bmCopy);
        colour.toGray(); //TODO Can we do better?

        int pixels[] = new int[w*h];
        bmCopy.getPixels(pixels, 0, w, 0, 0, w, h);
        for (int i = 0; i < w*h; i++)
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

    public void equalizer() {
        Bitmap bmp = this.getBmp();
        int[] cumulativeH = cumulativeHist(histogram(bmp));
        int w = bmp.getWidth();
        int h = bmp.getHeight();
        int n = w * h;

        int pixels[] = new int[n];
        bmp.getPixels(pixels, 0, w, 0, 0, w, h);
        for (int i = 0; i < w*h; i++) {
            int r = (cumulativeH[Color.red(pixels[i])] * 255 / n);
            pixels[i] = Color.rgb(r, r, r);
        }
        bmp.setPixels(pixels, 0, w, 0, 0, w, h);
    }
    /*End of histogram equalizer in black and white */


    /* Histogram equalizer for colored pictures */
    private void equalizeColors() {
        Bitmap bmp = this.getBmp();
        int[] cumulativeH = cumulativeHist(histogram(bmp));
        int w = bmp.getWidth();
        int h = bmp.getHeight();
        int n = w * h;

        int pixels[] = new int[n];
        bmp.getPixels(pixels, 0, w, 0, 0, w, h);
        for (int i = 0; i < w*h; i++) {
            int r = (cumulativeH[Color.red(pixels[i])] * 255 / n);
            int g = (cumulativeH[Color.green(pixels[i])] * 255 / n);
            int b = (cumulativeH[Color.blue(pixels[i])] * 255 / n);
            pixels[i] = Color.rgb(r, g, b);
        }
        bmp.setPixels(pixels, 0, w, 0, 0, w, h);
    }
}
