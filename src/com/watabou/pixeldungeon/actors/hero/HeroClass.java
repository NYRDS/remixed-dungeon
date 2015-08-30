/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
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

package com.watabou.pixeldungeon.actors.hero;

import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Badges;
import com.watabou.pixeldungeon.items.Ankh;
import com.watabou.pixeldungeon.items.ArmorKit;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.LloydsBeacon;
import com.watabou.pixeldungeon.items.TomeOfMastery;
import com.watabou.pixeldungeon.items.armor.ClothArmor;
import com.watabou.pixeldungeon.items.bags.Keyring;
import com.watabou.pixeldungeon.items.bags.PotionBelt;
import com.watabou.pixeldungeon.items.bags.Quiver;
import com.watabou.pixeldungeon.items.bags.ScrollHolder;
import com.watabou.pixeldungeon.items.bags.SeedPouch;
import com.watabou.pixeldungeon.items.bags.WandHolster;
import com.watabou.pixeldungeon.items.food.Ration;
import com.watabou.pixeldungeon.items.food.RottenPasty;
import com.watabou.pixeldungeon.items.potions.PotionOfFrost;
import com.watabou.pixeldungeon.items.potions.PotionOfLevitation;
import com.watabou.pixeldungeon.items.potions.PotionOfLiquidFlame;
import com.watabou.pixeldungeon.items.potions.PotionOfPurity;
import com.watabou.pixeldungeon.items.potions.PotionOfStrength;
import com.watabou.pixeldungeon.items.rings.RingOfPower;
import com.watabou.pixeldungeon.items.rings.RingOfShadows;
import com.watabou.pixeldungeon.items.rings.RingOfStoneWalking;
import com.watabou.pixeldungeon.items.scrolls.ScrollOfIdentify;
import com.watabou.pixeldungeon.items.scrolls.ScrollOfMagicMapping;
import com.watabou.pixeldungeon.items.wands.WandOfFirebolt;
import com.watabou.pixeldungeon.items.wands.WandOfMagicMissile;
import com.watabou.pixeldungeon.items.wands.WandOfTelekinesis;
import com.watabou.pixeldungeon.items.wands.WandOfTeleportation;
import com.watabou.pixeldungeon.items.weapon.melee.Dagger;
import com.watabou.pixeldungeon.items.weapon.melee.Glaive;
import com.watabou.pixeldungeon.items.weapon.melee.Knuckles;
import com.watabou.pixeldungeon.items.weapon.melee.ShortSword;
import com.watabou.pixeldungeon.items.weapon.melee.WoodenBow;
import com.watabou.pixeldungeon.items.weapon.missiles.Boomerang;
import com.watabou.pixeldungeon.items.weapon.missiles.CommonArrow;
import com.watabou.pixeldungeon.items.weapon.missiles.Dart;
import com.watabou.pixeldungeon.plants.Icecap;
import com.watabou.pixeldungeon.ui.QuickSlot;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.utils.Bundle;

public enum HeroClass {

	WARRIOR(Game.getVar(R.string.HeroClass_War)), MAGE(Game
			.getVar(R.string.HeroClass_Mag)), ROGUE(Game
			.getVar(R.string.HeroClass_Rog)), HUNTRESS(Game
			.getVar(R.string.HeroClass_Hun)), ELF(Game
			.getVar(R.string.HeroClass_Elf));

	private String title;

	public static final String[] WAR_PERKS = Game
			.getVars(R.array.HeroClass_WarPerks);
	public static final String[] MAG_PERKS = Game
			.getVars(R.array.HeroClass_MagPerks);
	public static final String[] ROG_PERKS = Game
			.getVars(R.array.HeroClass_RogPerks);
	public static final String[] HUN_PERKS = Game
			.getVars(R.array.HeroClass_HunPerks);
	public static final String[] ELF_PERKS = Game
			.getVars(R.array.HeroClass_ElfPerks);

	private HeroClass(String title) {
		this.title = title;
	}

	public void initHero(Hero hero) {
		hero.heroClass = this;
		initCommon(hero);

		switch (this) {
		case WARRIOR:
			initWarrior(hero);
			break;

		case MAGE:
			initMage(hero);
			break;

		case ROGUE:
			initRogue(hero);
			break;

		case HUNTRESS:
			initHuntress(hero);
			break;

		case ELF:
			initElf(hero);
			break;
		}

		hero.gender = getGender();

		if (Badges.isUnlocked(masteryBadge())) {
			new TomeOfMastery().collect(hero);
		}

		hero.updateAwareness();
	}

