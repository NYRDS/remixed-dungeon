package com.watabou.gltextures;

import com.nyrds.platform.gfx.BitmapData;
import com.nyrds.platform.gl.Texture;
import com.nyrds.util.ModdingMode;
import com.watabou.noosa.TextureFilm;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

import lombok.Synchronized;
import lombok.val;

public class TextureCache {

	private static final Map<Object, SmartTexture> all = new HashMap<>();
	private static final Map<Object, TextureFilm> allFilm = new HashMap<>();

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
		SmartTexture ret = rawget(src);
		if (ret!=null) {
			return ret;
		}

		if (src instanceof SmartTexture) {
			return (SmartTexture) src;
		}

		if (src instanceof BitmapData) {
			SmartTexture tx = new SmartTexture((BitmapData) src);
			all.put(src, tx);
			return tx;
		}

		BitmapData bmp = ModdingMode.getBitmapData((String) src);
		SmartTexture tx = new SmartTexture(bmp);
		all.put(src, tx);
		//bmp.dispose();
		return tx;

	}

	@Synchronized
	public static TextureFilm getFilm(@NotNull Object key, int w, int h) {
		if (allFilm.containsKey(key)) {
			return allFilm.get(key);
		} else {
			TextureFilm film = new TextureFilm(get(key), w, h);
			allFilm.put(key, film);
			return film;
		}
	}

	@Synchronized
	public static void clear() {
		for (Texture txt : all.values()) {
			txt.delete();
		}
		all.clear();
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
