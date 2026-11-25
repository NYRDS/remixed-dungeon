
package com.watabou.pixeldungeon.items.keys;

import com.nyrds.Packable;
import com.nyrds.pixeldungeon.mechanics.LuaScript;
import com.nyrds.pixeldungeon.utils.DungeonGenerator;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.items.Item;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

public class Key extends Item {

	public static final float TIME_TO_UNLOCK = 1f;

	// HUD counter for the depth's iron keys (was IronKey.countIronKeys)
	public static int curDepthQuantity = 0;

	public static void countIronKeys() {
		if (Dungeon.isLoading()) {
			return;
		}

		curDepthQuantity = 0;

		var levelId = DungeonGenerator.getCurrentLevelId();

		for (Item item : Dungeon.hero.getBelongings().backpack) {
			if (item instanceof Key && "IronKey".equals(item.getEntityKind()) && ((Key) item).levelId.equals(levelId)) {
				curDepthQuantity++;
			}
		}
	}

	@NotNull
	@Packable(defaultValue = "unknown")
	public String levelId;
	
	public Key() {
		levelId   = DungeonGenerator.getCurrentLevelId();
		stackable = false;
	}

	// data kinds (Iron/Golden/Skeleton): image from scripts/items/<Kind>.lua
	public Key(@NotNull String kind) {
		this();
		this.keyKind = kind;
		LuaScript kindScript = new LuaScript("scripts/items/" + kind, "scripts/lib/emptykind", this);
		int imageOverride = kindScript.runOptional("image", -1);
		if (imageOverride >= 0) {
			setImage(imageOverride);
		}
	}

	private String keyKind;

	@Override
	public String getEntityKind() {
		if (keyKind != null) {
			return keyKind;
		}
		return super.getEntityKind();
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
		return "Keyring";
	}
}
