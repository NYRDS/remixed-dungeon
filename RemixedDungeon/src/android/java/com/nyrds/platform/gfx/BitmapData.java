package com.nyrds.platform.gfx;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;

import com.nyrds.LuaInterface;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@LuaInterface
public class BitmapData {
    public Bitmap bmp;

    BitmapData(Bitmap bmp) {
        this.bmp = bmp;
    }

    @LuaInterface
    public BitmapData(int width, int height) {
        try {
            System.out.println("BitmapData constructor called with width=" + width + ", height=" + height);
            this.bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            System.out.println("BitmapData constructor completed, bmp=" + (this.bmp != null ? "created" : "null"));
        } catch (Exception e) {
            System.out.println("BitmapData constructor exception: " + e.getMessage());
            e.printStackTrace();
            this.bmp = null;
        }
    }

    @LuaInterface
    public static BitmapData decodeStream(InputStream inputStream) {
        try {
            return new BitmapData(BitmapFactory.decodeStream(inputStream));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @LuaInterface
    public static BitmapData createBitmap(int width, int height) {
        try {
            System.out.println("BitmapData.createBitmap called with width=" + width + ", height=" + height);
            BitmapData result = new BitmapData(Bitmap.createBitmap(width,height, Bitmap.Config.ARGB_8888));
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
        try {
            return new BitmapData(Bitmap.createBitmap(width,height, Bitmap.Config.ARGB_4444));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
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
    public void getPixels(int[] pixels, int i, int w, int i1, int i2, int w1, int h) {
        if (bmp != null) {
            bmp.getPixels(pixels, i, w, i1, i2, w1, h);
        }
    }

    @LuaInterface
    public void getAllPixels(int[] pixels) {
        if (bmp != null) {
            bmp.getPixels(pixels, 0, bmp.getWidth(), 0, 0, bmp.getWidth(), bmp.getHeight());
        }
    }

    @LuaInterface
    public int getPixel(int i, int i1) {
        if (bmp != null) {
            return bmp.getPixel(i,i1);
        }
        return 0;
    }

    @LuaInterface
    public void eraseColor(int color) {
        if (bmp != null) {
            bmp.eraseColor(color);
        }
    }

    @LuaInterface
    public void setPixel(int i, int i1, int color) {
        if (bmp != null) {
            bmp.setPixel(i,i1,color);
        }
    }

    /**
     * Fill the entire bitmap with a solid color.
     * 
     * @param color The color to fill the bitmap with, in ARGB format (0xAARRGGBB)
     */
    @LuaInterface
    public void clear(int color) {
        if (bmp != null) {
            bmp.eraseColor(color);
        }
    }

    @LuaInterface
    public void makeHalo(int radius, int c1, int c2) {
        Canvas canvas = new Canvas( bmp );
        Paint paint = new Paint();
        paint.setColor( c2 );
        canvas.drawCircle( radius, radius, radius * 0.75f, paint );
        paint.setColor( c1 );
        canvas.drawCircle( radius, radius, radius, paint );
    }

    @LuaInterface
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

    @LuaInterface
    public boolean isEmptyPixel(int x, int y) {
        return (getPixel (x,y) & 0xff000000) == 0;
    }

    /**
     * Convert ARGB color format to the platform-specific format.
     * On Android, colors are already in ARGB format, so no conversion is needed.
     * 
     * Note: When calling from Lua, be aware that Lua uses double-precision floating-point
     * numbers as its only numeric type. For color values, this generally works fine for
     * most colors, but very large values might lose precision.
     * 
     * @param color Color in ARGB format (0xAARRGGBB)
     * @return Color in platform-specific format
     */
    @LuaInterface
    public static int color(int color) {
        // Android uses ARGB format directly, so no conversion needed
        // Format: 0xAARRGGBB where AA is alpha, RR is red, GG is green, BB is blue
        return color;
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
    public void savePng(String path) {
        try {
            if (bmp != null) {
                // If path is relative, save to app's external files directory
                File file;
                if (path.startsWith("/")) {
                    file = new File(path);
                } else {
                    // Try to get external storage directory
                    File externalDir = new File("/sdcard/Android/data/com.nyrds.pixeldungeon.ml/files/");
                    externalDir.mkdirs();
                    file = new File(externalDir, path);
                }
                
                file.getParentFile().mkdirs();
                OutputStream out = new FileOutputStream(file);
                bmp.compress(Bitmap.CompressFormat.PNG, 100, out);
                out.close();
                
                // Log the full path where the file was saved
                System.out.println("Bitmap saved to: " + file.getAbsolutePath());
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @LuaInterface
    public void drawLine(int startX, int startY, int endX, int endY, int color) {
        try {
            if (bmp != null) {
                Canvas canvas = new Canvas(bmp);
                Paint paint = new Paint();
                paint.setColor(color(color));
                paint.setStrokeWidth(1);
                canvas.drawLine(startX, startY, endX, endY, paint);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @LuaInterface
    public void drawRect(int left, int top, int right, int bottom, int color) {
        try {
            if (bmp != null) {
                Canvas canvas = new Canvas(bmp);
                Paint paint = new Paint();
                paint.setColor(color(color));
                paint.setStyle(Paint.Style.STROKE);
                canvas.drawRect(left, top, right, bottom, paint);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @LuaInterface
    public void fillRect(int left, int top, int right, int bottom, int color) {
        try {
            if (bmp != null) {
                Canvas canvas = new Canvas(bmp);
                Paint paint = new Paint();
                paint.setColor(color(color));
                paint.setStyle(Paint.Style.FILL);
                canvas.drawRect(left, top, right, bottom, paint);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @LuaInterface
    public void drawCircle(int centerX, int centerY, int radius, int color) {
        try {
            if (bmp != null && radius > 0) {
                Canvas canvas = new Canvas(bmp);
                Paint paint = new Paint();
                paint.setColor(color(color));
                paint.setStyle(Paint.Style.STROKE);
                canvas.drawCircle(centerX, centerY, radius, paint);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @LuaInterface
    public void fillCircle(int centerX, int centerY, int radius, int color) {
        try {
            if (bmp != null && radius > 0) {
                Canvas canvas = new Canvas(bmp);
                Paint paint = new Paint();
                paint.setColor(color(color));
                paint.setStyle(Paint.Style.FILL);
                canvas.drawCircle(centerX, centerY, radius, paint);
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
            if (bmp != null && srcBitmap != null && srcBitmap.bmp != null) {
                Canvas canvas = new Canvas(bmp);
                canvas.drawBitmap(srcBitmap.bmp, 
                                 new android.graphics.Rect(srcX, srcY, srcX + width, srcY + height),
                                 new android.graphics.Rect(dstX, dstY, dstX + width, dstY + height),
                                 null);
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
            if (bmp != null && srcBitmap != null && srcBitmap.bmp != null) {
                Canvas canvas = new Canvas(bmp);
                Paint paint = new Paint();
                paint.setAlpha(255); // Full alpha for the paint
                canvas.drawBitmap(srcBitmap.bmp, 
                                 new android.graphics.Rect(srcX, srcY, srcX + width, srcY + height),
                                 new android.graphics.Rect(dstX, dstY, dstX + width, dstY + height),
                                 paint);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @LuaInterface
    public void dispose() {
        // Note: We're not disposing of the bitmap here to avoid native crashes
        // The garbage collector will handle cleanup
        // If bmp != null, bmp.recycle() could cause issues in some cases
        bmp = null;
    }
}
