package com.nyrds.platform.gl;

import com.badlogic.gdx.Gdx;
import com.nyrds.platform.gfx.BitmapData;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

public class Texture {

	public static final int NEAREST = Gdx.gl20.GL_NEAREST;
	public static final int LINEAR = Gdx.gl20.GL_LINEAR;
	public static final int REPEAT = Gdx.gl20.GL_REPEAT;
	public static final int MIRROR = Gdx.gl20.GL_MIRRORED_REPEAT;
	public static final int CLAMP = Gdx.gl20.GL_CLAMP_TO_EDGE;

	protected int id = -1; // -1 indicates that the texture has not been generated yet

	static private final int[] bound = new int[32];
	static private int active = 0;

	protected BitmapData bitmapData;
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

	// GL id lifecycle counters for leak debugging. Visible via the debug web
	// server: luajava.bindClass("com.nyrds.platform.gl.Texture"):glStats()
	// gen-del == live GL texture ids (grows forever => a delete() path is missed).
	private static volatile int genCount = 0;
	private static volatile int delCount = 0;

	// live GL id -> gen-site stack, insertion(=age)-ordered; leakProbe() dumps
	// the oldest live ids, which is where a missed delete() shows up first
	private static final java.util.Map<Integer, String> liveIds = new java.util.LinkedHashMap<>();

	// epoch = number of TextureCache.clear() calls; ids stamped with an older
	// epoch than the current one have survived a full cache clear => leaked
	private static volatile int cacheClears = 0;

	public static void noteCacheClear() {
		cacheClears++;
		evictIdleIds();
	}

	// textures not re-bound for 3 cache-clear epochs are idle orphans (owner
	// alive but no longer drawing them): delete their GL id. If the owner ever
	// draws again, bind() regenerates - correctness preserved.
	private static void evictIdleIds() {
		java.util.Iterator<java.util.Map.Entry<Integer, String>> it = liveIds.entrySet().iterator();
		while (it.hasNext()) {
			java.util.Map.Entry<Integer, String> e = it.next();
			int epoch = Integer.parseInt(e.getValue().substring(0, e.getValue().indexOf('|')));
			if (cacheClears - epoch >= 3) {
				Gdx.gl20.glDeleteTexture(e.getKey());
				delCount++;
				it.remove();
			}
		}
	}

	// GL ids of textures garbage-collected without delete(). TextureCache.clear
	// releases ownership of textures that live sprites may still re-bind; when
	// such a sprite later dies its texture is unreachable unmanaged, so nothing
	// would ever glDeleteTextures its id. The queue is drained on the GL thread
	// at the next bind() - a missed delete costs one frame, not forever.
	private static final java.util.Queue<Integer> orphanedIds = new java.util.concurrent.ConcurrentLinkedQueue<>();

	@Override
	protected void finalize() throws Throwable {
		if (id != -1) {
			orphanedIds.add(id);
			id = -1;
		}
		super.finalize();
	}

	private static void drainOrphanedIds() {
		Integer orphan;
		while ((orphan = orphanedIds.poll()) != null) {
			if (liveIds.remove(orphan) != null) {
				Gdx.gl20.glDeleteTexture(orphan);
				delCount++;
			}
		}
	}

	public static String glStats() {
		return "gen=" + genCount + " del=" + delCount + " live=" + (genCount - delCount);
	}

	public boolean debugHasLiveGlId() {
		return id != -1;
	}

	public static String leakProbe() {
	    java.util.Map<String, Integer> bySite = new java.util.LinkedHashMap<>();
	    int leaked = 0;
	    for (String entry : liveIds.values()) {
	        int sep = entry.indexOf('|');
	        int epoch = Integer.parseInt(entry.substring(0, sep));
	        if (cacheClears - epoch < 3) {
	            continue; // young enough to still be pending normal reuse
	        }
	        leaked++;
	        int sep2 = entry.indexOf('|', sep + 1);
	        String dims = entry.substring(sep + 1, sep2);
	        String site = dims + " @ " + entry.substring(sep2 + 1);
	        Integer c = bySite.get(site);
	        bySite.put(site, c == null ? 1 : c + 1);
	    }
	    StringBuilder sb = new StringBuilder("live=").append(liveIds.size())
	            .append(" leaked=").append(leaked).append(';');
	    int n = 0;
	    for (java.util.Map.Entry<String, Integer> e : bySite.entrySet()) {
	        if (n++ >= 12) break;
	        sb.append("\n").append(e.getValue()).append("x: ").append(e.getKey());
	    }
	    return sb.toString();
	}

