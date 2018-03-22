package com.example.alarvet.seguin_larvet_androidproject;

import android.graphics.Bitmap;
import android.graphics.Color;

/**
 * This class is the mother class of all the other filter classes.
 * It shares the height and the width of the bitmap with all the classes.
 */
public abstract class Filter {
    private final Bitmap bmp;
    protected int height;
    protected int width;

    public Filter(Bitmap bmp) {
        this.bmp = bmp;
        height = bmp.getHeight();
        width = bmp.getWidth();
    }

    public  Bitmap getBmp() {
        return bmp;
    }

    /**
     * This method returns the gray filter applied image into an array.
     * @param tab the array containing the initial image
     * @param height height of the image
     * @param width width of the image
     * @return the gray filter applied array
     */
    public int[] arrayOfGrayPixels(int[] tab, int height, int width) {
        int[] res = new int[height*width];
        for (int i = 0; i < height*width; i++) {
            final int w = (11 * Color.blue(tab[i]) + 30 * Color.red(tab[i]) + 59 * Color.green(tab[i])) / 100;
            res[i] = Color.rgb(w, w, w);
        }
        return res;
    }
}