	private static void initDebug(Hero hero) {
		hero.collect(new TomeOfMastery());

		Item gl = new Glaive().upgrade(8);
		gl.cursed = true;

		Item rr = new RingOfShadows().degrade(4);
		rr.cursed = true;

		hero.collect(rr);

		hero.collect(gl);

		hero.hp(hero.ht(1000));
		hero.STR(18);

		hero.attackSkill += 10;
		hero.defenseSkill += 10;

		hero.collect(new PotionOfStrength().identify());
		hero.collect(new PotionOfLiquidFlame().identify());
		hero.collect(new PotionOfFrost().identify());
		hero.collect(new PotionOfFrost().identify());
		hero.collect(new PotionOfFrost().identify());
		hero.collect(new PotionOfFrost().identify());
		hero.collect(new PotionOfFrost().identify());
		hero.collect(new PotionOfFrost().identify());
		hero.collect(new PotionOfPurity().identify());
		hero.collect(new PotionOfLevitation().identify());
		hero.collect(new WandOfTelekinesis().identify());
		hero.collect(new WandOfFirebolt());

		hero.collect(new Icecap.Seed());
		hero.collect(new Icecap.Seed());
		hero.collect(new Icecap.Seed());
		hero.collect(new Icecap.Seed());

		hero.collect(new RingOfPower().upgrade(3).identify());

		hero.collect(new RottenPasty());
		hero.collect(new ArmorKit());
		hero.collect(new CommonArrow(100));
		hero.collect(new Quiver());
		hero.collect(new SeedPouch());
		hero.collect(new WandHolster());
		hero.collect(new ScrollHolder());
		hero.collect(new PotionBelt());
		hero.collect(new Keyring());

		for(int i = 0; i < 100; ++i){
			hero.collect(new ScrollOfMagicMapping());
		}
		
		
		hero.collect(new LloydsBeacon());
		hero.collect(new WandOfTeleportation());
		hero.collect(new Ankh());

		hero.collect(new RingOfStoneWalking());
	}

	private static void initCommon(Hero hero) {
		(hero.belongings.armor = new ClothArmor()).identify();
		hero.collect(new Ration());

		//if (ModdingMode.mode()) {
			initDebug(hero);
		//}

		QuickSlot.cleanStorage();
	}

	public Badges.Badge masteryBadge() {
		switch (this) {
		case WARRIOR:
			return Badges.Badge.MASTERY_WARRIOR;
		case MAGE:
			return Badges.Badge.MASTERY_MAGE;
		case ROGUE:
			return Badges.Badge.MASTERY_ROGUE;
		case HUNTRESS:
			return Badges.Badge.MASTERY_HUNTRESS;
		case ELF:
			return Badges.Badge.MASTERY_ELF;
		}
		return null;
	}

	private static void initWarrior(Hero hero) {
		hero.STR(hero.STR() + 1);

		(hero.belongings.weapon = new ShortSword()).identify();
		hero.collect(new Dart(8).identify());

		QuickSlot.selectItem(Dart.class, 0);

		new PotionOfStrength().setKnown();
	}

	private static void initMage(Hero hero) {
		(hero.belongings.weapon = new Knuckles()).identify();

		WandOfMagicMissile wand = new WandOfMagicMissile();
		hero.collect(wand.identify());

		QuickSlot.selectItem(wand, 0);

		new ScrollOfIdentify().setKnown();
	}

	private static void initRogue(Hero hero) {
		(hero.belongings.weapon = new Dagger()).identify();
		(hero.belongings.ring1 = new RingOfShadows()).upgrade().identify();

		hero.collect(new Dart(8).identify());

		hero.belongings.ring1.activate(hero);

		QuickSlot.selectItem(Dart.class, 0);

		new ScrollOfMagicMapping().setKnown();
	}

	private static void initHuntress(Hero hero) {
		hero.ht(hero.ht() - 5);
		hero.hp(hero.ht());

		(hero.belongings.weapon = new Dagger()).identify();
		Boomerang boomerang = new Boomerang();
		hero.collect(boomerang.identify());

		QuickSlot.selectItem(boomerang, 0);
	}

