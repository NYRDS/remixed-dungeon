package com.nyrds.platform.gfx;

import com.nyrds.LuaInterface;
import com.nyrds.platform.EventCollector;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import javax.imageio.ImageIO;

// headless: pure-java bitmap over BufferedImage, pixels are ARGB-packed
// (desktop uses RGBA-packed Gdx2DPixmap; only matters for direct pixel readers,
// texture upload is a no-op here)
@LuaInterface
public class BitmapData {

    public BufferedImage bmp;

    public BitmapData(BufferedImage img) {
        bmp = img;
    }

    public BitmapData(int w, int h) {
        this(w, h, 0);
    }

    public BitmapData(int w, int h, int pixelFormat) {
        bmp = new BufferedImage(Math.max(1, w), Math.max(1, h), BufferedImage.TYPE_INT_ARGB);
    }

    public BitmapData(InputStream inputStream) {
        try {
            BufferedImage src = ImageIO.read(inputStream);
            if (src.getType() == BufferedImage.TYPE_INT_ARGB) {
                bmp = src;
            } else {
                bmp = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_ARGB);
                Graphics2D g = bmp.createGraphics();
                g.drawImage(src, 0, 0, null);
                g.dispose();
            }
        } catch (Exception e) {
            EventCollector.logException(e);
            bmp = null;
        }
    }

    @LuaInterface
    public static BitmapData createBitmap(int w, int h) {
        try {
            return new BitmapData(w, h);
        } catch (Exception e) {
            EventCollector.logException(e);
            return null;
        }
    }

    @LuaInterface
    public static BitmapData createBitmap4(int width, int height) {
        return new BitmapData(width, height, 0);
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
            bmp.getRGB(0, 0, bmp.getWidth(), bmp.getHeight(), pixels, 0, bmp.getWidth());
        }
    }

    @LuaInterface
    public int getPixel(int x, int y) {
        if (bmp != null && x >= 0 && y >= 0 && x < bmp.getWidth() && y < bmp.getHeight()) {
            return bmp.getRGB(x, y);
        }
        return 0;
    }

    @LuaInterface
    public boolean isEmptyPixel(int x, int y) {
        if (bmp != null) {
            return (getPixel(x, y) >>> 24) == 0;
        }
        return true;
    }

    @LuaInterface
    public void clear(int color) {
        fillRect(0, 0, getWidth(), getHeight(), color);
    }

    // headless: colors stay ARGB, no conversion needed
    @LuaInterface
    public static int color(int color) {
        return color;
    }

    @LuaInterface
    public static int colorFromComponents(int alpha, int red, int green, int blue) {
        return (alpha << 24) | (red << 16) | (green << 8) | blue;
    }

    @LuaInterface
    public void eraseColor(int color) {
        clear(color);
    }

    @LuaInterface
    public void setPixel(int x, int y, int color) {
        if (bmp != null && x >= 0 && y >= 0 && x < bmp.getWidth() && y < bmp.getHeight()) {
            bmp.setRGB(x, y, color);
        }
    }

    @LuaInterface
    public void makeHalo(int radius, int c1, int c2) {
        int centerX = radius;
        int centerY = radius;
        int innerRadius = (int) (0.75f * radius);
        int outerRadius = radius;

        for (int i = 0; i <= radius * 2; i++) {
            for (int j = 0; j <= radius * 2; j++) {
                float d = (float) Math.sqrt((i - centerX) * (i - centerX) + (j - centerY) * (j - centerY));
                float level = d > outerRadius ? 0 : d < innerRadius ? 1 : (outerRadius - d) / (outerRadius - innerRadius);
                int color = blendColors(c2, c1, level);
                setPixel(i, j, color);
            }
        }
    }

    @LuaInterface
    public void makeCircleMask(int radius, int c1, int c2, int c3) {
        int centerX = radius;
        int centerY = radius;
        int innerRadius = (int) (0.5f * radius);
        int middleRadius = (int) (0.75f * radius);
        int outerRadius = radius;

        for (int i = 0; i <= radius * 2; i++) {
            for (int j = 0; j <= radius * 2; j++) {
                float d = (float) Math.sqrt((i - centerX) * (i - centerX) + (j - centerY) * (j - centerY));
                float level;
                if (d > outerRadius) {
                    level = 0;
                } else if (d > middleRadius) {
                    level = (outerRadius - d) / (outerRadius - middleRadius);
                } else if (d > innerRadius) {
                    level = 1;
                } else {
                    level = d / innerRadius;
                }
                int color;
                if (d > middleRadius) {
                    color = blendColors(c3, c2, level);
                } else if (d > innerRadius) {
                    color = c2;
                } else {
                    color = blendColors(c2, c1, level);
                }
                setPixel(i, j, color);
            }
        }
    }

    private static int blendColors(int c1, int c2, float level) {
        int a1 = (c1 >> 24) & 0xFF, r1 = (c1 >> 16) & 0xFF, g1 = (c1 >> 8) & 0xFF, b1 = c1 & 0xFF;
        int a2 = (c2 >> 24) & 0xFF, r2 = (c2 >> 16) & 0xFF, g2 = (c2 >> 8) & 0xFF, b2 = c2 & 0xFF;
        int a = (int) (a1 * level + a2 * (1 - level));
        int r = (int) (r1 * level + r2 * (1 - level));
        int g = (int) (g1 * level + g2 * (1 - level));
        int b = (int) (b1 * level + b2 * (1 - level));
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    public void save(String path) {
        try {
            savePng(path);
        } catch (Exception e) {
            EventCollector.logException(e, path);
        }
    }

    public void savePng(String path) {
        try {
            if (bmp != null) {
                ImageIO.write(bmp, "png", new File(path));
            }
        } catch (Exception e) {
            EventCollector.logException(e, path);
        }
    }

    @LuaInterface
    public void drawLine(int startX, int startY, int endX, int endY, int color) {
        if (bmp == null) {
            return;
        }
        int dx = Math.abs(endX - startX), sx = startX < endX ? 1 : -1;
        int dy = -Math.abs(endY - startY), sy = startY < endY ? 1 : -1;
        int err = dx + dy;
        while (true) {
            setPixel(startX, startY, color);
            if (startX == endX && startY == endY) break;
            int e2 = 2 * err;
            if (e2 >= dy) {
                err += dy;
                startX += sx;
            }
            if (e2 <= dx) {
                err += dx;
                startY += sy;
            }
        }
    }

    @LuaInterface
    public void drawRect(int left, int top, int right, int bottom, int color) {
        drawLine(left, top, right, top, color);
        drawLine(right, top, right, bottom, color);
        drawLine(right, bottom, left, bottom, color);
        drawLine(left, bottom, left, top, color);
    }

    @LuaInterface
    public void fillRect(int left, int top, int right, int bottom, int color) {
        if (bmp == null) {
            return;
        }
        for (int y = Math.max(0, top); y < Math.min(bmp.getHeight(), bottom); y++) {
            for (int x = Math.max(0, left); x < Math.min(bmp.getWidth(), right); x++) {
                bmp.setRGB(x, y, color);
            }
        }
    }

    @LuaInterface
    public void drawCircle(int centerX, int centerY, int radius, int color) {
        for (int a = 0; a < 360; a += 2) {
            int x = centerX + (int) (radius * Math.cos(Math.toRadians(a)));
            int y = centerY + (int) (radius * Math.sin(Math.toRadians(a)));
            setPixel(x, y, color);
        }
    }

    @LuaInterface
    public void fillCircle(int centerX, int centerY, int radius, int color) {
        for (int y = -radius; y <= radius; y++) {
            for (int x = -radius; x <= radius; x++) {
                if (x * x + y * y <= radius * radius) {
                    setPixel(centerX + x, centerY + y, color);
                }
            }
        }
    }

    @LuaInterface
    public void copyRect(BitmapData srcBitmap, int srcX, int srcY, int width, int height, int dstX, int dstY) {
        if (bmp == null || srcBitmap == null || srcBitmap.bmp == null) {
            return;
        }
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (srcY + y < srcBitmap.bmp.getHeight() && srcX + x < srcBitmap.bmp.getWidth()
                        && dstY + y < bmp.getHeight() && dstX + x < bmp.getWidth()) {
                    int pixel = srcBitmap.bmp.getRGB(srcX + x, srcY + y);
                    bmp.setRGB(dstX + x, dstY + y, pixel);
                }
            }
        }
    }

    @LuaInterface
    public void rectCopy(BitmapData srcBitmap, int srcX, int srcY, int width, int height, int dstX, int dstY) {
        if (bmp == null || srcBitmap == null || srcBitmap.bmp == null) {
            return;
        }
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (srcY + y < srcBitmap.bmp.getHeight() && srcX + x < srcBitmap.bmp.getWidth()
                        && dstY + y < bmp.getHeight() && dstX + x < bmp.getWidth()) {
                    int pixel = srcBitmap.bmp.getRGB(srcX + x, srcY + y);
                    if ((pixel >>> 24) != 0) {
                        bmp.setRGB(dstX + x, dstY + y, pixel);
                    }
                }
            }
        }
    }

    public void dispose() {
        bmp = null;
    }
}
