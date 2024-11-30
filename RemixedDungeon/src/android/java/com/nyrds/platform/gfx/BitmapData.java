package com.nyrds.platform.gfx;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;

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

    public static BitmapData createBitmap4(int width, int height) {
        return new BitmapData(Bitmap.createBitmap(width,height, Bitmap.Config.ARGB_4444));
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

    public void makeHalo(int radius, int c1, int c2) {
        Canvas canvas = new Canvas( bmp );
        Paint paint = new Paint();
        paint.setColor( c2 );
        canvas.drawCircle( radius, radius, radius * 0.75f, paint );
        paint.setColor( c1 );
        canvas.drawCircle( radius, radius, radius, paint );
    }

    public void makeCircleMask(int radius, int c1, int c2, int c3) {
        Canvas canvas = new Canvas( bmp );
        Paint paint = new Paint();
        canvas.drawColor(Color.WHITE, PorterDuff.Mode.SRC);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
        paint.setColor( c3 );
        canvas.drawCircle( radius, radius, radius, paint );

        paint.setColor( c2 );
        canvas.drawCircle( radius, radius, radius*0.75f, paint );

        paint.setColor( c1 );
        canvas.drawCircle( radius, radius, radius*0.5f, paint );
    }

    public boolean isEmptyPixel(int x, int y) {
        return (getPixel (x,y) & 0xff000000) == 0;
    }
}
