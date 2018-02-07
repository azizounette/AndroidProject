package com.example.alarvet.seguin_larvet_androidproject;

import android.graphics.Bitmap;
import android.graphics.Color;

/**
 * Created by Aziza on 03/02/2018.
 */

public class Convolution extends Filter {

    public Convolution(Bitmap bmp){
        super(bmp);
    }

    /* Average blurring effect */
    private Object[] summedAreaTable (int[] initialTable, int width, int height) {
        int[] SATR = new int[width*height];
        int[] SATG = new int[width*height];
        int[] SATB = new int[width*height];

        for (int  i = 0; i < width*height; i++) {
            SATR[i] = Color.red(initialTable[i]);
            SATG[i] = Color.green(initialTable[i]);
            SATB[i] = Color.blue(initialTable[i]);
        }
        for (int i = 1; i < width; i++) {
            SATR[i] += SATR[i-1];
            SATG[i] += SATG[i-1];
            SATB[i] += SATB[i-1];
        }
        for (int i = 1; i < height; i++) {
            SATR[i*width] += SATR[(i-1)*width];
            SATG[i*width] += SATG[(i-1)*width];
            SATB[i*width] += SATB[(i-1)*width];
        }
        for (int i = 1; i < width; i++){
            for (int j = 1; j < height; j++) {
                SATR[i + j*width] += SATR[i-1 + j*width] + SATR[i + (j-1)*width] - SATR[i-1 + (j-1)*width];
                SATG[i + j*width] += SATG[i-1 + j*width] + SATG[i + (j-1)*width] - SATG[i-1 + (j-1)*width];
                SATB[i + j*width] += SATB[i-1 + j*width] + SATB[i + (j-1)*width] - SATB[i-1 + (j-1)*width];
            }
        }

        Object[] SATs = new Object[3];
        SATs[0] = SATR;
        SATs[1] = SATG;
        SATs[2] = SATB;

        return SATs;
    }

    public void averageBlurring (int radius) {
        Bitmap bmp = this.getBmp();
        int w = bmp.getWidth();
        int h = bmp.getHeight();
        int n = w * h;

        int[] pixels = new int[n];
        bmp.getPixels(pixels, 0, w, 0, 0, w, h);
        int[] SATR  = (int[]) summedAreaTable(pixels, w, h)[0];
        int[] SATG  = (int[]) summedAreaTable(pixels, w, h)[1];
        int[] SATB  = (int[]) summedAreaTable(pixels, w, h)[2];
        int weight = (2*radius+1)*(2*radius+1);
        for (int i = radius+1; i < w - radius; i++){
            for (int j = radius+1; j < h - radius; j++){
                pixels[i + j*w] = Color.rgb((SATR[i+radius+(j+radius)*w] - SATR[i-radius-1+(j+radius)*w] - SATR[i+radius+(j-radius-1)*w] + SATR[i-radius-1+(j-radius-1)*w]) / weight,
                        (SATG[i+radius+(j+radius)*w] - SATG[i-radius-1+(j+radius)*w] - SATG[i+radius+(j-radius-1)*w] + SATG[i-radius-1+(j-radius-1)*w]) / weight,
                        (SATB[i+radius+(j+radius)*w] - SATB[i-radius-1+(j+radius)*w] - SATB[i+radius+(j-radius-1)*w] + SATB[i-radius-1+(j-radius-1)*w]) / weight);
            }
        }
        bmp.setPixels(pixels, 0, w,  0, 0, w, h);
    }
    /* End of average blurring effect */


}
