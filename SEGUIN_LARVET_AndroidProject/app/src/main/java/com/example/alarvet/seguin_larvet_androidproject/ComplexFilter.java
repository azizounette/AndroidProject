package com.example.alarvet.seguin_larvet_androidproject;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;

/**
 * This class applies complex methods to the bitmap : Warhol effect and Cartoon effect.
 */
public class ComplexFilter extends Filter {
    public ComplexFilter(Bitmap bmp, Context context){
        super(bmp, context);
    }

    /**
     * This methods applies a Warhol effect : the bitmap is converted to gray, and any pixel that
     * have its red parameter (or blue or green because R=G=B) above the val parameter will be blue,
     * otherwise it will turn red.
     * @param val the threshold that will be check on every pixels
     */
    public void warhol(int val) {
        Bitmap bmp = this.getBmp();
        int[] pixels = new int[width * height];
        bmp.getPixels(pixels, 0, width, 0, 0, width, height);
        pixels = arrayOfGrayPixels(pixels, height, width);
        for (int i = 0; i < height*width; i++) {
            if (Color.red(pixels[i]) < val) {
                pixels[i] = Color.rgb(255,0,0);
            } else {
                pixels[i] = Color.rgb(0,255,255);
            }
        }
        bmp.setPixels(pixels, 0, width,  0, 0, width, height);
    }

    /**
     * This method applies a cartoon effect : it's a mix of contouring and blurring. First we build
     * the contouring of the image, and we add them to the initial image blurred.
     * @param threshold the threshold above which the contouring will be shown
     */
    public void cartoon(int threshold){
        Bitmap bmp = this.getBmp();
        int[] pixels = new int[width * height];
        int[] pixelsf = new int[width * height];
        Convolution convo = new Convolution(bmp, getContext());
        convo.averageBlurring(5);
        bmp = convo.getBmp();
        bmp.getPixels(pixels, 0, width, 0, 0, width, height);
        bmp.getPixels(pixelsf, 0, width, 0, 0, width, height);
        int red, green, blue, RED, GREEN, BLUE;
        boolean exceedThreshold;
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
                            exceedThreshold = (red+green+blue > threshold);
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
