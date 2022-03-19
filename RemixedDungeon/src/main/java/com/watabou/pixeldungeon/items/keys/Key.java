/*
 * Pixel Dungeon
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
package com.watabou.pixeldungeon.items.keys;

import com.nyrds.Packable;
import com.nyrds.pixeldungeon.utils.DungeonGenerator;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.bags.Keyring;

import org.jetbrains.annotations.NotNull;

import clone.org.json.JSONException;
import clone.org.json.JSONObject;

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
