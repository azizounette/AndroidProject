package com.example.alarvet.seguin_larvet_androidproject;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;

/**
 * Created by Aziza on 03/02/2018.
 */

public class Colour extends Filter {

    public Colour(Bitmap bmp) {
        super(bmp);
    }


    public void toGray() {
        Bitmap bmp = this.getBmp();

        int pixels[] = new int[width*height];
        bmp.getPixels(pixels, 0, width, 0, 0, width, height);
        for (int i = 0; i < width*height; i++) {
            int gray = (int) (0.11 * Color.blue(pixels[i]) + 0.3 * Color.red(pixels[i]) + 0.59 * Color.green(pixels[i]));
            pixels[i] = Color.rgb(gray, gray, gray);
        }
        bmp.setPixels(pixels, 0, width, 0, 0, width, height);
    }

    public void grayAndTint(int tint) {
        Bitmap bmp = this.getBmp();
        int offset = 30;

        int pixels[] = new int[width*height];
        bmp.getPixels(pixels, 0, width, 0, 0, width, height);
        for (int i = 0; i < width*height; i++) {
            float[] hsv = new float[3];
            Color.colorToHSV(pixels[i], hsv);
            // TODO cercle chromatique
            if (!(hsv[0] < Math.min(360, tint + offset) && hsv[0] > Math.max(0, tint - offset))) {
                int gray = (int) (0.11 * Color.blue(pixels[i]) + 0.3 * Color.red(pixels[i]) + 0.59 * Color.green(pixels[i]));
                pixels[i] = Color.rgb(gray, gray, gray);
            }
        }
        bmp.setPixels(pixels, 0, width, 0, 0, width, height);
    }

    public void changeTint(int tint, float saturation, float value) {
        Bitmap bmp = this.getBmp();
        int pixels[] = new int[width*height];
        bmp.getPixels(pixels, 0, width, 0, 0, width, height);
        float[] hsv = new float[3];
        hsv[0] = tint;
        hsv[1] = saturation;
        hsv[2] = value;
        int tintrgb = Color.HSVToColor(hsv);
        for (int i = 0; i < height*width; i++) {
            float w = (float) (0.11 * Color.blue(pixels[i]) + 0.3 * Color.red(pixels[i]) + 0.59 * Color.green(pixels[i]));
            pixels[i] = Color.rgb((int)(Color.red(tintrgb)*w/255), (int)(Color.green(tintrgb)*w/255), (int)(Color.blue(tintrgb)*w/255));
        }
        bmp.setPixels(pixels, 0, width, 0, 0, width, height);
    }

    public void sepia(){
        Bitmap bmp = this.getBmp();
        int[] pixels = new int[width * height];
        bmp.getPixels(pixels, 0, width, 0, 0, width, height);
        int tr, tg, tb, red, green, blue;
        for (int i = 0; i < height*width; i++){
            red = Color.red(pixels[i]);
            green = Color.green(pixels[i]);
            blue = Color.blue(pixels[i]);
            tr = (int) (0.393*red + 0.796*green + 0.189*blue);
            tg = (int) (0.349*red + 0.686*green + 0.168*blue);
            tb = (int) (0.272*red + 0.534*green + 0.131*blue);
            red = Math.min(tr, 255);
            green = Math.min(tg, 255);
            blue = Math.min(tb, 255);
            pixels[i] = Color.rgb(red, green, blue);
        }
        bmp.setPixels(pixels, 0, width,  0, 0, width, height);
    }

}
