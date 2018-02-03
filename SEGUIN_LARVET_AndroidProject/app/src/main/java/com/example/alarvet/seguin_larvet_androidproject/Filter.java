package com.example.alarvet.seguin_larvet_androidproject;

import android.graphics.Bitmap;

/**
 * Created by Aziza on 03/02/2018.
 */

public abstract class Filter {
    private final Bitmap bmp;

    public Filter(Bitmap bmp) {
        this.bmp = bmp;
    }

    public  Bitmap getBmp() {
        return bmp;
    }
}
