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
    public Object[] summedAreaTable (int[] initialTable, int width, int height) {
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
        int n = width * height;

        int[] pixels = new int[n];
        bmp.getPixels(pixels, 0, width, 0, 0, width, height);
        int[] SATR  = (int[]) summedAreaTable(pixels, width, height)[0];
        int[] SATG  = (int[]) summedAreaTable(pixels, width, height)[1];
        int[] SATB  = (int[]) summedAreaTable(pixels, width, height)[2];
        int weight = (2*radius+1)*(2*radius+1);
        for (int i = radius+1; i < width - radius; i++){
            for (int j = radius+1; j < height - radius; j++){
                pixels[i + j*width] = Color.rgb((SATR[i+radius+(j+radius)*width] - SATR[i-radius-1+(j+radius)*width] - SATR[i+radius+(j-radius-1)*width] + SATR[i-radius-1+(j-radius-1)*width]) / weight,
                        (SATG[i+radius+(j+radius)*width] - SATG[i-radius-1+(j+radius)*width] - SATG[i+radius+(j-radius-1)*width] + SATG[i-radius-1+(j-radius-1)*width]) / weight,
                        (SATB[i+radius+(j+radius)*width] - SATB[i-radius-1+(j+radius)*width] - SATB[i+radius+(j-radius-1)*width] + SATB[i-radius-1+(j-radius-1)*width]) / weight);
            }
        }
        bmp.setPixels(pixels, 0, width,  0, 0, width, height);
    }
    /* End of average blurring effect */

    public void laplacien(){
        Bitmap bmp = this.getBmp();
        int[] pixels = new int[width * height];
        bmp.getPixels(pixels, 0, width, 0, 0, width, height);
        int g;
        int[] tab = arrayOfGrayPixels(pixels, height, width);
        for (int i = 1; i < width-1; i++){
            for (int j = 1; j< height-1; j++) {
                g = (-4)*Color.red(tab[i+j*width]) +Color.red(tab[i-1+j*width]) +Color.red(tab[i+1+j*width]) +Color.red(tab[i+(j-1)*width]) +Color.red(tab[i+(j+1)*width]);
                pixels[i+j*width] = Color.rgb(g, g, g);
            }
        }
        bmp.setPixels(pixels, 0, width,  0, 0, width, height);
    }

    // x = distance au centre de la matrice; y normalise la valeur au centre à 10 fois le rayon.
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

    //TODO   1 = AverageBlur    2 = GaussianBlur    3 = Contouring    4 = Sharpening
    public void convolution (int[] matrixConvo) {
        Bitmap bmp = this.getBmp();
        
        int[] pixels = new int[width * height];
        int[] pixelsf = new int[width * height];
        bmp.getPixels(pixels, 0, width, 0, 0, width, height);
        bmp.getPixels(pixelsf, 0, width, 0, 0, width, height);
        int matrixWidth = (int) Math.sqrt(matrixConvo.length);
        int radius = ((matrixWidth-1)/2);

        float param1;
        float param2;
        float param3;
        int weight = 0;

        for (int i = 0; i < matrixWidth*matrixWidth; i++) {
            weight += matrixConvo[i];
        }
        for (int k = radius; k < width - radius; k++) {
            for (int l = radius; l < height - radius; l++) {
                param1 = 0;
                param2 = 0;
                param3 = 0;
                for (int m = 0; m < matrixWidth; m++) {
                    for (int o = 0; o < matrixWidth; o++) {
                        param1 += Color.red(pixels[k - radius + o + (l - radius + m) * width]) * matrixConvo[o + m * matrixWidth];
                        param2 += Color.green(pixels[k - radius + o + (l - radius + m) * width]) * matrixConvo[o + m * matrixWidth];
                        param3 += Color.blue(pixels[k - radius + o + (l - radius + m) * width]) * matrixConvo[o + m * matrixWidth];
                    }
                }
                pixelsf[k + l * width] = Color.rgb((int) param1 / weight, (int) param2 / weight, (int) param3 / weight);
            }
        }
        bmp.setPixels(pixelsf, 0, width,  0, 0, width, height);
    }

    public void contouring (int radius) {
        Bitmap bmp = this.getBmp();
        
        int[] pixels = new int[width * height];
        int[] pixelsf = new int[width * height];
        
        bmp.getPixels(pixels, 0, width, 0, 0, width, height);
        bmp.getPixels(pixelsf, 0, width, 0, 0, width, height);
        
        int matrixWidth = 2 * radius + 1;
        int[] matrixConvo = ConvolutionMatrix(3, matrixWidth);
        int[] matrixConvo1 = ConvolutionMatrix(0, matrixWidth);
        
        pixels = arrayOfGrayPixels(pixels, height, width);
        
        float param1, param2;
        for (int k = radius; k < width - radius; k++) {
            for (int l = radius; l < height - radius; l++) {
                param1 = 0;
                param2 = 0;
                for (int m = 0; m < matrixWidth; m++) {
                    for (int o = 0; o < matrixWidth; o++) {
                        param1 += Color.red(pixels[k - radius + o + (l - radius + m) * width]) * matrixConvo[o + m * matrixWidth];
                        param2 += Color.red(pixels[k - radius + o + (l - radius + m) * width]) * matrixConvo1[o + m * matrixWidth];
                    }
                }
                int norm = (int) Math.min(Math.sqrt(param1 * param1 + param2 * param2), 255);
                pixelsf[k + l * width] = Color.rgb( norm, norm, norm);
            }
        }
        
        bmp.setPixels(pixelsf, 0, width,  0, 0, width, height);
    }
}
