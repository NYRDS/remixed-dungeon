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
package com.watabou.pixeldungeon;

import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;

import java.util.HashMap;

public final class ResultDescriptions {

	public enum Reason {
		MOB, BOSS, WAND, GLYPH, TRAP, BURNING, HUNGER, POISON, GAS, BLEEDING, OOZE, FALL, IMMURED,
		NECROTISM, UNKNOWN, WIN
	}

	private static final HashMap<Reason, Integer> descriptionsMap;

	static {
		descriptionsMap = new HashMap<>();

		// Mobs
		descriptionsMap.put(Reason.MOB, R.string.ResultDescriptions_Mob);
		descriptionsMap.put(Reason.BOSS, R.string.ResultDescriptions_Boss);

		// Items
		descriptionsMap.put(Reason.WAND, R.string.ResultDescriptions_Wand);
		descriptionsMap.put(Reason.GLYPH, R.string.ResultDescriptions_Glyph);

		// Dungeon features
		descriptionsMap.put(Reason.TRAP, R.string.ResultDescriptions_Trap);

		// Debuffs & blobs
		descriptionsMap.put(Reason.BURNING, R.string.ResultDescriptions_Burning);
		descriptionsMap.put(Reason.HUNGER, R.string.ResultDescriptions_Hunger);
		descriptionsMap.put(Reason.POISON, R.string.ResultDescriptions_Poison);
		descriptionsMap.put(Reason.GAS, R.string.ResultDescriptions_Gas);
		descriptionsMap.put(Reason.BLEEDING, R.string.ResultDescriptions_Bleeding);
		descriptionsMap.put(Reason.OOZE, R.string.ResultDescriptions_Ooze);
		descriptionsMap.put(Reason.FALL, R.string.ResultDescriptions_Fall);
		descriptionsMap.put(Reason.IMMURED, R.string.ResultDescriptions_Immured);
		descriptionsMap.put(Reason.NECROTISM, R.string.ResultDescriptions_Necrotism);

		// Win
		descriptionsMap.put(Reason.WIN, R.string.ResultDescriptions_Win);
	}

	// Private constructor to avoid instantiation
	private ResultDescriptions() throws Exception{
		throw new Exception("Trying to instantiate a utility class ResultDescription.");
	}

	public static String getDescription(Reason reason){
		// Strangely not in the map, probably added a reason to the enum and forgot to add it to the HashMap
		if(!descriptionsMap.containsKey(reason)){
			reason = Reason.UNKNOWN;	// This one is definitely in the map. Returning UNKNOWN.
		}

		return Game.getVar(descriptionsMap.get(reason));
	}
}
