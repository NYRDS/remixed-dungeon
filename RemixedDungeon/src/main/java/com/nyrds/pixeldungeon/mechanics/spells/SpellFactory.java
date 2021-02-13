package com.nyrds.pixeldungeon.mechanics.spells;

import com.nyrds.LuaInterface;
import com.nyrds.pixeldungeon.mechanics.LuaScript;
import com.watabou.utils.Random;

import org.jetbrains.annotations.NotNull;
import org.luaj.vm2.LuaTable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import lombok.SneakyThrows;
import lombok.var;

public class SpellFactory {

	static private final Map<String, Class<? extends Spell>> mSpellsList = new HashMap<>();

	static private final LuaScript script = new LuaScript("scripts/spells/SpellsByAffinity", null);

	static private final Map<String,ArrayList<String>> mSpellsByAffinity = new HashMap<>();

	static {
		initSpellsMap();
		script.run("loadSpells",null);
	}

	@SneakyThrows
	private static void registerSpellClass(Class<? extends Spell> spellClass) {
		mSpellsList.put(spellClass.getSimpleName(), spellClass);

			Spell spell = spellClass.newInstance();
			String affinity = spell.getMagicAffinity();

			if(!mSpellsByAffinity.containsKey(affinity)) {
				mSpellsByAffinity.put(affinity, new ArrayList<>());
			}

			mSpellsByAffinity.get(affinity).add(spellClass.getSimpleName());
	}

	private static void initSpellsMap() {
		registerSpellClass(SummonDeathling.class);

		registerSpellClass(WindGust.class);
		registerSpellClass(Ignite.class);
		registerSpellClass(RootSpell.class);
		registerSpellClass(FreezeGlobe.class);

		registerSpellClass(MagicTorch.class);
		registerSpellClass(Healing.class);
	}

	public static boolean hasSpellForName (String name) {
		if (mSpellsList.get(name) != null) {
			return true;
		}

		return script.run("haveSpell", name).checkboolean();
	}

	@NotNull
	@SneakyThrows
	public static Spell getSpellByName(String name) {
		if(hasSpellForName(name)) {
			Class<? extends Spell> spellClass = mSpellsList.get(name);
			if (spellClass == null) {
				return new CustomSpell(name);
			}
			return spellClass.newInstance();
		} else {
			return getSpellByName(Random.element(getSpellsByAffinity(SpellHelper.AFFINITY_COMMON)));
		}
	}

	@NotNull
	public static ArrayList<String> getSpellsByAffinity(String affinity) {
		LuaTable luaList = script.run("getSpellsList", affinity).checktable();

		ArrayList<String> spellList = new ArrayList<>();

		if(mSpellsByAffinity.containsKey(affinity)) {
			spellList.addAll(mSpellsByAffinity.get(affinity));
		}

		for (int i = 1;i<=luaList.length();i++) {
			spellList.add(luaList.rawget(i).checkjstring());
		}

		return spellList;
	}

	@NotNull
	public static ArrayList<String> getAllSpells() {
		LuaTable luaList = script.run("getSpellsList").checktable();

		ArrayList<String> spellList = new ArrayList<>();

		for(var aff: mSpellsByAffinity.values()) {
			spellList.addAll(aff);
		}

		for (int i = 1;i<=luaList.length();i++) {
			spellList.add(luaList.rawget(i).checkjstring());
		}

		return spellList;
	}

	@LuaInterface
	Spell getRandomSpell() {
		return getSpellByName(Random.element(getAllSpells()));
	}
}
