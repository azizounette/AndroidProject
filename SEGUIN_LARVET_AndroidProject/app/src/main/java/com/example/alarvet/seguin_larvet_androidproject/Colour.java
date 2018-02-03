package com.example.alarvet.seguin_larvet_androidproject;

import android.graphics.Bitmap;
import android.graphics.Color;

/**
 * Created by Aziza on 03/02/2018.
 */

public class Colour extends Filter {

    public Colour(Bitmap bmp) {
        super(bmp);
    }

    public void toGray() {
        Bitmap bmp = this.getBmp();
        int outH = bmp.getHeight();
        int outW = bmp.getWidth();

        int pixels[] = new int[outW*outH];
        bmp.getPixels(pixels, 0, outW, 0, 0, outW, outH);
        for (int i = 0; i < outW*outH; i++) {
            int gray = (int) (0.11 * Color.blue(pixels[i]) + 0.3 * Color.red(pixels[i]) + 0.59 * Color.green(pixels[i]));
            pixels[i] = Color.rgb(gray, gray, gray);
        }
        bmp.setPixels(pixels, 0, outW, 0, 0, outW, outH);
    }

    public void grayAndTint(int tint) {
        Bitmap bmp = this.getBmp();
        int offset = 30;
        int outH = bmp.getHeight();
        int outW = bmp.getWidth();

        int pixels[] = new int[outW*outH];
        bmp.getPixels(pixels, 0, outW, 0, 0, outW, outH);
        for (int i = 0; i < outW*outH; i++) {
            float[] hsv = new float[3];
            Color.colorToHSV(pixels[i], hsv);
            // TODO cercle chromatique
            if (!(hsv[0] < Math.min(360, tint + offset) && hsv[0] > Math.max(0, tint - offset))) {
                int gray = (int) (0.11 * Color.blue(pixels[i]) + 0.3 * Color.red(pixels[i]) + 0.59 * Color.green(pixels[i]));
                pixels[i] = Color.rgb(gray, gray, gray);
            }
        }
        bmp.setPixels(pixels, 0, outW, 0, 0, outW, outH);
    }

    public void changeTint(int tint) {
        Bitmap bmp = this.getBmp();
        int outH = bmp.getHeight();
        int outW = bmp.getWidth();

        int pixels[] = new int[outW*outH];
        bmp.getPixels(pixels, 0, outW, 0, 0, outW, outH);
        for (int i = 0; i < outW*outH; i++) {
            float[] hsv = new float[3];
            Color.colorToHSV(pixels[i], hsv);
            hsv[0] = tint;
            pixels[i] = Color.HSVToColor(hsv);
        }
        bmp.setPixels(pixels, 0, outW, 0, 0, outW, outH);
    }

}
