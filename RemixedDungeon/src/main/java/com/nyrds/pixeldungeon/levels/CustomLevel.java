package com.nyrds.pixeldungeon.levels;

import com.nyrds.lua.LuaEngine;
import com.nyrds.util.JsonHelper;
import com.nyrds.util.ModdingMode;
import com.watabou.pixeldungeon.levels.CommonLevel;
import com.watabou.utils.Bundle;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;

import clone.org.json.JSONException;
import clone.org.json.JSONObject;

/**
 * Created by mike on 13.11.2016.
 */

public abstract class CustomLevel extends CommonLevel {

	@NotNull
	protected JSONObject mLevelDesc = new JSONObject();

	@Nullable
	protected String     mDescFile;

	private final String DESC_FILE = "descFile";

	protected void readDescFile(String descFile) {
		if(descFile.endsWith(".json")) {
			mLevelDesc = JsonHelper.readJsonFromAsset(descFile);
			return;
		}

		if(descFile.endsWith(".lua")) {
			LuaEngine.getEngine().runScriptFile(descFile);
			String desc = LuaEngine.getEngine().call("getJson").tojstring();
			try {
				mLevelDesc = JsonHelper.readJsonFromStream(new ByteArrayInputStream(desc.getBytes()));
			} catch (JSONException e) {
				throw ModdingMode.modException(e);
			}
		}
	}

	@Override
	public boolean getProperty(String key, boolean defVal) {
		return mLevelDesc.optBoolean(key, defVal);
	}

	@Override
	public float getProperty(String key, float defVal) {
		return (float) mLevelDesc.optDouble(key, defVal);
	}

	@Override
	public String getProperty(String key, String defVal) {
		return mLevelDesc.optString(key, defVal);
	}

	@Override
	public String tilesTex() {
		return getProperty("tiles", "tiles0.png");
	}

	@Override
	public String tilesTexEx() {
		return getProperty("tiles_x", null);
	}

	@Override
	public String waterTex() {
		return getProperty("water", "water0.png");
	}

	@Override
	public boolean isBossLevel() {
		return getProperty("boss_level",super.isBossLevel());
	}

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		if(mDescFile!=null) {
			bundle.put(DESC_FILE, mDescFile);
		}
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		if(bundle.contains(DESC_FILE)) {
			mDescFile = bundle.getString(DESC_FILE);
			readDescFile(mDescFile);
		}
	}
}
