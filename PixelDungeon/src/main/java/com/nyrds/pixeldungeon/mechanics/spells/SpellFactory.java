package com.nyrds.pixeldungeon.mechanics.spells;

import com.nyrds.android.util.TrackedRuntimeException;
import com.nyrds.pixeldungeon.ml.EventCollector;
import com.watabou.noosa.Game;

import java.util.HashMap;
import java.util.Map;


public class SpellFactory {

	static private Map<String, Class<? extends Spell>> mSpellsList = new HashMap<>();
	static private Map<Class<? extends Spell>,String>  mNamesList = new HashMap<>();
	static {
		initSpellsMap();
	}

	private static void registerSpellClass(Class<? extends Spell> spellClass) {
		mSpellsList.put(spellClass.getSimpleName(), spellClass);
		mNamesList.put(spellClass,spellClass.getSimpleName());
	}

	private static void initSpellsMap() {
		registerSpellClass(SummonDeathling.class);
	}

	public static boolean isValidSpellClass(String spellClass) {
		return mSpellsList.containsKey(spellClass);
	}

	public static Spell spellByName(String selectedSpellClass) {
		try {
			return spellsClassByName(selectedSpellClass).newInstance();
		} catch (InstantiationException e) {
			throw new TrackedRuntimeException("", e);
		} catch (IllegalAccessException e) {
			throw new TrackedRuntimeException("", e);
		}
	}

	private static Class<? extends Spell> spellsClassByName(String selectedSpellClass) {
		Class<? extends Spell> spellClass = mSpellsList.get(selectedSpellClass);
		if (spellClass != null) {
			return spellClass;
		} else {
			Game.toast("Unknown spell: [%s], using 'Summon Wisp' instead", selectedSpellClass);
			return SummonDeathling.class;
		}
	}

	public static String spellNameByClass(Class<? extends Spell> clazz) {
		String ret = mNamesList.get(clazz);
		if(ret==null) {
			EventCollector.logEvent("Unregistered entry",clazz.getCanonicalName());
			ret = "SummonDeathling";
		}
		return ret;
	}

//	public static Item createSpellFromDesc(JSONObject spellDesc) throws IllegalAccessException, InstantiationException, JSONException {
//		return spell;
//	}
}
