package com.nyrds.pixeldungeon.levels;

import com.nyrds.android.util.JsonHelper;
import com.watabou.pixeldungeon.levels.CommonLevel;
import com.watabou.utils.Bundle;

import org.json.JSONObject;

/**
 * Created by mike on 13.11.2016.
 */

public abstract class CustomLevel extends CommonLevel {

	protected JSONObject mLevelDesc;
	protected String     mDescFile;

	protected final String descFileKey = "descFile";

	protected void readDescFile(String descFile) {
		mLevelDesc = JsonHelper.readJsonFromAsset(descFile);
	}

	@Override
	public String tilesTex() {
		return mLevelDesc.optString("tiles", "tiles0.png");
	}

	public String tilesTexEx() {
		return mLevelDesc.optString("tiles_x", null);
	}

	public String waterTex() {
		return mLevelDesc.optString("water", "water0.png");
	}

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(descFileKey, mDescFile);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		mDescFile = bundle.getString(descFileKey);
		readDescFile(mDescFile);
	}
}
