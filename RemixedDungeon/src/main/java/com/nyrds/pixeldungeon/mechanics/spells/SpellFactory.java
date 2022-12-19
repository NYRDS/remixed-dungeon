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


public class SpellFactory {

	static private Map<String, Class<? extends Spell>> mSpellsList;
	static private Map<String,ArrayList<String>> mSpellsByAffinity;

	static private final LuaScript script = new LuaScript("scripts/spells/SpellsByAffinity", null);


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


	public static boolean hasSpellForName (String name) {
		if (getSpellsList().get(name) != null) {
			return true;
		}

		return script.run("haveSpell", name).checkboolean();
	}

	@NotNull
	@SneakyThrows
	public static Spell getSpellByName(String name) {
		if(hasSpellForName(name)) {
			Class<? extends Spell> spellClass = getSpellsList().get(name);
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

		if(getSpellsByAffinity().containsKey(affinity)) {
			spellList.addAll(getSpellsByAffinity().get(affinity));
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

		for(var aff: getSpellsByAffinity().values()) {
			spellList.addAll(aff);
		}

		for (int i = 1;i<=luaList.length();i++) {
			spellList.add(luaList.rawget(i).checkjstring());
		}

		return spellList;
	}

	@LuaInterface
	public static Spell getRandomSpell() {
		return getSpellByName(Random.element(getAllSpells()));
	}

	private static Map<String, ArrayList<String>> getSpellsByAffinity() {
		getSpellsList();
		return mSpellsByAffinity;
	}

	private static Map<String, Class<? extends Spell>> getSpellsList() {

		if(mSpellsList == null) {
			mSpellsList = new HashMap<>();
			mSpellsByAffinity = new HashMap<>();

			registerSpellClass(SummonDeathling.class);

			registerSpellClass(WindGust.class);
			registerSpellClass(Ignite.class);
			registerSpellClass(RootSpell.class);
			registerSpellClass(FreezeGlobe.class);

			registerSpellClass(MagicTorch.class);
			registerSpellClass(Healing.class);

			script.run("loadSpells",null);
		}

		return mSpellsList;
	}
}
