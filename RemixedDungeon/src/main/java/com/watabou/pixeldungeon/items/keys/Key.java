
package com.watabou.pixeldungeon.items.keys;

import com.nyrds.Packable;
import com.nyrds.pixeldungeon.utils.DungeonGenerator;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.bags.Keyring;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

public class Key extends Item {

	public static final float TIME_TO_UNLOCK = 1f;

	@NotNull
	@Packable(defaultValue = "unknown")
	public String levelId;
	
	public Key() {
		levelId   = DungeonGenerator.getCurrentLevelId();
		stackable = false;
	}

	@Override
	public boolean isUpgradable() {
		return false;
	}
	
	@Override
	public boolean isIdentified() {
		return true;
	}
	
	@Override
	public String status() {
		return getDepth() + "*";
	}

	@Override
	public void fromJson(JSONObject itemDesc) throws JSONException {
		super.fromJson(itemDesc);
		levelId = itemDesc.optString("levelId",levelId);
	}

	public int getDepth() {
		return DungeonGenerator.getLevelDepth(levelId);
	}

	@Override
	public String bag() {
		return Keyring.class.getSimpleName();
	}
}
