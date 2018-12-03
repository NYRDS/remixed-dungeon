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
package com.watabou.pixeldungeon.items;

import com.nyrds.android.util.TrackedRuntimeException;
import com.nyrds.pixeldungeon.items.artifacts.SpellBook;
import com.nyrds.pixeldungeon.items.common.GoldenSword;
import com.nyrds.pixeldungeon.items.common.SacrificialSword;
import com.nyrds.pixeldungeon.items.drinks.Drink;
import com.nyrds.pixeldungeon.items.drinks.ManaPotion;
import com.nyrds.pixeldungeon.items.food.ChristmasTurkey;
import com.nyrds.pixeldungeon.items.food.PumpkinPie;
import com.nyrds.pixeldungeon.items.guts.armor.GothicArmor;
import com.nyrds.pixeldungeon.items.guts.weapon.melee.Claymore;
import com.nyrds.pixeldungeon.items.guts.weapon.melee.Halberd;
import com.nyrds.pixeldungeon.items.guts.weapon.ranged.CompositeCrossbow;
import com.nyrds.pixeldungeon.items.guts.weapon.ranged.RubyCrossbow;
import com.nyrds.pixeldungeon.items.guts.weapon.ranged.WoodenCrossbow;
import com.nyrds.pixeldungeon.ml.EventCollector;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.mobs.npcs.WandMaker.Rotberry;
import com.watabou.pixeldungeon.items.armor.Armor;
import com.watabou.pixeldungeon.items.armor.ClothArmor;
import com.watabou.pixeldungeon.items.armor.LeatherArmor;
import com.watabou.pixeldungeon.items.armor.MailArmor;
import com.watabou.pixeldungeon.items.armor.PlateArmor;
import com.watabou.pixeldungeon.items.armor.ScaleArmor;
import com.watabou.pixeldungeon.items.bags.Bag;
import com.watabou.pixeldungeon.items.food.Food;
import com.watabou.pixeldungeon.items.food.MysteryMeat;
import com.watabou.pixeldungeon.items.food.Pasty;
import com.watabou.pixeldungeon.items.food.Ration;
import com.watabou.pixeldungeon.items.potions.Potion;
import com.watabou.pixeldungeon.items.potions.PotionOfExperience;
import com.watabou.pixeldungeon.items.potions.PotionOfFrost;
import com.watabou.pixeldungeon.items.potions.PotionOfHealing;
import com.watabou.pixeldungeon.items.potions.PotionOfInvisibility;
import com.watabou.pixeldungeon.items.potions.PotionOfLevitation;
import com.watabou.pixeldungeon.items.potions.PotionOfLiquidFlame;
import com.watabou.pixeldungeon.items.potions.PotionOfMight;
import com.watabou.pixeldungeon.items.potions.PotionOfMindVision;
import com.watabou.pixeldungeon.items.potions.PotionOfParalyticGas;
import com.watabou.pixeldungeon.items.potions.PotionOfPurity;
import com.watabou.pixeldungeon.items.potions.PotionOfStrength;
import com.watabou.pixeldungeon.items.potions.PotionOfToxicGas;
import com.watabou.pixeldungeon.items.rings.Ring;
import com.watabou.pixeldungeon.items.rings.RingOfAccuracy;
import com.watabou.pixeldungeon.items.rings.RingOfDetection;
import com.watabou.pixeldungeon.items.rings.RingOfElements;
import com.watabou.pixeldungeon.items.rings.RingOfEvasion;
import com.watabou.pixeldungeon.items.rings.RingOfHaggler;
import com.watabou.pixeldungeon.items.rings.RingOfHaste;
import com.watabou.pixeldungeon.items.rings.RingOfHerbalism;
import com.watabou.pixeldungeon.items.rings.RingOfMending;
import com.watabou.pixeldungeon.items.rings.RingOfPower;
import com.watabou.pixeldungeon.items.rings.RingOfSatiety;
import com.watabou.pixeldungeon.items.rings.RingOfShadows;
import com.watabou.pixeldungeon.items.rings.RingOfThorns;
import com.watabou.pixeldungeon.items.scrolls.BlankScroll;
import com.watabou.pixeldungeon.items.scrolls.Scroll;
import com.watabou.pixeldungeon.items.scrolls.ScrollOfChallenge;
import com.watabou.pixeldungeon.items.scrolls.ScrollOfCurse;
import com.watabou.pixeldungeon.items.scrolls.ScrollOfDomination;
import com.watabou.pixeldungeon.items.scrolls.ScrollOfIdentify;
import com.watabou.pixeldungeon.items.scrolls.ScrollOfLullaby;
import com.watabou.pixeldungeon.items.scrolls.ScrollOfMagicMapping;
import com.watabou.pixeldungeon.items.scrolls.ScrollOfMirrorImage;
import com.watabou.pixeldungeon.items.scrolls.ScrollOfPsionicBlast;
import com.watabou.pixeldungeon.items.scrolls.ScrollOfRecharging;
import com.watabou.pixeldungeon.items.scrolls.ScrollOfRemoveCurse;
import com.watabou.pixeldungeon.items.scrolls.ScrollOfSummoning;
import com.watabou.pixeldungeon.items.scrolls.ScrollOfTeleportation;
import com.watabou.pixeldungeon.items.scrolls.ScrollOfTerror;
import com.watabou.pixeldungeon.items.scrolls.ScrollOfUpgrade;
import com.watabou.pixeldungeon.items.scrolls.ScrollOfWeaponUpgrade;
import com.watabou.pixeldungeon.items.wands.Wand;
import com.watabou.pixeldungeon.items.wands.WandOfAmok;
import com.watabou.pixeldungeon.items.wands.WandOfAvalanche;
import com.watabou.pixeldungeon.items.wands.WandOfBlink;
import com.watabou.pixeldungeon.items.wands.WandOfDisintegration;
import com.watabou.pixeldungeon.items.wands.WandOfFirebolt;
import com.watabou.pixeldungeon.items.wands.WandOfFlock;
import com.watabou.pixeldungeon.items.wands.WandOfLightning;
import com.watabou.pixeldungeon.items.wands.WandOfMagicMissile;
import com.watabou.pixeldungeon.items.wands.WandOfPoison;
import com.watabou.pixeldungeon.items.wands.WandOfRegrowth;
import com.watabou.pixeldungeon.items.wands.WandOfSlowness;
import com.watabou.pixeldungeon.items.wands.WandOfTelekinesis;
import com.watabou.pixeldungeon.items.wands.WandOfTeleportation;
import com.watabou.pixeldungeon.items.weapon.Weapon;
import com.watabou.pixeldungeon.items.weapon.melee.BattleAxe;
import com.watabou.pixeldungeon.items.weapon.melee.CompoundBow;
import com.watabou.pixeldungeon.items.weapon.melee.Dagger;
import com.watabou.pixeldungeon.items.weapon.melee.Glaive;
import com.watabou.pixeldungeon.items.weapon.melee.KindOfBow;
import com.watabou.pixeldungeon.items.weapon.melee.Knuckles;
import com.watabou.pixeldungeon.items.weapon.melee.Kusarigama;
import com.watabou.pixeldungeon.items.weapon.melee.Longsword;
import com.watabou.pixeldungeon.items.weapon.melee.Mace;
import com.watabou.pixeldungeon.items.weapon.melee.Quarterstaff;
import com.watabou.pixeldungeon.items.weapon.melee.RubyBow;
import com.watabou.pixeldungeon.items.weapon.melee.ShortSword;
import com.watabou.pixeldungeon.items.weapon.melee.Spear;
import com.watabou.pixeldungeon.items.weapon.melee.Sword;
import com.watabou.pixeldungeon.items.weapon.melee.WarHammer;
import com.watabou.pixeldungeon.items.weapon.melee.WoodenBow;
import com.watabou.pixeldungeon.items.weapon.missiles.AmokArrow;
import com.watabou.pixeldungeon.items.weapon.missiles.AmokDart;
import com.watabou.pixeldungeon.items.weapon.missiles.Arrow;
import com.watabou.pixeldungeon.items.weapon.missiles.Boomerang;
import com.watabou.pixeldungeon.items.weapon.missiles.CommonArrow;
import com.watabou.pixeldungeon.items.weapon.missiles.CurareDart;
import com.watabou.pixeldungeon.items.weapon.missiles.Dart;
import com.watabou.pixeldungeon.items.weapon.missiles.FireArrow;
import com.watabou.pixeldungeon.items.weapon.missiles.HealthArrow;
import com.watabou.pixeldungeon.items.weapon.missiles.HealthDart;
import com.watabou.pixeldungeon.items.weapon.missiles.IncendiaryDart;
import com.watabou.pixeldungeon.items.weapon.missiles.Javelin;
import com.watabou.pixeldungeon.items.weapon.missiles.MissileWeapon;
import com.watabou.pixeldungeon.items.weapon.missiles.ParalysisArrow;
import com.watabou.pixeldungeon.items.weapon.missiles.PoisonArrow;
import com.watabou.pixeldungeon.items.weapon.missiles.Shuriken;
import com.watabou.pixeldungeon.items.weapon.missiles.Tamahawk;
import com.watabou.pixeldungeon.plants.Dreamweed;
import com.watabou.pixeldungeon.plants.Earthroot;
import com.watabou.pixeldungeon.plants.Fadeleaf;
import com.watabou.pixeldungeon.plants.Firebloom;
import com.watabou.pixeldungeon.plants.Icecap;
import com.watabou.pixeldungeon.plants.Plant;
import com.watabou.pixeldungeon.plants.Sorrowmoss;
import com.watabou.pixeldungeon.plants.Sungrass;
import com.watabou.utils.Random;

