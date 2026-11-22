package com.nyrds.platform.gl;

import com.badlogic.gdx.Gdx;
import com.nyrds.platform.EventCollector;
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

	// live GL id -> dims|createdAt|bind-site, insertion(=age)-ordered; leakProbe()
	// dumps the oldest live ids, which is where a missed delete() shows up first
	private static final java.util.Map<Integer, String> liveIds = new java.util.LinkedHashMap<>();

	// id -> owner alive? evictDeadOwnerIds cleans entries whose owner is garbage;
	// leakProbe()/textureHealth() read it to name the owner of a broken GL id
	private static final java.util.Map<Integer, java.lang.ref.WeakReference<Texture>> owners =
			new java.util.LinkedHashMap<>();

	// epoch = number of TextureCache.clear() calls; bind() stamps it on the
	// owner at every call, so a lagging bindEpoch means "not drawn since"
	private static volatile int cacheClears = 0;

	public static void noteCacheClear() {
		cacheClears++;
		evictDeadOwnerIds();
	}

	// clean up GL ids whose owner object is already garbage: nobody can re-bind
	// them, so deleting is safe. Alive owners are NEVER touched here - deleting
	// a live owner's id behind its back left bind() stuck on a dead name (pixel
	// data is already released at first upload, so regeneration uploads nothing)
	// and the texture rendered blank forever (bd 3tf option C). Ids of alive
	// owners are freed by finalize() via the orphan drain when the owner dies.
	private static void evictDeadOwnerIds() {
		java.util.Iterator<java.util.Map.Entry<Integer, String>> it = liveIds.entrySet().iterator();
		while (it.hasNext()) {
			java.util.Map.Entry<Integer, String> e = it.next();
			java.lang.ref.WeakReference<Texture> ownerRef = owners.get(e.getKey());
			if (ownerRef != null && ownerRef.get() != null) {
				continue; // alive owner: may still draw, never delete behind its back
			}
			int deadId = e.getKey();
			owners.remove(deadId);
			it.remove();
			Gdx.gl20.glDeleteTexture(deadId);
			delCount++;
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
			owners.remove(orphan);
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

	public int debugGlId() {
		return id;
	}

	public static boolean debugGlIsTexture(int glId) {
		return Gdx.gl20.glIsTexture(glId);
	}

	// debug helper: delete a GL name behind our back (simulates EGL context loss
	// or driver-side deletion) - drives the bad-bind report path in tests
	public static void debugGlDeleteName(int glId) {
		Gdx.gl20.glDeleteTexture(glId);
	}

	// latched after the first dead-name bind report: the event goes out once per
	// process and the per-bind glIsTexture guard stops checking
	private static volatile boolean badBindReported = false;

	public static boolean badBindReported() {
		return badBindReported;
	}

	// latched after the first self-heal from a dead GL name (bd 3tf option D):
	// healing itself keeps working for every texture, only the event is once
	private static volatile boolean healReported = false;

	public static boolean healReported() {
		return healReported;
	}

	// self-heal hooks: SmartTexture overrides them when a heal source is set -
	// the base class never heals
	protected boolean canHeal() {
		return false;
	}

	protected BitmapData healPixels() {
		return null;
	}

	// called from bind() when a stale id is about to be bound and the driver no
	// longer knows it - this bind renders black. The exception's stack is the
	// draw path that hit it; textureHealth() in the message tells isolated dead
	// name (singular bug) apart from everything-dead (EGL context loss).
	private void reportBadBind() {
		badBindReported = true;
		try {
			String health = textureHealth();
			if (health.length() > 600) {
				health = health.substring(0, 600);
			}
			EventCollector.logException(new Exception("Bad texture bind: GL name " + id
					+ " unknown to the driver (deleted behind our back or EGL context loss), "
					+ "owner=" + getClass().getSimpleName() + " " + width + "x" + height
					+ ", " + glStats() + ", " + health), "texture");
		} catch (Throwable t) {
			EventCollector.logException(t, "texture bad-bind report failed");
		}
	}

	// on-device blank-texture diagnostic: two ways a live-on-our-side texture can
	// render black - a GL name the driver no longer knows (glIsTexture false,
	// deleted behind our back / context loss) and a driver-valid name with no
	// pixel content behind it (never re-uploaded after regeneration). GL-thread only.
	public static String textureHealth() {
		int dead = 0;
		int incomplete = 0;
		StringBuilder samples = new StringBuilder();
		for (java.util.Map.Entry<Integer, java.util.Map.Entry<Texture, String>> e : liveIdsWithOwners()) {
			int glId = e.getKey();
			Texture owner = e.getValue().getKey();
			String meta = e.getValue().getValue();
			boolean nameDead = !Gdx.gl20.glIsTexture(glId);
			boolean noContent = owner != null && !owner.hasPixels;
			if (!nameDead && !noContent) {
				continue;
			}
			if (nameDead) {
				dead++;
			}
			if (noContent) {
				incomplete++;
			}
			if (dead + incomplete <= 8) {
				samples.append("\n").append(glId).append(": ")
						.append(nameDead ? "DEAD-NAME " : "").append(noContent ? "NO-PIXELS " : "")
						.append(owner == null ? "DEAD-owner" : owner.getClass().getSimpleName())
						.append(' ').append(meta, 0, Math.min(meta.length(), 160));
			}
		}
		return "live=" + liveIds.size() + " deadNames=" + dead + " incomplete=" + incomplete + ";" + samples;
	}

	private static Iterable<java.util.Map.Entry<Integer, java.util.Map.Entry<Texture, String>>> liveIdsWithOwners() {
		java.util.List<java.util.Map.Entry<Integer, java.util.Map.Entry<Texture, String>>> out =
				new java.util.ArrayList<>();
		for (java.util.Map.Entry<Integer, String> e : liveIds.entrySet()) {
			java.lang.ref.WeakReference<Texture> ownerRef = owners.get(e.getKey());
			Texture owner = ownerRef == null ? null : ownerRef.get();
			out.add(new java.util.AbstractMap.SimpleEntry<>(e.getKey(),
					new java.util.AbstractMap.SimpleEntry<>(owner, e.getValue())));
		}
		return out;
	}

	public static String leakProbe() {
		java.util.Map<String, Integer> bySite = new java.util.LinkedHashMap<>();
		int deadOwners = 0;
		int idleOwners = 0;
		for (java.util.Map.Entry<Integer, String> e : liveIds.entrySet()) {
			String entry = e.getValue();
			int sep = entry.indexOf('|');
			String dims = entry.substring(0, sep);
			int sep2 = entry.indexOf('|', sep + 1);
			String createdAt = entry.substring(sep + 1, sep2);
			String bindstack = entry.substring(sep2 + 1);
			java.lang.ref.WeakReference<Texture> ownerRef = owners.get(e.getKey());
			Texture owner = ownerRef == null ? null : ownerRef.get();
			String kind;
			if (owner == null) {
				deadOwners++;
				kind = "DEAD created-by: " + createdAt;
			} else if (cacheClears - owner.bindEpoch >= 3) {
				// alive but not drawn for 3+ clears: transiently retained, its id
				// is freed by the orphan drain when the owner dies - NOT deleted
				// here, deleting behind an alive owner blanks the texture
				idleOwners++;
				kind = "IDLE-OWNER " + owner.getClass().getSimpleName() + " created-by: " + createdAt;
			} else {
				continue; // healthy: drawn recently
			}
			String key = kind + " " + dims + " @" + bindstack.substring(0, Math.min(bindstack.length(), 120));
			Integer c = bySite.get(key);
			bySite.put(key, c == null ? 1 : c + 1);
		}
		StringBuilder sb = new StringBuilder("live=").append(liveIds.size())
				.append(" deadOwner=").append(deadOwners)
				.append(" idleOwner=").append(idleOwners).append(';');
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

	// cacheClears value at the last bind() - tells leakProbe whether an alive
	// owner's id is still being drawn or has gone idle (transiently retained)
	private int bindEpoch;

	// true once this texture's current GL name has actually been bound - glIsTexture
	// reports false for a name reserved by glGenTextures but never bound yet, so the
	// bad-bind guard may only run after the first successful bind of this id
	private boolean everBound;

	// true once the current GL name has pixel content behind it - false means the
	// name is driver-valid but empty: samples read black (textureHealth reports it)
	private boolean hasPixels;

	// false after a regeneration that had no pixel source yet: the GL name is
	// valid but empty - draws sample black until the owner re-supplies
	public boolean hasPixels() {
		return hasPixels;
	}

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
		bindEpoch = cacheClears;
		if (id != -1 && everBound && !Gdx.gl20.glIsTexture(id)) {
			// dead GL name (context loss or deleted behind our back): with a heal
			// source, regenerate + re-decode instead of rendering black (bd 3tf
			// option D); otherwise report the first bad bind (bd k1l0)
			if (canHeal()) {
				if (!healReported) {
					healReported = true;
					EventCollector.logException(new Exception("Texture self-healed from dead GL name "
							+ id + " " + width + "x" + height + ", " + glStats()), "texture");
				}
				delete(); // bookkeeping + id=-1 + dataDirty=true; gen below regenerates, upload re-decodes
			} else if (!badBindReported) {
				reportBadBind();
			}
		}
		if (id == -1) {
			id = Gdx.gl20.glGenTexture();
			if (id == 0) {
				throw new AssertionError();
			}
			genCount++;
			// a reserved name glIsTexture's as false until its first bind
			everBound = false;
			hasPixels = false;
			StackTraceElement[] st = new Throwable().getStackTrace();
			StringBuilder sb = new StringBuilder();
			for (int i = 1; i < Math.min(st.length, 11); i++) {
				sb.append(st[i].getClassName()).append('.').append(st[i].getMethodName())
				  .append(':').append(st[i].getLineNumber()).append(" <- ");
			}
			liveIds.put(id, width + "x" + height + "|" + createdAt + "|" + sb);
			owners.put(id, new java.lang.ref.WeakReference<>(this));
		}

		if (bound[active] != id || !everBound) {
			// !everBound: this id was just (re)generated - a regen can reuse the
			// name currently recorded in bound[], and the reserved name's GL
			// object is empty until it is actually bound and re-uploaded
			// (self-heal re-decode)
			Gdx.gl20.glBindTexture(Gdx.gl20.GL_TEXTURE_2D, id);
			Gl.glCheck();
			bound[active] = id;

			// Apply stored parameters and upload pixel data if necessary
			applyParameters();
			uploadPixelData();
		}
		everBound = true;
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
			if (bitmapData == null && pixels == null && bytePixels == null) {
				// self-heal: all upload sources consumed or released - re-decode
				// the pixels from the heal source instead of uploading nothing
				// (which would render an incomplete, black texture)
				bitmapData = healPixels();
			}
			boolean uploaded = false;
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
				uploaded = true;
			} else if (pixels != null) {
				uploadPixels(pixels, width, height);
				pixels = null; // Clear the pixel data after uploading
				uploaded = true;
			} else if (bytePixels != null) {
				uploadBytes(bytePixels, width, height);
				bytePixels = null; // Clear the byte pixel data after uploading
				uploaded = true;
			}
			// false = this GL name has no content behind it: samples read black
			hasPixels = uploaded;
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
			owners.remove(id);
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