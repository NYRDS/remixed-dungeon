/*
 * Copyright (C) 2012-2014  Oleg Dolya
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.watabou.gltextures;

import com.nyrds.platform.game.RemixedDungeon;
import com.nyrds.platform.gfx.BitmapData;
import com.nyrds.platform.gl.Texture;
import com.nyrds.util.ModError;
import com.nyrds.util.ModdingMode;
import com.watabou.noosa.TextureFilm;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

import lombok.SneakyThrows;
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
		} else if (src instanceof SmartTexture) {
			return (SmartTexture) src;
		} else {
			SmartTexture tx = new SmartTexture(getBitmapData(src));
			all.put(src, tx);
			return tx;
		}
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

	@SneakyThrows
	private static @NotNull BitmapData getBitmapData(Object src) {
		if (src instanceof String) {
			String resName = (String) src;

			BitmapData modAsset = BitmapData.decodeStream(ModdingMode.getInputStream(resName));

			if(modAsset.bmp==null) {
				throw new ModError("Bad bitmap: "+ resName);
			}

			if(ModdingMode.sizeAgnosticFiles.contains(resName)) {
				return modAsset;
			}

			if(ModdingMode.isAssetExist(resName)) {
				BitmapData baseAsset = BitmapData.decodeStream(ModdingMode.getInputStreamBuiltIn(resName));

				if(baseAsset.bmp==null) {
					throw new ModError("Bad builtin bitmap: "+ resName);
				}

				if (modAsset.getHeight() * modAsset.getWidth() < baseAsset.getWidth() * baseAsset.getHeight()) {
					RemixedDungeon.toast("%s image in %s smaller than in Remixed, using base version", resName, ModdingMode.activeMod());
					return baseAsset;
				}
			}

			return modAsset;
		} else if (src instanceof BitmapData) {
			return (BitmapData) src;
		}

		throw new ModError("Bad resource source for Bitmap "+ src.getClass().getName());
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