import java.util.HashMap;
import java.util.Locale;

import androidx.annotation.NonNull;

public class Generator {

	public enum Category {
		WEAPON(15, Weapon.class),
		ARMOR(10, Armor.class),
		POTION(50, Potion.class),
		SCROLL(40, Scroll.class),
		WAND(4, Wand.class),
		RING(2, Ring.class),
		SEED(5, Plant.Seed.class),
		FOOD(0, Food.class),
		GOLD(50, Gold.class),
		RANGED(2, KindOfBow.class),
		BULLETS(5, Arrow.class),
		THROWABLE(0, MissileWeapon.class),
		DRINK(25, Drink.class),
		UNIQUE(2, Item.class);

		public Class<?>[] classes;
		public float[]    probs;

		private float                 prob;
		private Class<? extends Item> superClass;

		Category(float prob, Class<? extends Item> superClass) {
			this.prob = prob;
			this.superClass = superClass;
		}

		public static int order(Item item) {
			for (int i = 0; i < values().length; i++) {
				if (values()[i].superClass.isInstance(item)) {
					return i;
				}
			}

			return item instanceof Bag ? Integer.MAX_VALUE : Integer.MAX_VALUE - 1;
		}

		@NonNull
		public Item random() {
			int index;
			do {
				index = Random.chances(probs);
				if (!(index < classes.length)) {
					EventCollector.logException("bad index in category" + name());
				}
			} while (!(index < classes.length));

			try {
				return ((Item) classes[index].newInstance()).random();
			} catch (Exception e) {
				EventCollector.logException(e, "bug: " + name());
				return new Gold(5);
			}
		}
	}

