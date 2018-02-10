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

    private float gauss (int x, int y, double sigma, int radius) {
        return (float) (10*radius*Math.exp(-((y-radius)*(y-radius)+(x-radius)*(x-radius))/(2*sigma*sigma)));
    }

    // 1 = AverageBlur    2 = GaussianBlur    0/3 = Contouring    4 = Sharpening
    private int[] ConvolutionMatrix (int cases , int dimension) {
        int[] res = new int[dimension*dimension];
        switch (cases) {
            case 0:
                res[0] = -1;res[2] = 1;res[3] = -1;res[5] = 1;res[6] = -1;res[8] = 1;
                break;
            case 1:
                for (int i = 0; i < dimension*dimension; i++) {
                    res[i] = 1;
                }
                break;
            case 2:
                int radius = (dimension - 1)/2;
                double sigma = Math.sqrt(radius*radius/(Math.log(10*radius)));
                for (int m = 0; m < dimension; m++) {
                    for (int n = 0; n < dimension; n++) {
                        res[m + dimension * n] = (int) gauss(m, n, sigma, radius);
                    }
                }
                break;
            case 3:
                res[0] = -1;res[1] = -2;res[2] = -1;res[6] = 1;res[7] = 2;res[8] = 1;
                break;
            case 4:
                res[1] = -1; res[3] = -1; res[4] = 5; res[5] = -1; res[7] = -1;
                break;
        }
        return res;
    }

    public void convolutions (int[] matrixConvo) {
        Bitmap bmp = this.getBmp();
        int w = bmp.getWidth();
        int h = bmp.getHeight();
        int n = w * h;

        int[] pixels = new int[n];
        int[] pixelsRef = new int[n];
        bmp.getPixels(pixels, 0, w, 0, 0, w, h);

        int matrixWidth = (int) Math.sqrt(matrixConvo.length);
        int radius = ((matrixWidth-1)/2);

        float R;
        float G;
        float B;
        int weight = 0;
        for (int i = 0; i < matrixWidth*matrixWidth; i++) {
            weight += matrixConvo[i];
        }

        for (int k = radius; k < w - radius; k++) {
            for (int l = radius; l < h - radius; l++) {
                R = 0;
                G = 0;
                B = 0;
                for (int m = 0; m < matrixWidth; m++) {
                    for (int o = 0; o < matrixWidth; o++) {
                        R += Color.red(pixelsRef[k - radius + o + (l - radius + m) * w]) * matrixConvo[o + m * matrixWidth];
                        G += Color.green(pixelsRef[k - radius + o + (l - radius + m) * w]) * matrixConvo[o + m * matrixWidth];
                        B += Color.blue(pixelsRef[k - radius + o + (l - radius + m) * w]) * matrixConvo[o + m * matrixWidth];
                    }
                }
                pixels[k + l * w] = Color.rgb((int) R / weight, (int) G / weight, (int) B / weight);
            }
        }
        bmp.setPixels(pixels, 0, w,  0, 0, w, h);
    }


}
