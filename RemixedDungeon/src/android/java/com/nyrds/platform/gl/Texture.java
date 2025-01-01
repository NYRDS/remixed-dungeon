package com.nyrds.platform.gl;

import android.opengl.GLES20;
import android.opengl.GLUtils;

import com.nyrds.platform.gfx.BitmapData;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

public class Texture {

    public static final int NEAREST = GLES20.GL_NEAREST;
    public static final int LINEAR = GLES20.GL_LINEAR;
    public static final int REPEAT = GLES20.GL_REPEAT;
    public static final int MIRROR = GLES20.GL_MIRRORED_REPEAT;
    public static final int CLAMP = GLES20.GL_CLAMP_TO_EDGE;

    protected int id = -1; // -1 indicates that the texture has not been generated yet

    private BitmapData bitmapData;
    private int minFilter = LINEAR;
    private int maxFilter = LINEAR;
    private int wrapS = CLAMP;
    private int wrapT = CLAMP;
    private int[] pixels;
    private int width;
    private int height;
    private byte[] bytePixels;
    private boolean dataDirty = false;

    public Texture() {
        // Texture generation is deferred until bind() is called
    }

    public static void activate(int index) {
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + index);
    }

    public void bind() {
        if (id == -1) {
            int[] ids = new int[1];
            GLES20.glGenTextures(1, ids, 0);
            id = ids[0];

            if (id == 0) {
                throw new AssertionError();
            }
        }

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, id);

        // Apply stored parameters and upload pixel data if necessary
        applyParameters();
        uploadPixelData();
    }

    private void applyParameters() {
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, minFilter);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, maxFilter);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, wrapS);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, wrapT);
    }

    private void uploadPixelData() {
        if (dataDirty) {
            if (bitmapData != null) {
                GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmapData.bmp, 0);
                bitmapData = null; // Clear the bitmap data after uploading
            } else if (pixels != null) {
                uploadPixels(pixels, width, height);
                pixels = null; // Clear the pixel data after uploading
            } else if (bytePixels != null) {
                uploadBytes(bytePixels, width, height);
                bytePixels = null; // Clear the byte pixel data after uploading
            }
            dataDirty = false;
        }
    }

    private void uploadPixels(int[] pixels, int w, int h) {
        IntBuffer imageBuffer = ByteBuffer
                .allocateDirect(w * h * 4)
                .order(ByteOrder.nativeOrder())
                .asIntBuffer();
        imageBuffer.put(pixels);
        imageBuffer.position(0);

        GLES20.glTexImage2D(
                GLES20.GL_TEXTURE_2D,
                0,
                GLES20.GL_RGBA,
                w,
                h,
                0,
                GLES20.GL_RGBA,
                GLES20.GL_UNSIGNED_BYTE,
                imageBuffer);
    }

    private void uploadBytes(byte[] pixels, int w, int h) {
        ByteBuffer imageBuffer = ByteBuffer
                .allocateDirect(w * h)
                .order(ByteOrder.nativeOrder());
        imageBuffer.put(pixels);
        imageBuffer.position(0);

        GLES20.glPixelStorei(GLES20.GL_UNPACK_ALIGNMENT, 1);

        GLES20.glTexImage2D(
                GLES20.GL_TEXTURE_2D,
                0,
                GLES20.GL_ALPHA,
                w,
                h,
                0,
                GLES20.GL_ALPHA,
                GLES20.GL_UNSIGNED_BYTE,
                imageBuffer);
    }

    public void filter(int minMode, int maxMode) {
        this.minFilter = minMode;
        this.maxFilter = maxMode;
    }

    public void wrap(int s, int t) {
        this.wrapS = s;
        this.wrapT = t;
    }

    public void delete() {
        if (id != -1) {
            GLES20.glDeleteTextures(1, new int[]{id}, 0);
            id = -1;
            dataDirty = true; // Mark data as dirty to regenerate on next bind()
        }
    }

    public void bitmap(BitmapData bitmap) {
        this.bitmapData = bitmap;
        this.dataDirty = true;
    }

    public void pixels(int w, int h, int[] pixels) {
        this.width = w;
        this.height = h;
        this.pixels = pixels;
        this.bytePixels = null; // Clear byte pixel data
        this.bitmapData = null; // Clear bitmap data
        this.dataDirty = true;
    }
}