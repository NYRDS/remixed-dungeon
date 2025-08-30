package com.nyrds.platform.gfx;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Gdx2DPixmap;

import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * HTML version of BitmapData
 */
public class BitmapData {
    public Gdx2DPixmap bmp;
    public Texture texture;
    public Pixmap pixmap;
    public int width;
    public int height;
    
    public BitmapData(Gdx2DPixmap bmp) {
        this.bmp = bmp;
        this.width = bmp.getWidth();
        this.height = bmp.getHeight();
    }
    
    public BitmapData(Pixmap pixmap) {
        this.pixmap = pixmap;
        this.width = pixmap.getWidth();
        this.height = pixmap.getHeight();
        this.texture = new Texture(pixmap);
    }
    
    public BitmapData(Texture texture) {
        this.texture = texture;
        this.width = texture.getWidth();
        this.height = texture.getHeight();
    }
    
    public BitmapData(int w, int h) {
        this.pixmap = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        this.width = w;
        this.height = h;
    }
    
    public static BitmapData createBitmap(int w, int h) {
        return new BitmapData(w, h);
    }
    
    public int getWidth() {
        return width;
    }
    
    public int getHeight() {
        return height;
    }
    
    public boolean isEmptyPixel(int x, int y) {
        // Simple implementation for HTML version
        return false;
    }
    
    public void eraseColor(int color) {
        // Simple implementation for HTML version
        if (pixmap != null) {
            pixmap.setColor(color);
            pixmap.fill();
        }
    }
    
    public int getPixel(int x, int y) {
        // Simple implementation for HTML version
        if (pixmap != null) {
            return pixmap.getPixel(x, y);
        }
        return 0;
    }
    
    public void makeCircleMask(int radius, int c1, int c2, int c3) {
        // Simple implementation for HTML version
    }
    
    public void makeHalo(int radius, int c1, int c2) {
        // Simple implementation for HTML version
    }
    
    public void dispose() {
        if (bmp != null) {
            bmp.dispose();
        }
        if (pixmap != null) {
            pixmap.dispose();
        }
        if (texture != null) {
            texture.dispose();
        }
    }
}