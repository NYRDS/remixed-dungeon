package com.nyrds.platform.gfx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.g2d.Gdx2DPixmap;
import com.nyrds.LuaInterface;

import java.io.InputStream;

import static com.badlogic.gdx.graphics.g2d.Gdx2DPixmap.GDX2D_BLEND_NONE;
import static com.badlogic.gdx.graphics.g2d.Gdx2DPixmap.GDX2D_FORMAT_RGBA8888;

@LuaInterface
public class BitmapData {

    public Gdx2DPixmap bmp;

    public BitmapData(Gdx2DPixmap bmp) {
        this.bmp = bmp;
    }

    @LuaInterface
    public BitmapData(int w, int h) {
        try {
            System.out.println("BitmapData constructor called with w=" + w + ", h=" + h);
            bmp = Gdx2DPixmap.newPixmap(w,h,GDX2D_FORMAT_RGBA8888);
            System.out.println("BitmapData constructor completed, bmp=" + (bmp != null ? "created" : "null"));
        } catch (Exception e) {
            System.out.println("BitmapData constructor exception: " + e.getMessage());
            e.printStackTrace();
            bmp = null;
        }
    }

    @LuaInterface
    public BitmapData(int w, int h, int pixelFormat) {
        try {
            bmp = Gdx2DPixmap.newPixmap(w,h,pixelFormat);
        } catch (Exception e) {
            e.printStackTrace();
            bmp = null;
        }
    }

    @LuaInterface
    public BitmapData(InputStream inputStream) {
        try {
            bmp = Gdx2DPixmap.newPixmap(inputStream,GDX2D_FORMAT_RGBA8888);
        } catch (Exception e) {
            e.printStackTrace();
            bmp = null;
        }
    }

