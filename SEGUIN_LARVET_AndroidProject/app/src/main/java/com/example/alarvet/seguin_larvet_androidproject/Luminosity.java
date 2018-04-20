package com.example.alarvet.seguin_larvet_androidproject;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;

/**
 * This class applies luminosity filters.
 */
public class Luminosity extends Filter {

    public Luminosity(Bitmap bmp, Context context){
        super(bmp, context);
    }

    /**
     * This method overexposes the image : multiply the luminosity by 1.5.
     */
    public void overexposure() {
        Bitmap bmp = this.getBmp();
        double multiplier = 1.5; //TODO Pas trop grand???
        int[] pixels = new int[width*height];
        bmp.getPixels(pixels, 0, width, 0, 0, width, height);
        for (int i = 0; i < width*height; i++) {
            float[] hsv = new float[3];
            Color.RGBToHSV(Color.red(pixels[i]), Color.green(pixels[i]), Color.blue(pixels[i]), hsv);
            hsv[2] *= multiplier;
            pixels[i] = Color.HSVToColor(hsv);
        }
        bmp.setPixels(pixels, 0, width, 0, 0, width, height);
    }

    //TODO Value between -100 and 28
    /**
     * This method changes the luminosity of the image according to the parameter.
     * @param value the amount of luminosity to add.
     */
    public void luminosityChange(float value) {
        Bitmap bmp = this.getBmp();
        int[] pixels = new int[width * height];
        bmp.getPixels(pixels, 0, width, 0, 0, width, height);
        float k = 1 + value/100;
        int Red, Green, Blue;
        for (int i = 0; i < height*width; i++) {
            Red = Math.min((int)((Color.red(pixels[i]))*k), 255);
            Green = Math.min((int)((Color.green(pixels[i]))*k), 255);
            Blue = Math.min((int)((Color.blue(pixels[i]))*k), 255);
            pixels[i] = Color.rgb(Red, Green, Blue);
        }
        bmp.setPixels(pixels, 0, width,  0, 0, width, height);
    }
}
