package com.nyrds.platform.gfx;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Gdx2DPixmap;
import com.nyrds.LuaInterface;


/**
 * HTML version of BitmapData
 */
@LuaInterface
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
        // alpha byte of the RGBA8888 pixel, matches the desktop Gdx2DPixmap check
        return (getPixel(x, y) & 0xff) == 0;
    }

    // ARGB (0xAARRGGBB) -> RGBA8888 packed, same as the desktop helper
    private static int color(int color) {
        int a = (color >> 24) & 0xFF;
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;
        return (r << 24) | (g << 16) | (b << 8) | a;
    }
    
    public void eraseColor(int color) {
        if (pixmap != null) {
            pixmap.setColor(color(color));
            pixmap.fill();
        }
    }

    public void clear(int color) {
        if (pixmap != null) {
            pixmap.setColor(color(color));
            pixmap.fill();
        }
    }

    public void setPixel(int x, int y, int color) {
        if (pixmap != null) {
            pixmap.drawPixel(x, y, color(color));
        }
    }
    
    public Pixmap getPixmap() {
        return pixmap;
    }
    
    public int getPixel(int x, int y) {
        // Simple implementation for HTML version
        if (pixmap != null) {
            return pixmap.getPixel(x, y);
        }
        return 0;
    }
    
    public void makeCircleMask(int radius, int c1, int c2, int c3) {
        if (pixmap == null) {
            return;
        }
        // desktop gdx2d semantics: paint the concentric circles with blending
        // disabled - with the default SourceOver the transparent inner circle
        // would blend to a no-op and the mask would stay fully opaque
        pixmap.setBlending(Pixmap.Blending.None);
        pixmap.setColor(color(0xffffffff));
        pixmap.fill();

        int centerX = radius;
        int centerY = radius;
        pixmap.setColor(color(c3));
        pixmap.fillCircle(centerX, centerY, radius);
        pixmap.setColor(color(c2));
        pixmap.fillCircle(centerX, centerY, (int) (0.75f * radius));
        pixmap.setColor(color(c1));
        pixmap.fillCircle(centerX, centerY, (int) (0.5f * radius));
        pixmap.setBlending(Pixmap.Blending.SourceOver);
    }
    
    public void makeHalo(int radius, int c1, int c2) {
        if (pixmap == null) {
            return;
        }
        // desktop semantics: transparent base, opaque inner disc over a
        // translucent outer ring, blending disabled so colors replace
        pixmap.setBlending(Pixmap.Blending.None);
        pixmap.setColor(0);
        pixmap.fill();
        pixmap.setColor(color(c1));
        pixmap.fillCircle(radius, radius, radius);
        pixmap.setColor(color(c2));
        pixmap.fillCircle(radius, radius, (int) (0.75f * radius));
        pixmap.setBlending(Pixmap.Blending.SourceOver);
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