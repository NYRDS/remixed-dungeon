package com.nyrds.pixeldungeon.mobs.common;

import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.utils.Bundle;

import org.json.JSONException;
import org.json.JSONObject;

public class MultiKindMob extends Mob {
	
	private static final String KIND_TAG = "KIND";
	protected int kind = 0;
	
	@Override
	public int getKind() {
		return kind;
	}
	
	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(KIND_TAG, kind);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		kind = bundle.optInt(KIND_TAG, 0);
	}

	@Override
	public void fromJson(JSONObject mobDesc) throws JSONException, InstantiationException, IllegalAccessException {
		super.fromJson(mobDesc);
		//Yes it is illogical but "kind" already used for mob class
		kind = mobDesc.optInt("var",0);
	}
}