	// creation-site stack of this texture: identifies who constructed a
	// texture whose GL id leaked (string survives the owner's GC)
	private final String createdAt = captureCreationSite();

	private static String captureCreationSite() {
		StackTraceElement[] st = new Throwable().getStackTrace();
		StringBuilder sb = new StringBuilder();
		for (int i = 2; i < Math.min(st.length, 8); i++) {
			sb.append(st[i].getClassName()).append('.').append(st[i].getMethodName())
					.append(':').append(st[i].getLineNumber()).append(" <- ");
		}
		return sb.toString();
	}

	public Texture() {
		// Texture generation is deferred until bind() is called
	}

	public static void activate(int index) {
		active = index;
		Gdx.gl20.glActiveTexture(Gdx.gl20.GL_TEXTURE0 + index);
	}

	public void bind() {
		drainOrphanedIds();
		if (id == -1) {
			id = Gdx.gl20.glGenTexture();
			if (id == 0) {
				throw new AssertionError();
			}
			genCount++;
			StackTraceElement[] st = new Throwable().getStackTrace();
			StringBuilder sb = new StringBuilder();
			for (int i = 1; i < Math.min(st.length, 11); i++) {
				sb.append(st[i].getClassName()).append('.').append(st[i].getMethodName())
				  .append(':').append(st[i].getLineNumber()).append(" <- ");
			}
			liveIds.put(id, cacheClears + "|" + width + "x" + height + "|" + sb);
		}

		if (bound[active] != id) {
			Gdx.gl20.glBindTexture(Gdx.gl20.GL_TEXTURE_2D, id);
			Gl.glCheck();
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
		Gl.glCheck();
	}

	private void uploadPixelData() {
		if (dataDirty) {
			if (bitmapData != null) {
				int w = bitmapData.getWidth();
				int h = bitmapData.getHeight();
				int[] pixels = new int[w * h];
				bitmapData.getAllPixels(pixels);
				if (autoDisposeBitmapData) {
					bitmapData.dispose();
				}

				final int as = 0;
				final int rs = 8;
				final int gs = 16;
				final int bs = 24;

				if (recode) {
					for (int i = 0; i < pixels.length; i++) {
						int color = pixels[i];
						int a = (color >> as) & 0xFF;
						int r = (color >> rs) & 0xFF;
						int g = (color >> gs) & 0xFF;
						int b = (color >> bs) & 0xFF;

						pixels[i] = (a << 24) | (r << 16) | (g << 8) | (b);
					}
				}

				uploadPixels(pixels, w, h);
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
		Gl.glCheck();
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
		Gl.glCheck();
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
			liveIds.remove(id);
			id = -1;
			delCount++;
			dataDirty = true; // Mark data as dirty to regenerate on next bind()
		}
	}

	/**
	 * Terminal release of the upload pixmap. Textures created but never bound
	 * keep their bitmap here forever otherwise - Gdx2DPixmap has no finalizer,
	 * so nothing would ever free the native memory. Call only when the texture
	 * is being discarded (e.g. TextureCache.clear), not on reversible delete().
	 */
	public void releaseBitmapData() {
		if (bitmapData != null) {
			if (autoDisposeBitmapData) {
				bitmapData.dispose();
			}
			bitmapData = null;
		}
	}

	public void bitmap(BitmapData bitmap) {
		this.bitmapData = bitmap;
		this.recode = true;
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

	public void pixels(int w, int h, int[] pixels) {
		this.width = w;
		this.height = h;
		this.pixels = pixels;
		this.bytePixels = null; // Clear byte pixel data
		this.bitmapData = null; // Clear bitmap data
		this.dataDirty = true;
	}

	public void pixels(int w, int h, byte[] pixels) {
		this.width = w;
		this.height = h;
		this.bytePixels = pixels;
		this.pixels = null; // Clear pixel data
		this.bitmapData = null; // Clear bitmap data
		this.dataDirty = true;
	}

	/**
	 * Get the bitmap data associated with this texture
	 * @return The bitmap data, or null if it's not available
	 */
	public BitmapData getBitmapData() {
		return this.bitmapData;
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
}