	private static HashMap<Category, Float> categoryProbs = new HashMap<>();

	static {

		Category.GOLD.classes = new Class<?>[]{
				Gold.class};
		Category.GOLD.probs = new float[]{1};

		Category.SCROLL.classes = new Class<?>[]{
				ScrollOfIdentify.class,
				ScrollOfTeleportation.class,
				ScrollOfRemoveCurse.class,
				ScrollOfUpgrade.class,
				ScrollOfRecharging.class,
				ScrollOfMagicMapping.class,
				ScrollOfChallenge.class,
				ScrollOfTerror.class,
				ScrollOfLullaby.class,
				ScrollOfWeaponUpgrade.class,
				ScrollOfPsionicBlast.class,
				ScrollOfMirrorImage.class,
				BlankScroll.class,
				ScrollOfDomination.class,
				ScrollOfSummoning.class,
				ScrollOfCurse.class};
		Category.SCROLL.probs = new float[]{30, 10, 15, 0, 10, 15, 12, 8, 8, 0, 4, 6, 10, 8, 4, 6};

		Category.POTION.classes = new Class<?>[]{
				PotionOfHealing.class,
				PotionOfExperience.class,
				PotionOfToxicGas.class,
				PotionOfParalyticGas.class,
				PotionOfLiquidFlame.class,
				PotionOfLevitation.class,
				PotionOfStrength.class,
				PotionOfMindVision.class,
				PotionOfPurity.class,
				PotionOfInvisibility.class,
				PotionOfMight.class,
				PotionOfFrost.class};
		Category.POTION.probs = new float[]{45, 4, 15, 10, 15, 10, 0, 20, 12, 10, 0, 10};

		Category.WAND.classes = new Class<?>[]{
				WandOfTeleportation.class,
				WandOfSlowness.class,
				WandOfFirebolt.class,
				WandOfRegrowth.class,
				WandOfPoison.class,
				WandOfBlink.class,
				WandOfLightning.class,
				WandOfAmok.class,
				WandOfTelekinesis.class,
				WandOfFlock.class,
				WandOfMagicMissile.class,
				WandOfDisintegration.class,
				WandOfAvalanche.class};
		Category.WAND.probs = new float[]{10, 10, 15, 6, 10, 11, 15, 10, 6, 10, 0, 5, 5};

		Category.WEAPON.classes = new Class<?>[]{
				Dagger.class,
				Knuckles.class,
				Quarterstaff.class,
				Spear.class,
				Mace.class,
				Sword.class,
				Longsword.class,
				BattleAxe.class,
				WarHammer.class,
				Glaive.class,
				ShortSword.class,
				Dart.class,
				Javelin.class,
				IncendiaryDart.class,
				CurareDart.class,
				Shuriken.class,
				Boomerang.class,
				Tamahawk.class,
				Kusarigama.class,
				SacrificialSword.class,
				Claymore.class,
				Halberd.class,
				GoldenSword.class,
				AmokDart.class,
				HealthDart.class};
		Category.WEAPON.probs = new float[]{1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 1, 1, 1, 1, 0, 1, 0.1f, 0.1f, 1, 1, 0.1f, 0.1f, 0.1f};

		Category.THROWABLE.classes = new Class<?>[]{
				Dart.class,
				Javelin.class,
				IncendiaryDart.class,
				CurareDart.class,
				Shuriken.class,
				Tamahawk.class};

		Category.THROWABLE.probs = new float[]{1, 1, 1, 1, 1, 1};

		Category.ARMOR.classes = new Class<?>[]{
				ClothArmor.class,
				LeatherArmor.class,
				MailArmor.class,
				ScaleArmor.class,
				PlateArmor.class,
				GothicArmor.class};
		Category.ARMOR.probs = new float[]{1, 1, 1, 1, 1, 1};

		Category.FOOD.classes = new Class<?>[]{
				Ration.class,
				Pasty.class,
				MysteryMeat.class,
				PumpkinPie.class,
				ChristmasTurkey.class};
		Category.FOOD.probs = new float[]{4, 1, 0, 0, 0};

		Category.DRINK.classes = new Class<?>[]{
				ManaPotion.class};
		Category.DRINK.probs = new float[]{1};

		Category.RING.classes = new Class<?>[]{
				RingOfMending.class,
				RingOfDetection.class,
				RingOfShadows.class,
				RingOfPower.class,
				RingOfHerbalism.class,
				RingOfAccuracy.class,
				RingOfEvasion.class,
				RingOfSatiety.class,
				RingOfHaste.class,
				RingOfElements.class,
				RingOfHaggler.class,
				RingOfThorns.class};
		Category.RING.probs = new float[]{1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0};

		Category.SEED.classes = new Class<?>[]{
				Firebloom.Seed.class,
				Icecap.Seed.class,
				Sorrowmoss.Seed.class,
				Dreamweed.Seed.class,
				Sungrass.Seed.class,
				Earthroot.Seed.class,
				Fadeleaf.Seed.class,
				Rotberry.Seed.class};
		Category.SEED.probs = new float[]{1, 1, 1, 1, 1, 1, 1, 0};

		Category.RANGED.classes = new Class<?>[]{
				WoodenBow.class,
				WoodenCrossbow.class,
				CompoundBow.class,
				CompositeCrossbow.class,
				RubyBow.class,
				RubyCrossbow.class
		};
		Category.RANGED.probs = new float[]{3, 4, 3, 3, 1, 1};

		Category.BULLETS.classes = new Class<?>[]{
				CommonArrow.class,
				FireArrow.class,
				PoisonArrow.class,
				ParalysisArrow.class,
                HealthArrow.class,
				AmokArrow.class
		};
		Category.BULLETS.probs = new float[]{10, 2, 2, 2, 2, 2};

		Category.UNIQUE.classes = new Class<?>[]{
				SpellBook.class,
				Torch.class};
		Category.UNIQUE.probs = new float[]{1, 4};

		reset();
	}

