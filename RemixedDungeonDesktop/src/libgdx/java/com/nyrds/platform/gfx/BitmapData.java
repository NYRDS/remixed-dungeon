package com.nyrds.platform.gfx;

import static com.badlogic.gdx.graphics.g2d.Gdx2DPixmap.GDX2D_FORMAT_RGBA8888;

import com.badlogic.gdx.graphics.g2d.Gdx2DPixmap;

import java.io.InputStream;

public class BitmapData {

    Gdx2DPixmap bitmap;

    public BitmapData(int w, int h) {
        bitmap = Gdx2DPixmap.newPixmap(w,h,GDX2D_FORMAT_RGBA8888);
    }

    public BitmapData(InputStream inputStream) {
        bitmap = Gdx2DPixmap.newPixmap(inputStream,GDX2D_FORMAT_RGBA8888);
    }

    public static BitmapData createBitmap(int w, int h) {
        return new BitmapData(w,h);
    }

    public static BitmapData decodeStream(InputStream inputStream) {
        return new BitmapData(inputStream);
    }

    public int getWidth() {
        return bitmap.getWidth();
    }

    public int getHeight() {
        return bitmap.getHeight();
    }

    public void getAllPixels(int[] pixels) {
        bitmap.getPixels().asIntBuffer().get(pixels);
    }

    public int getPixel(int x, int y) {
        return bitmap.getPixel(x,y);
    }

    private int color(int color) {
        final int as = 0;
        final int rs = 8;
        final int gs = 16;
        final int bs = 24;

        int a = (color & (0xFF << as)) >> as;
        int r = (color & (0xFF << rs)) >> rs;
        int g = (color & (0xFF << gs)) >> gs;
        int b = (color & (0xFF << bs)) >> bs;
        return (a << 24) + (r << 16) + (g << 8) + (b);
    }

    public void eraseColor(int color) {
        bitmap.clear(color(color));
    }

    public void setPixel(int x, int y, int color) {
        bitmap.setPixel(x,y, color(color));
    }
}
