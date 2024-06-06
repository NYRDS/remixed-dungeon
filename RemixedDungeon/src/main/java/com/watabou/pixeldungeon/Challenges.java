
package com.watabou.pixeldungeon;

import android.annotation.SuppressLint;

import com.nyrds.pixeldungeon.items.Treasury;
import com.nyrds.pixeldungeon.mechanics.spells.SpellFactory;
import com.watabou.pixeldungeon.items.DewVial;
import com.watabou.pixeldungeon.items.Dewdrop;
import com.watabou.pixeldungeon.items.Stylus;
import com.watabou.pixeldungeon.items.potions.PotionOfHealing;

import java.util.ArrayList;
import org.apache.commons.collections4.map.HashedMap;
import java.util.Map;
import java.util.Objects;

public class Challenges {

	public static final int NO_FOOD				= (int)Math.pow(2,0);
	public static final int NO_ARMOR			= (int)Math.pow(2,1);
	public static final int NO_HEALING			= (int)Math.pow(2,2);
	public static final int NO_HERBALISM		= (int)Math.pow(2,3);
	public static final int SWARM_INTELLIGENCE	= (int)Math.pow(2,4);
	public static final int DARKNESS			= (int)Math.pow(2,5);
	public static final int NO_SCROLLS          = (int)Math.pow(2,6);
	public static final int NO_WEAPON           = (int)Math.pow(2,7);
	public static final int NO_TOWN             = (int)Math.pow(2,8);


	public static final int[] MASKS = {
		NO_FOOD, NO_ARMOR, NO_HEALING, NO_HERBALISM, SWARM_INTELLIGENCE, DARKNESS, NO_SCROLLS, NO_WEAPON, NO_TOWN
	};

	@SuppressLint("UseSparseArrays")
	private static final Map<Integer, ArrayList<String>> forbiddenCategories = new HashedMap<>();

	public static final Map<Integer, ArrayList<String>> forbiddenSpells = new HashedMap<>();

	public static final Map<Integer, ArrayList<Integer>> conflictingFacilitations = new HashedMap<>();


	static {
		for(Integer mask:MASKS) {
			forbiddenCategories.put(mask, new ArrayList<>());
			forbiddenSpells.put(mask, new ArrayList<>());
			conflictingFacilitations.put(mask, new ArrayList<>());
		}

		Objects.requireNonNull(conflictingFacilitations.get(NO_FOOD)).add(Facilitations.NO_HUNGER);

		Objects.requireNonNull(forbiddenCategories.get(NO_FOOD)).add(Treasury.Category.FOOD.name());
		Objects.requireNonNull(forbiddenCategories.get(NO_ARMOR)).add(Treasury.Category.ARMOR.name());
		Objects.requireNonNull(forbiddenCategories.get(NO_HEALING)).add(PotionOfHealing.class.getSimpleName());
		Objects.requireNonNull(forbiddenCategories.get(NO_HERBALISM)).add(Treasury.Category.SEED.name());
		Objects.requireNonNull(forbiddenCategories.get(NO_HERBALISM)).add(Dewdrop.class.getSimpleName());
		Objects.requireNonNull(forbiddenCategories.get(NO_HERBALISM)).add(DewVial.class.getSimpleName());
		Objects.requireNonNull(forbiddenCategories.get(NO_SCROLLS)).add(Treasury.Category.SCROLL.name());
		Objects.requireNonNull(forbiddenCategories.get(NO_SCROLLS)).add(Stylus.class.getSimpleName());
		Objects.requireNonNull(forbiddenCategories.get(NO_WEAPON)).add(Treasury.Category.WEAPON.name());
		Objects.requireNonNull(forbiddenCategories.get(NO_WEAPON)).add(Treasury.Category.WAND.name());
		Objects.requireNonNull(forbiddenCategories.get(NO_WEAPON)).add(Treasury.Category.THROWABLE.name());
		Objects.requireNonNull(forbiddenCategories.get(NO_WEAPON)).add(Treasury.Category.BULLETS.name());
		Objects.requireNonNull(forbiddenCategories.get(NO_WEAPON)).add(Treasury.Category.RANGED.name());

		Objects.requireNonNull(forbiddenSpells.get(NO_TOWN)).add("TownPortal");

	}

	public static void forbidCategories(int challenge,Treasury treasury) {
		for(int mask:MASKS) {
			if((mask & challenge) != 0 && forbiddenCategories.containsKey(mask)) {
				for(String catOrItem: Objects.requireNonNull(forbiddenCategories.get(mask))) {
					treasury.forbid(catOrItem);
				}
			}
		}
	}

	public static void forbidSpells(int challenge) {
		for(int mask:MASKS) {
			if((mask & challenge) != 0 && forbiddenSpells.containsKey(mask)) {
				for(String spell: Objects.requireNonNull(forbiddenSpells.get(mask))) {
					SpellFactory.forbid(spell);
				}
			}
		}
	}
}