	private void initElf(Hero hero) {
		hero.STR(hero.STR() - 1);

		hero.ht(hero.ht() - 5);
		hero.hp(hero.ht());

		(hero.belongings.armor = new ClothArmor()).upgrade().identify();
		(hero.belongings.weapon = new WoodenBow()).upgrade().identify();

		hero.collect(new Dagger().upgrade().identify());
		hero.collect(new CommonArrow(20));

		QuickSlot.selectItem(CommonArrow.class, 0);
	}

	public String title() {
		return title;
	}

	public static String spritesheet(Hero hero) {
		switch (hero.heroClass) {
		case WARRIOR:
			switch (hero.subClass) {
			case BERSERKER:
				if (hero.inFury()) {
					return Assets.WARRIOR_BERSERK_IN_FURY;
				}
				return Assets.WARRIOR_BERSERK;
			case GLADIATOR:
				return Assets.WARRIOR_GLADIATOR;

			default:
				return Assets.WARRIOR;
			}
		case MAGE:
			switch (hero.subClass) {
			case BATTLEMAGE:
				return Assets.MAGE_BATTLEMAGE;
			case WARLOCK:
				return Assets.MAGE_WARLOCK;
			default:
				return Assets.MAGE;
			}
		case ROGUE:
			switch (hero.subClass) {
			case ASSASSIN:
				return Assets.ROGUE_ASSASIN;
			case FREERUNNER:
				return Assets.ROGUE_FREERUNNER;
			default:
				return Assets.ROGUE;
			}
		case HUNTRESS:
			switch (hero.subClass) {
			case SNIPER:
				return Assets.HUNTRESS_SNIPER;
			case WARDEN:
				return Assets.HUNTRESS_WARDEN;

			default:
				return Assets.HUNTRESS;
			}
		case ELF:
			switch (hero.subClass) {
			case SCOUT:
				return Assets.ELF_SCOUT;
			case SHAMAN:
				return Assets.ELF_SHAMAN;

			default:
				return Assets.ELF;
			}
		}

		return null;
	}

	public String[] perks() {

		switch (this) {
		case WARRIOR:
			return WAR_PERKS;
		case MAGE:
			return MAG_PERKS;
		case ROGUE:
			return ROG_PERKS;
		case HUNTRESS:
			return HUN_PERKS;
		case ELF:
			return ELF_PERKS;
		}

		return null;
	}

	public int getGender() {
		switch (this) {
		case WARRIOR:
		case MAGE:
		case ROGUE:
		case ELF:
			return Utils.MASCULINE;
		case HUNTRESS:
			return Utils.FEMININE;
		}
		return Utils.NEUTER;
	}

	private static final String CLASS = "class";

	public void storeInBundle(Bundle bundle) {
		bundle.put(CLASS, toString());
	}

	public static HeroClass restoreInBundle(Bundle bundle) {
		String value = bundle.getString(CLASS);
		return value.length() > 0 ? valueOf(value) : ROGUE;
	}

	public static boolean isSpriteSheet(String spriteKind) {
		if (spriteKind.equals(Assets.WARRIOR))
			return true;

		if (spriteKind.equals(Assets.WARRIOR_BERSERK))
			return true;

		if (spriteKind.equals(Assets.WARRIOR_BERSERK_IN_FURY))
			return true;

		if (spriteKind.equals(Assets.WARRIOR_GLADIATOR))
			return true;

		if (spriteKind.equals(Assets.MAGE))
			return true;

		if (spriteKind.equals(Assets.MAGE_BATTLEMAGE))
			return true;

		if (spriteKind.equals(Assets.MAGE_WARLOCK))
			return true;

		if (spriteKind.equals(Assets.ROGUE))
			return true;

		if (spriteKind.equals(Assets.ROGUE_ASSASIN))
			return true;

		if (spriteKind.equals(Assets.ROGUE_FREERUNNER))
			return true;
		
		if (spriteKind.equals(Assets.HUNTRESS))
			return true;

		if (spriteKind.equals(Assets.HUNTRESS_SNIPER))
			return true;

		if (spriteKind.equals(Assets.HUNTRESS_WARDEN))
			return true;

		if (spriteKind.equals(Assets.ELF))
			return true;

		if (spriteKind.equals(Assets.ELF_SCOUT))
			return true;

		if (spriteKind.equals(Assets.ELF_SHAMAN))
			return true;

		return false;
	}
}