	public static void reset() {
		for (Category cat : Category.values()) {
			if (cat.probs.length != cat.classes.length) {
				throw new TrackedRuntimeException(String.format(Locale.ROOT, "Category: %s %d items %d probs", cat.name(), cat.classes.length, cat.probs.length));
			}

			categoryProbs.put(cat, cat.prob);
		}
	}

	public static Item random() {
		return random(Random.chances(categoryProbs));
	}

	public static Item random(Category cat) {
		try {
			categoryProbs.put(cat, categoryProbs.get(cat) / 2);

			switch (cat) {
				case ARMOR:
					return randomArmor();
				case WEAPON:
					return randomWeapon();
				default:
					return cat.random();
			}
		} catch (Exception e) {
			EventCollector.logException(e, "bug: " + cat.name());
			return new Gold(5);
		}
	}

	public static Item random(Class<? extends Item> cl) {
		try {
			return cl.newInstance().random();
		} catch (Exception e) {
			throw new TrackedRuntimeException(e);
		}
	}

	private static Armor randomArmor() throws Exception {

		int curStr = Hero.STARTING_STR + Dungeon.potionOfStrength;

		Category cat = Category.ARMOR;

		Armor a1 = (Armor) cat.random();
		Armor a2 = (Armor) cat.random();

		return Math.abs(curStr - a1.STR) < Math.abs(curStr - a2.STR) ? a1 : a2;
	}

	private static Weapon randomWeapon() throws Exception {

		int curStr = Hero.STARTING_STR + Dungeon.potionOfStrength;

		Category cat = Category.WEAPON;

		Weapon w1 = (Weapon) cat.random();
		Weapon w2 = (Weapon) cat.random();

		return Math.abs(curStr - w1.STR) < Math.abs(curStr - w2.STR) ? w1 : w2;
	}
}
