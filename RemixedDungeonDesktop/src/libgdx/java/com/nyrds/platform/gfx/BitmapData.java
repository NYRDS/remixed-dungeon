package com.nyrds.platform.gfx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.g2d.Gdx2DPixmap;

import java.io.InputStream;

import static com.badlogic.gdx.graphics.g2d.Gdx2DPixmap.GDX2D_BLEND_NONE;
import static com.badlogic.gdx.graphics.g2d.Gdx2DPixmap.GDX2D_FORMAT_RGBA8888;

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

    public static int color(int color) {
        int a = (color >> 24) & 0xFF; // Extract A
        int r = (color >> 16) & 0xFF; // Extract R
        int g = (color >> 8) & 0xFF;  // Extract G
        int b = color & 0xFF;         // Extract B
        int ret =  (r << 24) | (g << 16) | (b << 8) | a; // Combine as RGBA
        return ret;
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

        bmp.clear(0);
        bmp.setBlend(GDX2D_BLEND_NONE);

        bmp.fillCircle(centerX, centerY, outerRadius, color(c1));
        bmp.fillCircle(centerX, centerY, innerRadius, color(c2));
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
