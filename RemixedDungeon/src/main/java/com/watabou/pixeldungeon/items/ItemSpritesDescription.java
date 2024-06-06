package com.watabou.pixeldungeon.items;

import com.nyrds.platform.game.Game;
import com.nyrds.util.JsonHelper;
import com.nyrds.util.ModdingMode;

import org.json.JSONObject;

import org.apache.commons.collections4.map.HashedMap;
import java.util.Iterator;
import java.util.Map;

public class ItemSpritesDescription {
	private static final String SPRITES_DESC_ITEMS_JSON = "spritesDesc/items.json";

	static private final Map<String, ItemSpritesDescription> m_descMap = new HashedMap<>();

	private final String imageFile;
	private final int imageIndex;
	private final boolean fliesStraight;
	private final boolean fliesFastRotating;

	private ItemSpritesDescription(String imageFile, int imageIndex, boolean fliesStraight, boolean fliesFastRotating) {
		this.imageFile = imageFile;
		this.imageIndex = imageIndex;
		this.fliesStraight = fliesStraight;
		this.fliesFastRotating = fliesFastRotating;
	}

	static public String getImageFile(Item item) {
		ItemSpritesDescription entry = m_descMap.get(item.getEntityKind());
		if (entry != null) {
			return entry.imageFile;
		}
		return null;
	}

	static public Integer getImageIndex(Item item) {
		ItemSpritesDescription entry = m_descMap.get(item.getEntityKind());
		if (entry != null) {
			return entry.imageIndex;
		}
		return null;
	}

	static public boolean isFliesStraight(Item item) {
		ItemSpritesDescription entry = m_descMap.get(item.getEntityKind());
		if (entry != null) {
			return entry.fliesStraight;
		}
		return false;
	}

	static public boolean isFliesFastRotating(Item item) {
		ItemSpritesDescription entry = m_descMap.get(item.getEntityKind());
		if (entry != null) {
			return entry.fliesFastRotating;
		}
		return false;
	}

	static {
		if (ModdingMode.isResourceExist(SPRITES_DESC_ITEMS_JSON)) {
			JSONObject itemsDesc = JsonHelper.readJsonFromAsset(SPRITES_DESC_ITEMS_JSON);

			Iterator<?> keys = itemsDesc.keys();

			while (keys.hasNext()) {
				String key = (String) keys.next();
				try {
					JSONObject itemDesc = itemsDesc.getJSONObject(key);

					m_descMap.put(key,
							new ItemSpritesDescription(itemDesc.optString("file", "items.png"),
									itemDesc.optInt("index", 0), 
									itemDesc.optBoolean("fliesStraight", false),
									itemDesc.optBoolean("fliesFastRotating", false)));
				} catch (Exception e) {
					Game.toast("malformed desc (%s) for %s ignored", itemsDesc.toString(), key);
				}

			}
		}
	}
}