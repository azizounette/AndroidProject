package com.example.alarvet.seguin_larvet_androidproject;

import android.graphics.Bitmap;
import android.graphics.Color;

/**
 * Created by Aziza on 03/02/2018.
 */

public abstract class Filter {
    private final Bitmap bmp;
    protected int height;
    protected int width;

    public Filter(Bitmap bmp) {
        this.bmp = bmp;
        height = bmp.getWidth();
    }

    public  Bitmap getBmp() {
        return bmp;
    }

    public int[] arrayOfGrayPixels(int[] tab, int height, int width) {
        int[] res = new int[height*width];
        for (int i = 0; i < height*width; i++) {
            final int w = (11 * Color.blue(tab[i]) + 30 * Color.red(tab[i]) + 59 * Color.green(tab[i])) / 100;
            res[i] = Color.rgb(w, w, w);
        }
        return res;
    }
}
