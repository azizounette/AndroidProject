package com.example.alarvet.seguin_larvet_androidproject;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.RenderScript;

/**
 * this class applies contrast methods to the bitmap.
 */
public class Contrast extends Filter {

    public Contrast(Bitmap bmp, Context context){
        super(bmp, context);
    }

    /* Histogram equalizer for contrasts in black and white */
    private int[] histogram(Bitmap bmp) {
        int[] res = new int[256];
        Bitmap bmCopy = bmp.copy(bmp.getConfig(), true);
        int pixels1[] = new int[width*height];
        int pixels2[] = new int[width*height];
        bmCopy.getPixels(pixels1, 0, width, 0, 0, width, height);
        pixels2 = arrayOfGrayPixels(pixels1,height,width);
        for (int i = 0; i < width*height; i++)
            res[Color.red(pixels2[i])]++;
        return res;
    }

    private int[] cumulativeHist(int[] hist) {
        int[] res = new int[256];
        res[0] = hist[0];
        for (int i = 1; i < 256; i++)
            res[i] = res[i - 1] + hist[i];
        return res;
    }

    /**
     * This method equalize the gray histogram of the bitmap (require the bitmap to be applied a gray filter).
     */
    public void equalizeGray() {
        Bitmap bmp = this.getBmp();
        int[] cumulativeH = cumulativeHist(histogram(bmp));
        int n = width * height;
        int pixels[] = new int[n];
        bmp.getPixels(pixels, 0, width, 0, 0, width, height);
        for (int i = 0; i < width*height; i++) {
            int r = (cumulativeH[Color.red(pixels[i])] * 255 / n);
            pixels[i] = Color.rgb(r, r, r);
        }
        bmp.setPixels(pixels, 0, width, 0, 0, width, height);
    }
    /*End of histogram equalizer in black and white */


    /**
     * This method equalize the red, the green and the blue histograms.
     */
    /*public void equalizeColors() {
        Bitmap bmp = this.getBmp();
        int[] cumulativeH = cumulativeHist(histogram(bmp));
        int n = width * height;

        int pixels[] = new int[n];
        bmp.getPixels(pixels, 0, width, 0, 0, width, height);
        for (int i = 0; i < width*height; i++) {
            int r = (cumulativeH[Color.red(pixels[i])] * 255 / n);
            int g = (cumulativeH[Color.green(pixels[i])] * 255 / n);
            int b = (cumulativeH[Color.blue(pixels[i])] * 255 / n);
            pixels[i] = Color.rgb(r, g, b);
        }
        bmp.setPixels(pixels, 0, width, 0, 0, width, height);
    }*/

    public void equalizeColors(){

        Bitmap bmp = getBmp();
        //Get bmp size
        int width = bmp.getWidth();
        int height = bmp.getHeight();

        //Create renderscript
        RenderScript rs = RenderScript.create(getContext());

        //Create allocation from Bitmap
        Allocation allocationA = Allocation.createFromBitmap(rs, bmp);

        //Create allocation with same type
        Allocation allocationB = Allocation.createTyped(rs, allocationA.getType());

        //Create script from rs file.
        ScriptC_histo histEqScript = new ScriptC_histo(rs);

        //Set size in script
        histEqScript.set_size(width*height);

        //Call the first kernel.
        histEqScript.forEach_root(allocationA, allocationB);

        //Call the rs method to compute the remap array
        histEqScript.invoke_createRemapArray();

        //Call the second kernel
        histEqScript.forEach_remaptoRGB(allocationB, allocationA);

        //Copy script result into bitmap
        allocationA.copyTo(bmp);

        //Destroy everything to free memory
        allocationA.destroy();
        allocationB.destroy();
        histEqScript.destroy();
        rs.destroy();
    }


    //TODO Value between -128 and 128
    /**
     * This method change the contrast of the image according to the parameter.
     * @param value the amount of contrast to add.
     */
    public void contrastChange (float value) {
        Bitmap bmp = this.getBmp();
        int[] pixels = new int[width * height];
        bmp.getPixels(pixels, 0, width, 0, 0, width, height);
        float k = 259*(value+255)/255/(259-value);
        int Red, Green, Blue;
        for (int i = 0; i < height*width; i++) {
            Red = Math.max(Math.min((int) (k * (Color.red(pixels[i])-128) + 128), 255),0);
            Green = Math.max(Math.min((int) (k * (Color.green(pixels[i])-128) + 128), 255),0);
            Blue = Math.max(Math.min((int) (k * (Color.blue(pixels[i])-128) + 128), 255),0);
            pixels[i] = Color.rgb(Red, Green, Blue);
        }
        bmp.setPixels(pixels, 0, width,  0, 0, width, height);
    }

    //TODO Value between -128 and 128
    /**
     * This method change the warmth of the image by changing the contrast of red and blue
     * symmetrically.
     * @param value the amount of contrast to apply.
     */
    public void warmthChange (float value) {
        Bitmap bmp = this.getBmp();
        int[] pixels = new int[width * height];
        bmp.getPixels(pixels, 0, width, 0, 0, width, height);
        int Red, Blue;
        float k = 258*(value+255)/255/(258-value);
        float v = 258*(-value+255)/255/(258+value);
        for (int i = 0; i < height*width; i++) {
            Red = Math.max(Math.min((int) (v * (Color.red(pixels[i])-128) + 128), 255),0);
            Blue = Math.max(Math.min((int) (k * (Color.blue(pixels[i])-128) + 128), 255),0);
            pixels[i] = Color.rgb(Red, Color.green(pixels[i]), Blue);
        }
        bmp.setPixels(pixels, 0, width,  0, 0, width, height);
    }

    /**
     * This method will change the image surprisingly.
     * @param value the amount of surprise wanted.
     */
    public void magicWand (int value) {
        Bitmap bmp = this.getBmp();
        int[] pixels = new int[width * height];
        bmp.getPixels(pixels, 0, width, 0, 0, width, height);
        int Red, Blue;
        for (int i = 0; i < height*width; i++) {
            Blue = Math.max(0,Color.blue(pixels[i])*(100+Math.min(value, 0)))/100;
            Red = Math.max(0,Color.red(pixels[i])*(100-Math.max(value, 0))/100);
            pixels[i] = Color.rgb(Red, Color.green(pixels[i]), Blue);
        }
        bmp.setPixels(pixels, 0, width,  0, 0, width, height);
    }
}
