package com.example.alarvet.seguin_larvet_androidproject;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.RenderScript;

/**
 * This class applies colour filters to the bitmap.
 */
public class Colour extends Filter {

    public Colour(Bitmap bmp, Context context) {
        super(bmp, context);
    }

    /**
     * This method applies a gray filter by calculating the weighted average of R, G and B for each pixels.
     */
    /*public void toGray() {
        Bitmap bmp = this.getBmp();

        int pixels[] = new int[width*height];
        bmp.getPixels(pixels, 0, width, 0, 0, width, height);
        for (int i = 0; i < width*height; i++) {
            int gray = (int) (0.11 * Color.blue(pixels[i]) + 0.3 * Color.red(pixels[i]) + 0.59 * Color.green(pixels[i]));
            pixels[i] = Color.rgb(gray, gray, gray);
        }
        bmp.setPixels(pixels, 0, width, 0, 0, width, height);
    }*/

    public void toGray() {
        RenderScript rs = RenderScript.create(getContext());

        Allocation input = Allocation.createFromBitmap(rs, getBmp());
        Allocation output = Allocation.createTyped(rs, input.getType());

        ScriptC_toGray grayScript = new ScriptC_toGray(rs);
        grayScript.forEach_toGray(input, output);

        output.copyTo(getBmp());

        input.destroy();
        output.destroy();
        grayScript.destroy();
        rs.destroy();
    }

    /**
     * This method applies a gray filter to every pixels but the one that are within the range of the selected color (tint).
     * @param tint int between 0 and 350 (bound the hue of the color)
     */
    public void grayAndTint(int tint) {
        Bitmap bmp = this.getBmp();
        int offset = 30;

        int pixels[] = new int[width*height];
        bmp.getPixels(pixels, 0, width, 0, 0, width, height);
        for (int i = 0; i < width*height; i++) {
            float[] hsv = new float[3];
            Color.colorToHSV(pixels[i], hsv);

            if (!(hsv[0] < Math.min(360, tint + offset) && hsv[0] > Math.max(0, tint - offset))) {
                int gray = (int) (0.11 * Color.blue(pixels[i]) + 0.3 * Color.red(pixels[i]) + 0.59 * Color.green(pixels[i]));
                pixels[i] = Color.rgb(gray, gray, gray);
            }
        }
        bmp.setPixels(pixels, 0, width, 0, 0, width, height);
    }

    /**
     * This method applies a color filter. The color is bound to the parameters : the applied color
     * HSV is (hue, saturation, value).
     * @param hue int between 0 and 359
     * @param saturation float between 0 and 1
     * @param value float between 0 and 1
     */
    public void changeTint(int hue, float saturation, float value) {
        Bitmap bmp = this.getBmp();
        int pixels[] = new int[width*height];
        bmp.getPixels(pixels, 0, width, 0, 0, width, height);
        float[] hsv = new float[3];
        hsv[0] = hue;
        hsv[1] = saturation;
        hsv[2] = value;
        int tintrgb = Color.HSVToColor(hsv);
        for (int i = 0; i < height*width; i++) {
            float w = (float) (0.11 * Color.blue(pixels[i]) + 0.3 * Color.red(pixels[i]) + 0.59 * Color.green(pixels[i]));
            pixels[i] = Color.rgb((int)(Color.red(tintrgb)*w/255), (int)(Color.green(tintrgb)*w/255), (int)(Color.blue(tintrgb)*w/255));
        }
        bmp.setPixels(pixels, 0, width, 0, 0, width, height);
    }

    /**
     * This method applies a sepia filter by doing a weighted average of R, G and B on R, G and B.
     */
    /*public void sepia(){
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
    }*/

    public void sepia() {
        RenderScript rs = RenderScript.create(getContext());

        Allocation input = Allocation.createFromBitmap(rs, getBmp());
        Allocation output = Allocation.createTyped(rs, input.getType());

        ScriptC_toSepia sepiaScript = new ScriptC_toSepia(rs);
        sepiaScript.forEach_toSepia(input, output);

        output.copyTo(getBmp());

        input.destroy();
        output.destroy();
        sepiaScript.destroy();
        rs.destroy();
    }

}