    @LuaInterface
    public static BitmapData createBitmap(int w, int h) {
        try {
            System.out.println("BitmapData.createBitmap called with w=" + w + ", h=" + h);
            BitmapData result = new BitmapData(w,h);
            System.out.println("BitmapData.createBitmap completed, result=" + (result != null ? "created" : "null"));
            return result;
        } catch (Exception e) {
            System.out.println("BitmapData.createBitmap exception: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @LuaInterface
    public static BitmapData createBitmap4(int width, int height) {
        return new BitmapData(width, height, GDX2D_FORMAT_RGBA8888); // Using RGBA8888 as equivalent
    }

    @LuaInterface
    public static BitmapData decodeStream(InputStream inputStream) {
        return new BitmapData(inputStream);
    }

    @LuaInterface
    public int getWidth() {
        if (bmp != null) {
            return bmp.getWidth();
        }
        return 0;
    }

    @LuaInterface
    public int getHeight() {
        if (bmp != null) {
            return bmp.getHeight();
        }
        return 0;
    }

    @LuaInterface
    public void getAllPixels(int[] pixels) {
        if (bmp != null) {
            bmp.getPixels().asIntBuffer().get(pixels);
        }
    }

    @LuaInterface
    public int getPixel(int x, int y) {
        if (bmp != null) {
            return bmp.getPixel(x,y);
        }
        return 0;
    }

    @LuaInterface
    public boolean isEmptyPixel(int x, int y) {
        if (bmp != null) {
            return (getPixel (x,y) & 0xff) == 0;
        }
        return true;
    }

    /**
     * Fill the entire bitmap with a solid color.
     * 
     * @param color The color to fill the bitmap with, in ARGB format (0xAARRGGBB)
     */
    @LuaInterface
    public void clear(int color) {
        if (bmp != null) {
            bmp.clear(color(color));
        }
    }

    /**
     * Convert ARGB color format to the platform-specific format.
     * On desktop, colors need to be converted from ARGB to RGBA format.
     * 
     * @param color Color in ARGB format (0xAARRGGBB)
     * @return Color in RGBA format
     */
    @LuaInterface
    public static int color(int color) {
        int a = (color >> 24) & 0xFF; // Extract A
        int r = (color >> 16) & 0xFF; // Extract R
        int g = (color >> 8) & 0xFF;  // Extract G
        int b = color & 0xFF;         // Extract B
        int ret =  (r << 24) | (g << 16) | (b << 8) | a; // Combine as RGBA
        return ret;
    }

    /**
     * Alternative method for handling colors from Lua to avoid precision issues.
     * Instead of passing a single large integer, colors can be passed as separate
     * components (alpha, red, green, blue) each in the 0-255 range.
     * 
     * @param alpha Alpha component (0-255)
     * @param red Red component (0-255)
     * @param green Green component (0-255)
     * @param blue Blue component (0-255)
     * @return Color in ARGB format
     */
    @LuaInterface
    public static int colorFromComponents(int alpha, int red, int green, int blue) {
        return (alpha << 24) | (red << 16) | (green << 8) | blue;
    }

    @LuaInterface
    public void eraseColor(int color) {
        bmp.clear(color(color));
    }

    @LuaInterface
    public void setPixel(int x, int y, int color) {
        bmp.setPixel(x,y, color(color));
    }

    @LuaInterface
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

    @LuaInterface
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

    @LuaInterface
    public void save(String path) {
        try {
            if (bmp != null) {
                Pixmap pixmap = new Pixmap(bmp);
                // Log the path where the file will be saved
                System.out.println("Bitmap saving to: " + path);
                PixmapIO.writePNG(Gdx.files.local(path), pixmap);
                pixmap.dispose();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @LuaInterface
    public void savePng(String path) {
        save(path);
    }

    @LuaInterface
    public void drawLine(int startX, int startY, int endX, int endY, int color) {
        try {
            if (bmp != null) {
                bmp.drawLine(startX, startY, endX, endY, color(color));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @LuaInterface
    public void drawRect(int left, int top, int right, int bottom, int color) {
        try {
            if (bmp != null) {
                bmp.drawRect(left, top, right, bottom, color(color));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @LuaInterface
    public void fillRect(int left, int top, int right, int bottom, int color) {
        try {
            if (bmp != null) {
                bmp.fillRect(left, top, right, bottom, color(color));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @LuaInterface
    public void drawCircle(int centerX, int centerY, int radius, int color) {
        try {
            if (bmp != null && radius > 0) {
                bmp.drawCircle(centerX, centerY, radius, color(color));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @LuaInterface
    public void fillCircle(int centerX, int centerY, int radius, int color) {
        try {
            if (bmp != null && radius > 0) {
                bmp.fillCircle(centerX, centerY, radius, color(color));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Copy a rectangular region from another BitmapData to this one.
     * 
     * @param srcBitmap The source BitmapData to copy from
     * @param srcX The x-coordinate of the upper-left corner of the source rectangle
     * @param srcY The y-coordinate of the upper-left corner of the source rectangle
     * @param width The width of the rectangle to copy
     * @param height The height of the rectangle to copy
     * @param dstX The x-coordinate of the upper-left corner of the destination rectangle
     * @param dstY The y-coordinate of the upper-left corner of the destination rectangle
     */
    @LuaInterface
    public void copyRect(BitmapData srcBitmap, int srcX, int srcY, int width, int height, int dstX, int dstY) {
        try {
            // Validate parameters
            if (bmp == null || srcBitmap == null || srcBitmap.bmp == null) {
                return;
            }
            
            // Extract pixels from source bitmap
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    if (srcY + y < srcBitmap.bmp.getHeight() && srcX + x < srcBitmap.bmp.getWidth() &&
                        dstY + y < bmp.getHeight() && dstX + x < bmp.getWidth()) {
                        int pixel = srcBitmap.bmp.getPixel(srcX + x, srcY + y);
                        bmp.setPixel(dstX + x, dstY + y, pixel);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Copy a rectangular region from another BitmapData to this one, respecting source alpha.
     * This method blends the source pixels with the destination based on the source pixel's alpha channel.
     * 
     * @param srcBitmap The source BitmapData to copy from
     * @param srcX The x-coordinate of the upper-left corner of the source rectangle
     * @param srcY The y-coordinate of the upper-left corner of the source rectangle
     * @param width The width of the rectangle to copy
     * @param height The height of the rectangle to copy
     * @param dstX The x-coordinate of the upper-left corner of the destination rectangle
     * @param dstY The y-coordinate of the upper-left corner of the destination rectangle
     */
    @LuaInterface
    public void rectCopy(BitmapData srcBitmap, int srcX, int srcY, int width, int height, int dstX, int dstY) {
        try {
            // Validate parameters
            if (bmp == null || srcBitmap == null || srcBitmap.bmp == null) {
                return;
            }
            
            // Extract and blend pixels from source bitmap with alpha blending
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    if (srcY + y < srcBitmap.bmp.getHeight() && srcX + x < srcBitmap.bmp.getWidth() &&
                        dstY + y < bmp.getHeight() && dstX + x < bmp.getWidth()) {
                        int srcPixel = srcBitmap.bmp.getPixel(srcX + x, srcY + y);
                        int dstPixel = bmp.getPixel(dstX + x, dstY + y);
                        
                        // Extract components
                        int srcA = (srcPixel >> 24) & 0xFF;
                        int srcR = (srcPixel >> 16) & 0xFF;
                        int srcG = (srcPixel >> 8) & 0xFF;
                        int srcB = srcPixel & 0xFF;
                        
                        // If source pixel is fully transparent, skip
                        if (srcA == 0) {
                            continue;
                        }
                        
                        // If source pixel is fully opaque, just copy it
                        if (srcA == 255) {
                            bmp.setPixel(dstX + x, dstY + y, srcPixel);
                        } else {
                            // Alpha blending
                            int dstA = (dstPixel >> 24) & 0xFF;
                            int dstR = (dstPixel >> 16) & 0xFF;
                            int dstG = (dstPixel >> 8) & 0xFF;
                            int dstB = dstPixel & 0xFF;
                            
                            // Blend with source alpha
                            int blendR = (srcR * srcA + dstR * (255 - srcA)) / 255;
                            int blendG = (srcG * srcA + dstG * (255 - srcA)) / 255;
                            int blendB = (srcB * srcA + dstB * (255 - srcA)) / 255;
                            int blendA = srcA + (dstA * (255 - srcA)) / 255;
                            
                            int blendedPixel = (blendA << 24) | (blendR << 16) | (blendG << 8) | blendB;
                            bmp.setPixel(dstX + x, dstY + y, blendedPixel);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @LuaInterface
    public void dispose() {
        // Note: We're not disposing of the bitmap here to avoid native crashes
        // The garbage collector will handle cleanup
        // If bmp != null, bmp.dispose() would cause a double free error
        bmp = null;
    }
}
