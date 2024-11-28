package com.nyrds.platform.gfx;

import static com.badlogic.gdx.graphics.g2d.Gdx2DPixmap.GDX2D_BLEND_NONE;
import static com.badlogic.gdx.graphics.g2d.Gdx2DPixmap.GDX2D_BLEND_SRC_OVER;
import static com.badlogic.gdx.graphics.g2d.Gdx2DPixmap.GDX2D_FORMAT_RGBA8888;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.g2d.Gdx2DPixmap;

import java.io.InputStream;

public class BitmapData {

    public Gdx2DPixmap bmp;

    public BitmapData(Gdx2DPixmap bmp) {
        this.bmp = bmp;
    }

    public BitmapData(int w, int h) {
        bmp = Gdx2DPixmap.newPixmap(w,h,GDX2D_FORMAT_RGBA8888);
    }

    public BitmapData(int w, int h, int pixelFormat) {
        bmp = Gdx2DPixmap.newPixmap(w,h,pixelFormat);
    }

    public BitmapData(InputStream inputStream) {
        bmp = Gdx2DPixmap.newPixmap(inputStream,GDX2D_FORMAT_RGBA8888);
    }

    public static BitmapData createBitmap(int w, int h) {
        return new BitmapData(w,h);
    }

    public static BitmapData decodeStream(InputStream inputStream) {
        return new BitmapData(inputStream);
    }

    public int getWidth() {
        return bmp.getWidth();
    }

    public int getHeight() {
        return bmp.getHeight();
    }

    public void getAllPixels(int[] pixels) {
        bmp.getPixels().asIntBuffer().get(pixels);
    }

    public int getPixel(int x, int y) {
        return bmp.getPixel(x,y);
    }

    public boolean isEmptyPixel(int x, int y) {
        return (getPixel (x,y) & 0xff) == 0;
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
        bmp.clear(color(color));
    }

    public void setPixel(int x, int y, int color) {
        bmp.setPixel(x,y, color(color));
    }

    public void makeHalo(int radius, int c1, int c2) {
        int centerX = radius;
        int centerY = radius;
        int innerRadius = (int) (0.75f * radius);
        int outerRadius = radius;

        bmp.clear(0xffffffff);
        bmp.setBlend(GDX2D_BLEND_NONE);

        bmp.fillCircle(centerX, centerY, outerRadius, color(c2));
        bmp.fillCircle(centerX, centerY, innerRadius, color(c1));
    }

    public void makeCircleMask(int radius, int c1, int c2, int c3) {
        int centerX = radius;
        int centerY = radius;
        int innerRadius = (int) (0.5f * radius);
        int middleRadius = (int) (0.75f * radius);
        int outerRadius = radius;

        bmp.clear(0xffffffff);
        bmp.setBlend(GDX2D_BLEND_NONE);

        bmp.fillCircle(centerX, centerY, outerRadius, color(c3));
        bmp.fillCircle(centerX, centerY, middleRadius, color(c2));
        bmp.fillCircle(centerX, centerY, innerRadius, color(c1));
    }

    public void save(String path) {
    	Pixmap pixmap = new Pixmap(bmp);
    	PixmapIO.writePNG(Gdx.files.local(path), pixmap);
        pixmap.dispose();
    }

    public void dispose() {
    	bmp.dispose();
    }
}
