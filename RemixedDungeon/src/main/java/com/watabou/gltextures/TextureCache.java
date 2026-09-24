package com.watabou.gltextures;

import com.nyrds.platform.EventCollector;
import com.nyrds.platform.gfx.BitmapData;
import com.nyrds.platform.gl.Texture;
import com.nyrds.util.ModdingMode;
import com.watabou.noosa.TextureFilm;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import lombok.Synchronized;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TextureCache {

	private static final Map<Object, SmartTexture> all = new HashMap<>();
	// weak keys: object-keyed films (bitmaps/textures as keys) would otherwise pin
	// the whole SmartTexture->BitmapData chain forever, string keys stay while in use
	private static final Map<Object, TextureFilm> allFilm = new WeakHashMap<>();

	public static SmartTexture createSolid(int color) {
		String key = "1x1:" + color;

		if (all.containsKey(key)) {
			return all.get(key);
		} else {

			BitmapData bmp = BitmapData.createBitmap(1, 1);
			bmp.eraseColor(color);
			SmartTexture tx = new SmartTexture(bmp);
			all.put(key, tx);

			return tx;
		}
	}

	@Synchronized
	public static void add(Object key, SmartTexture tx) {
		all.put(key, tx);
	}

	@Synchronized
	@Nullable
	public static SmartTexture rawget(@NotNull Object src) {
		return all.get(src);
	}

	@Nullable
	public static SmartTexture getOrCreate(@NotNull Object src, SmartTextureFactory factory) {
		SmartTexture ret = rawget(src);
		if(ret!=null) {
			return ret;
		}

		ret = factory.create();
		add(src, ret);

		return ret;
	}

	@Synchronized
	public static SmartTexture get(@NotNull Object src) {
		SmartTexture tx = rawget(src);
		if (tx!=null) {
			return tx;
		}

		if (src instanceof SmartTexture) {
			return (SmartTexture) src;
		}

		if (src instanceof BitmapData) {
			tx = new SmartTexture((BitmapData) src);
			all.put(src, tx);
			return tx;
		}

		if(src instanceof String) {
			String strSrc = (String) src;
			strSrc = strSrc.strip();

			if(strSrc.isEmpty()) {
				EventCollector.logException(new Exception(), "Empty texture name");
				return createSolid(0);
			}

			BitmapData bmp = ModdingMode.getBitmapData(strSrc);
			tx = new SmartTexture(bmp);
		}

		all.put(src, tx);
		return tx;
	}

	@Synchronized
	@NotNull
	public static TextureFilm getFilm(@NotNull Object key, int w, int h) {
		if (allFilm.containsKey(key)) {
			return allFilm.get(key);
		} else {
			TextureFilm film = new TextureFilm(get(key), w, h);
			allFilm.put(key, film);
			return film;
		}
	}

	// debug: size of `all` at the most recent clear(); visible via lua_eval
	private static int lastClearSize = -1;
	private static int lastClearLiveIds = -1;
	private static int maxClearSize = -1;
	private static int clearsTotal = 0;

	public static int debugLastClearSize() {
		return lastClearSize;
	}

	public static int debugLastClearLiveIds() {
		return lastClearLiveIds;
	}

	public static int debugMaxClearSize() {
		return maxClearSize;
	}

	public static int debugClearsTotal() {
		return clearsTotal;
	}

	@Synchronized
	public static void clear() {
		Texture.noteCacheClear();
		clearsTotal++;
		lastClearSize = all.size();
		if (all.size() > maxClearSize) {
			maxClearSize = all.size();
		}
		int liveIds = 0;
		for (SmartTexture txt : all.values()) {
			if (txt.debugHasLiveGlId()) {
				liveIds++;
			}
			txt.delete();
			// terminal drop of the cache reference - free the upload pixmap of
			// any texture that was never bound (dispose-on-bind never ran)
			txt.releaseBitmapData();
		}
		lastClearLiveIds = liveIds;
		all.clear();
		allFilm.clear();
	}

	@Synchronized
	public static boolean contains(Object key) {
		return all.containsKey(key);
	}

	public interface SmartTextureFactory {
		SmartTexture create();
	}

	@Synchronized
	public static Object getKey(SmartTexture value) {
		for (val entry : all.entrySet()) {
			if(entry.getValue().equals(value)) {
				return entry.getKey();
			}
		}
		return null;
	}

}
