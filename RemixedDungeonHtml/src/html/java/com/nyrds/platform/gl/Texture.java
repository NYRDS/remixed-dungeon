package com.nyrds.platform.gl;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.nyrds.platform.gfx.BitmapData;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

/**
 * HTML version of Texture
 */
public class Texture {
    
    public static final int NEAREST = Gdx.gl20.GL_NEAREST;
    public static final int LINEAR = Gdx.gl20.GL_LINEAR;
    public static final int REPEAT = Gdx.gl20.GL_REPEAT;
    public static final int MIRROR = Gdx.gl20.GL_MIRRORED_REPEAT;
    public static final int CLAMP = Gdx.gl20.GL_CLAMP_TO_EDGE;

    protected int id = -1; // -1 indicates that the texture has not been generated yet

    static private final int[] bound = new int[32];
    static private int active = 0;

    protected Pixmap pixmap;
    private boolean recode;
    private int minFilter = LINEAR;
    private int maxFilter = LINEAR;
    private int wrapS = CLAMP;
    private int wrapT = CLAMP;
    private int[] pixels;
    protected int width;
    protected int height;
    private byte[] bytePixels;
    private boolean dataDirty = false;

    // Static flag to control whether bitmap data should be disposed after upload
    private static boolean autoDisposeBitmapData = true;

    public Texture() {
        // Texture generation is deferred until bind() is called
    }

    public Texture(String internalPath) {
        this.pixmap = new Pixmap(Gdx.files.internal(internalPath));
        this.dataDirty = true;
    }
    
    public Texture(Pixmap pixmap) {
        this.pixmap = pixmap;
        this.dataDirty = true;
    }
    
    public Texture(Pixmap pixmap, boolean useMipMaps) {
        this(pixmap);
        // MipMaps are handled by LibGDX
    }

    public static void activate(int index) {
        active = index;
        Gdx.gl20.glActiveTexture(Gdx.gl20.GL_TEXTURE0 + index);
    }

    public void bind() {
        if (id == -1) {
            id = Gdx.gl20.glGenTexture();
            if (id == 0) {
                throw new AssertionError();
            }
        }

        if (bound[active] != id) {
            Gdx.gl20.glBindTexture(Gdx.gl20.GL_TEXTURE_2D, id);
            //Gl.glCheck(); // Skip this for now, we'll add it later if needed
            bound[active] = id;

            // Apply stored parameters and upload pixel data if necessary
            applyParameters();
            uploadPixelData();
        }
    }

    private void applyParameters() {
        Gdx.gl20.glTexParameterf(Gdx.gl20.GL_TEXTURE_2D, Gdx.gl20.GL_TEXTURE_MIN_FILTER, minFilter);
        Gdx.gl20.glTexParameterf(Gdx.gl20.GL_TEXTURE_2D, Gdx.gl20.GL_TEXTURE_MAG_FILTER, maxFilter);
        Gdx.gl20.glTexParameterf(Gdx.gl20.GL_TEXTURE_2D, Gdx.gl20.GL_TEXTURE_WRAP_S, wrapS);
        Gdx.gl20.glTexParameterf(Gdx.gl20.GL_TEXTURE_2D, Gdx.gl20.GL_TEXTURE_WRAP_T, wrapT);
        //Gl.glCheck(); // Skip this for now
    }

    private void uploadPixelData() {
        if (dataDirty) {
            if (pixmap != null) {
                int w = pixmap.getWidth();
                int h = pixmap.getHeight();

                // For now, we'll just upload the pixmap directly
                Gdx.gl20.glTexImage2D(
                    Gdx.gl20.GL_TEXTURE_2D,
                    0,
                    pixmap.getGLInternalFormat(),
                    w,
                    h,
                    0,
                    pixmap.getGLFormat(),
                    pixmap.getGLType(),
                    pixmap.getPixels());

                if (autoDisposeBitmapData) {
                    pixmap.dispose(); // Dispose of the pixmap after uploading if needed
                }
                pixmap = null;
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

        Gdx.gl20.glTexImage2D(
                Gdx.gl20.GL_TEXTURE_2D,
                0,
                Gdx.gl20.GL_RGBA,
                w,
                h,
                0,
                Gdx.gl20.GL_RGBA,
                Gdx.gl20.GL_UNSIGNED_BYTE,
                imageBuffer);
        //Gl.glCheck(); // Skip this for now
    }

    private void uploadBytes(byte[] pixels, int w, int h) {
        ByteBuffer imageBuffer = ByteBuffer
                .allocateDirect(w * h)
                .order(ByteOrder.nativeOrder());
        imageBuffer.put(pixels);
        imageBuffer.position(0);

        Gdx.gl20.glPixelStorei(Gdx.gl20.GL_UNPACK_ALIGNMENT, 1);

        Gdx.gl20.glTexImage2D(
                Gdx.gl20.GL_TEXTURE_2D,
                0,
                Gdx.gl20.GL_ALPHA,
                w,
                h,
                0,
                Gdx.gl20.GL_ALPHA,
                Gdx.gl20.GL_UNSIGNED_BYTE,
                imageBuffer);
        //Gl.glCheck(); // Skip this for now
    }

    static public void unbind() {
        bound[active] = -1;
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
            Gdx.gl20.glDeleteTexture(id);
            id = -1;
            dataDirty = true; // Mark data as dirty to regenerate on next bind()
        }
    }

    public void pixels(int w, int h, int[] pixels) {
        this.width = w;
        this.height = h;
        this.pixels = pixels;
        this.bytePixels = null; // Clear byte pixel data
        this.pixmap = null; // Clear pixmap
        this.dataDirty = true;
    }

    public void pixels(int w, int h, byte[] pixels) {
        this.width = w;
        this.height = h;
        this.bytePixels = pixels;
        this.pixels = null; // Clear pixel data
        this.pixmap = null; // Clear pixmap
        this.dataDirty = true;
    }

    /**
     * Get the bitmap data associated with this texture
     * @return The bitmap data, or null if it's not available
     */
    public BitmapData getBitmapData() {
        if (this.pixmap != null) {
            return new BitmapData(this.pixmap);
        }
        return null;
    }

    /**
     * Get the width of this texture
     * @return The width of the texture
     */
    public int getWidth() {
        return this.width;
    }

    /**
     * Get the height of this texture
     * @return The height of the texture
     */
    public int getHeight() {
        return this.height;
    }

    public void bitmap(BitmapData bitmap) {
        this.pixmap = bitmap.getPixmap();
        this.dataDirty = true;
    }

    /**
     * Set whether bitmap data should be automatically disposed after being uploaded as a texture
     * @param autoDispose True to dispose after upload (default), false to preserve bitmap data
     */
    public static void setAutoDisposeBitmapData(boolean autoDispose) {
        autoDisposeBitmapData = autoDispose;
    }

    /**
     * Get whether bitmap data is automatically disposed after being uploaded as a texture
     * @return True if bitmap data is disposed after upload, false otherwise
     */
    public static boolean getAutoDisposeBitmapData() {
        return autoDisposeBitmapData;
    }
}