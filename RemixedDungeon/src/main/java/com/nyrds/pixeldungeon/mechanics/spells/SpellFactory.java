package com.nyrds.pixeldungeon.mechanics.spells;

import com.nyrds.LuaInterface;
import com.nyrds.pixeldungeon.mechanics.LuaScript;
import com.watabou.pixeldungeon.Challenges;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.utils.Random;

import org.jetbrains.annotations.NotNull;
import org.luaj.vm2.LuaTable;

import java.util.ArrayList;
import org.apache.commons.collections4.map.HashedMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import lombok.SneakyThrows;


public class SpellFactory {

	static private Map<String, Class<? extends Spell>> mSpellsList;
	static private Map<String,ArrayList<String>> mSpellsByAffinity;

	static private final LuaScript script = new LuaScript("scripts/spells/SpellsByAffinity", null);

	static private final Set<String> forbiddenSpells = new HashSet<>();


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
		return getAllSpells().contains(name);
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

		spellList.removeAll(forbiddenSpells);

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

		spellList.removeAll(forbiddenSpells);

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
			mSpellsList = new HashedMap<>();
			mSpellsByAffinity = new HashedMap<>();

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

	public static void forbid(String spell) {
		forbiddenSpells.add(spell);
	}

	public static void reset() {
		forbiddenSpells.clear();
		Challenges.forbidSpells(Dungeon.getChallenges());
	}
}
