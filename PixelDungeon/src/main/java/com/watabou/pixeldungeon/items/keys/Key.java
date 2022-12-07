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

import android.support.annotation.NonNull;

import com.nyrds.retrodungeon.utils.DungeonGenerator;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.utils.Bundle;

import org.json.JSONException;
import org.json.JSONObject;

public class Key extends Item {

	public static final float TIME_TO_UNLOCK = 1f;

	public int depth;

	@NonNull
	public String levelId;
	
	public Key() {
		depth     = DungeonGenerator.getCurrentLevelDepth();
		levelId   = DungeonGenerator.getCurrentLevelId();
		stackable = false;
	}
	
	private static final String DEPTH = "depth";
	private static final String LEVELID = "levelId";
	
	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		//bundle.put( DEPTH,   depth );
		bundle.put( LEVELID, levelId);
	}
	
	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		levelId = bundle.optString(LEVELID, DungeonGenerator.UNKNOWN);
		depth = bundle.optInt( DEPTH,DungeonGenerator.getLevelDepth(levelId) );

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
		return depth + "*";
	}

	@Override
	public void fromJson(JSONObject itemDesc) throws JSONException {
		super.fromJson(itemDesc);
		levelId = itemDesc.optString("levelId",levelId);
		depth   = DungeonGenerator.getLevelDepth(levelId);
		depth   = itemDesc.optInt("depth",depth);
	}
}
