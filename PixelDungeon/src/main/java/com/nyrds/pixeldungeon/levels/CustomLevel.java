package com.nyrds.pixeldungeon.levels;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.nyrds.android.util.JsonHelper;
import com.watabou.pixeldungeon.levels.CommonLevel;
import com.watabou.utils.Bundle;

import org.json.JSONObject;

/**
 * Created by mike on 13.11.2016.
 */

public abstract class CustomLevel extends CommonLevel {

	@NonNull
	protected JSONObject mLevelDesc = new JSONObject();

	@Nullable
	protected String     mDescFile;

	private final String DESC_FILE = "descFile";

	protected void readDescFile(String descFile) {
		mLevelDesc = JsonHelper.readJsonFromAsset(descFile);
	}

	@Override
	public String tilesTex() {
		return mLevelDesc.optString("tiles", "tiles0.png");
	}

	@Override
	public String tilesTexEx() {
		return mLevelDesc.optString("tiles_x", null);
	}

	@Override
	public String waterTex() {
		return mLevelDesc.optString("water", "water0.png");
	}

	@Override
	public boolean isBossLevel() {
		return mLevelDesc.optBoolean("boss_level",super.isBossLevel());
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
