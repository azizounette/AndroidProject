package com.example.alarvet.seguin_larvet_androidproject;

import android.graphics.Bitmap;
import android.graphics.Color;

/**
 * Created by maxseguin on 01/03/18.
 */

public class ComplexFilter extends Filter {
    public ComplexFilter(Bitmap bmp){
        super(bmp);
    }

    public int[] grayArray(int[] tab, int height, int width) {
        int[] res = new int[height*width];
        for (int i = 0; i < height*width; i++) {
            final int w = (11 * Color.blue(tab[i]) + 30 * Color.red(tab[i]) + 59 * Color.green(tab[i])) / 100;
            res[i] = Color.rgb(w, w, w);
        }
        return res;
    }
    
    //TODO val between 0 and 256
    public void warhol(int val) {
        Bitmap bmp = this.getBmp();
        int[] pixels = new int[width * height];
        bmp.getPixels(pixels, 0, width, 0, 0, width, height);
        pixels = grayArray(pixels, height, width);
        for (int i = 0; i < height*width; i++) {
            if (Color.red(pixels[i]) < val) {
                pixels[i] = Color.rgb(255,0,0);
            } else {
                pixels[i] = Color.rgb(0,255,255);
            }
        }
        bmp.setPixels(pixels, 0, width,  0, 0, width, height);
    }

    public void cartoon(int threshold){
        Bitmap bmp = this.getBmp();
        int[] pixels = new int[width * height];
        int[] pixelsf = new int[width * height];
        Convolution convo = new Convolution(bmp);
        convo.averageBlurring(5);
        bmp = convo.getBmp();
        bmp.getPixels(pixels, 0, width, 0, 0, width, height);
        bmp.getPixels(pixelsf, 0, width, 0, 0, width, height);
        int red = 0, green = 0, blue = 0, RED = 0, GREEN = 0, BLUE = 0;
        boolean exceedThreshold = false;
        for (int i = 4; i < width - 4; i++){
            for (int j = 4; j < height - 4; j++) {
                red = Math.abs(Color.red(pixels[i+j*width-1])-Color.red(pixels[i+j*width+1])) + Math.abs(Color.red(pixels[i+(j-1)*width])-Color.red(pixels[i+(j+1)*width]));
                green = Math.abs(Color.green(pixels[i+j*width-1])-Color.green(pixels[i+j*width+1])) + Math.abs(Color.green(pixels[i+(j-1)*width])-Color.green(pixels[i+(j+1)*width]));
                blue = Math.abs(Color.blue(pixels[i+j*width-1])-Color.blue(pixels[i+j*width+1])) + Math.abs(Color.blue(pixels[i+(j-1)*width])-Color.blue(pixels[i+(j+1)*width]));
                if (red+green+blue > threshold) {
                    exceedThreshold = true;
                } else {
                    red = Math.abs(Color.red(pixels[i+j*width-1])-Color.red(pixels[i+j*width+1]));
                    green = Math.abs(Color.green(pixels[i+j*width-1])-Color.green(pixels[i+j*width+1]));
                    blue = Math.abs(Color.blue(pixels[i+j*width-1])-Color.blue(pixels[i+j*width+1]));
                    if (red+green+blue > threshold) {
                        exceedThreshold = true;
                    } else {
                        red = Math.abs(Color.red(pixels[i+(j-1)*width])-Color.red(pixels[i+(j+1)*width]));
                        green = Math.abs(Color.green(pixels[i+(j-1)*width])-Color.green(pixels[i+(j+1)*width]));
                        blue = Math.abs(Color.blue(pixels[i+(j-1)*width])-Color.blue(pixels[i+(j+1)*width]));
                        if (red+green+blue > threshold) {
                            exceedThreshold = true;
                        } else {
                            red = Math.abs(Color.red(pixels[i+(j-1)*width-1])-Color.red(pixels[i+(j+1)*width+1])) + Math.abs(Color.red(pixels[i+(j-1)*width+1])-Color.red(pixels[i+(j+1)*width-1]));
                            green = Math.abs(Color.green(pixels[i+(j-1)*width-1])-Color.green(pixels[i+(j+1)*width+1])) + Math.abs(Color.green(pixels[i+(j-1)*width+1])-Color.green(pixels[i+(j+1)*width-1]));
                            blue = Math.abs(Color.blue(pixels[i+(j-1)*width-1])-Color.blue(pixels[i+(j+1)*width+1])) + Math.abs(Color.blue(pixels[i+(j-1)*width+1])-Color.blue(pixels[i+(j+1)*width-1]));
                            if (red+green+blue > threshold) {
                                exceedThreshold = true;
                            } else {
                                exceedThreshold = false;
                            }
                        }
                    }
                }
                if (exceedThreshold) {
                    RED = 0; GREEN = 0; BLUE = 0;
                } else {
                    RED = Color.red(pixels[i+j*width]); GREEN = Color.green(pixels[i+j*width]); BLUE = Color.blue(pixels[i+j*width]);
                }
                pixelsf[i+j*width] = Color.rgb(RED,GREEN,BLUE);
            }
        }
        bmp.setPixels(pixelsf, 0, width,  0, 0, width, height);
    }
}
