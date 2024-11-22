package com.nyrds.platform.gfx;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.InputStream;

public class BitmapData {
    public Bitmap bmp;

    BitmapData(Bitmap bmp) {
        this.bmp = bmp;
    }

    public static BitmapData decodeStream(InputStream inputStream) {
        return new BitmapData(BitmapFactory.decodeStream(inputStream));
    }

    public static BitmapData createBitmap(int width, int height) {
        return new BitmapData(Bitmap.createBitmap(width,height, Bitmap.Config.ARGB_8888));
    }

    public int getWidth() {
        return bmp.getWidth();
    }

    public int getHeight() {
        return bmp.getHeight();
    }

    public void getPixels(int[] pixels, int i, int w, int i1, int i2, int w1, int h) {
        bmp.getPixels(pixels, i, w, i1, i2, w1, h);
    }

    public int getPixel(int i, int i1) {
        return bmp.getPixel(i,i1);
    }

    public void eraseColor(int color) {
        bmp.eraseColor(color);
    }

    public void setPixel(int i, int i1, int color) {
        bmp.setPixel(i,i1,color);
    }
}
