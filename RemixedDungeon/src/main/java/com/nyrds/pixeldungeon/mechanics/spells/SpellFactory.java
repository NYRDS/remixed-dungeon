package com.nyrds.pixeldungeon.mechanics.spells;

import com.nyrds.android.util.TrackedRuntimeException;
import com.nyrds.pixeldungeon.mechanics.LuaScript;
import com.watabou.utils.Random;

import org.jetbrains.annotations.NotNull;
import org.luaj.vm2.LuaTable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SpellFactory {

	static private Map<String, Class<? extends Spell>> mSpellsList = new HashMap<>();

	static private LuaScript script = new LuaScript("scripts/spells/SpellsByAffinity", null);

	static private Map<String,ArrayList<String>> mSpellsByAffinity = new HashMap<>();

	static {
		initSpellsMap();
		script.run("loadSpells",null);
	}

	private static void registerSpellClass(Class<? extends Spell> spellClass) {
		mSpellsList.put(spellClass.getSimpleName(), spellClass);

		try {
			Spell spell = spellClass.newInstance();
			String affinity = spell.getMagicAffinity();

			if(!mSpellsByAffinity.containsKey(affinity)) {
				mSpellsByAffinity.put(affinity, new ArrayList<>());
			}

			mSpellsByAffinity.get(affinity).add(spellClass.getSimpleName());

		} catch (InstantiationException e) {
			throw new TrackedRuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new TrackedRuntimeException(e);
		}
	}

	private static void initSpellsMap() {
		registerSpellClass(SummonDeathling.class);
		//registerSpellClass(Desecrate.class);

		registerSpellClass(WindGust.class);
		registerSpellClass(Ignite.class);
		registerSpellClass(RootSpell.class);
		registerSpellClass(FreezeGlobe.class);

		registerSpellClass(MagicTorch.class);
		registerSpellClass(Healing.class);
		//registerSpellClass(TownPortal.class);
	}

	public static boolean hasSpellForName (String name) {
		if (mSpellsList.get(name) != null) {
			return true;
		}
		script.run("haveSpell", name);
		return script.getResult().checkboolean();
	}

	@NotNull
	public static Spell getSpellByName(String name) {
		try {
			if(hasSpellForName(name)) {
				Class<? extends Spell> spellClass = mSpellsList.get(name);
				if (spellClass == null) {
					return new CustomSpell(name);
				}
				return spellClass.newInstance();
			} else {
				return getSpellByName(Random.element(getSpellsByAffinity(SpellHelper.AFFINITY_COMMON)));
			}

		} catch (InstantiationException e) {
			throw new TrackedRuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new TrackedRuntimeException(e);
		}
	}

	@NotNull
	public static ArrayList<String> getSpellsByAffinity(String affinity) {
		script.run("getSpellsList", affinity);
		LuaTable luaList = script.getResult().checktable();

		ArrayList<String> spellList = new ArrayList<>();

		if(mSpellsByAffinity.containsKey(affinity)) {
			spellList.addAll(mSpellsByAffinity.get(affinity));
		}

		for (int i = 1;i<=luaList.length();i++) {
			spellList.add(luaList.rawget(i).checkjstring());
		}

		return spellList;
	}
}
