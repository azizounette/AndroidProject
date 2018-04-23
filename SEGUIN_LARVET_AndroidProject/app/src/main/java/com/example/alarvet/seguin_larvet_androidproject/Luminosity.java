package com.example.alarvet.seguin_larvet_androidproject;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.RenderScript;

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
        double multiplier = 1.5;
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

    /**
     * This method changes the luminosity of the image according to the parameter.
     * It uses RenderScript.
     * @param value the amount of luminosity to add.
     */
    public void luminosityChange(float value) {
            RenderScript rs = RenderScript.create(getContext());

            Allocation input = Allocation.createFromBitmap(rs, getBmp());
            Allocation output = Allocation.createTyped(rs, input.getType());

            ScriptC_brightness brightnessScript = new ScriptC_brightness(rs);

            brightnessScript.set_brightnessScale(value);

            brightnessScript.forEach_negative(input, output);

            output.copyTo(getBmp());

            input.destroy(); output.destroy();
            brightnessScript.destroy(); rs.destroy();
    }
}