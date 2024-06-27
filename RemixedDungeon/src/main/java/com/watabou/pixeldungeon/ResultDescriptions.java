
package com.watabou.pixeldungeon;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;

import java.util.HashMap;

public final class ResultDescriptions {

	public enum Reason {
		MOB, BOSS, WAND, GLYPH, TRAP, BURNING, HUNGER, POISON, GAS, BLEEDING, OOZE, FALL, IMMURED,
		NECROTISM, NECROMANCY, UNKNOWN, WIN
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

		// Necromancy
		descriptionsMap.put(Reason.NECROMANCY, R.string.ResultDescriptions_Necromancy);

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

        return StringsManager.getVar(descriptionsMap.get(reason));
    }
}
