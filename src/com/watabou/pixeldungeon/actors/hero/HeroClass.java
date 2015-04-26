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
import com.watabou.pixeldungeon.items.TomeOfMastery;
import com.watabou.pixeldungeon.items.armor.ClothArmor;
import com.watabou.pixeldungeon.items.food.Ration;
import com.watabou.pixeldungeon.items.potions.PotionOfStrength;
import com.watabou.pixeldungeon.items.rings.RingOfShadows;
import com.watabou.pixeldungeon.items.scrolls.ScrollOfIdentify;
import com.watabou.pixeldungeon.items.scrolls.ScrollOfMagicMapping;
import com.watabou.pixeldungeon.items.wands.WandOfMagicMissile;
import com.watabou.pixeldungeon.items.weapon.melee.Dagger;
import com.watabou.pixeldungeon.items.weapon.melee.Knuckles;
import com.watabou.pixeldungeon.items.weapon.melee.ShortSword;
import com.watabou.pixeldungeon.items.weapon.melee.WoodenBow;
import com.watabou.pixeldungeon.items.weapon.missiles.Boomerang;
import com.watabou.pixeldungeon.items.weapon.missiles.CommonArrow;
import com.watabou.pixeldungeon.items.weapon.missiles.Dart;
import com.watabou.pixeldungeon.ui.QuickSlot;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.utils.Bundle;

public enum HeroClass {

	WARRIOR(Game.getVar(R.string.HeroClass_War)),
	MAGE(Game.getVar(R.string.HeroClass_Mag)),
	ROGUE(Game.getVar(R.string.HeroClass_Rog)),
	HUNTRESS(Game.getVar(R.string.HeroClass_Hun)),
	ELF(Game.getVar(R.string.HeroClass_Elf));

	private String title;

	private HeroClass( String title ) {
		this.title = title;
	}

	public static final String[] WAR_PERKS = Game.getVars(R.array.HeroClass_WarPerks);
	public static final String[] MAG_PERKS = Game.getVars(R.array.HeroClass_MagPerks);
	public static final String[] ROG_PERKS = Game.getVars(R.array.HeroClass_RogPerks);
	public static final String[] HUN_PERKS = Game.getVars(R.array.HeroClass_HunPerks);
	public static final String[] ELF_PERKS = Game.getVars(R.array.HeroClass_ElfPerks);
	
	public void initHero( Hero hero ) {
		hero.heroClass = this;
		initCommon( hero );

		switch (this) {
		case WARRIOR:
			initWarrior( hero );
			break;

		case MAGE:
			initMage( hero );
			break;
			
		case ROGUE:
			initRogue( hero );
			break;
			
		case HUNTRESS:
			initHuntress( hero );
			break;
		
		case ELF:
			initElf( hero );
			break;
		}
		
		hero.gender = getGender();
		
		if (Badges.isUnlocked( masteryBadge() )) {
			new TomeOfMastery().collect(hero);
		}
		
		hero.updateAwareness();
	}
	
	private void initElf(Hero hero) {
		(hero.belongings.armor = new ClothArmor()).identify();
		hero.collect(new WoodenBow().upgrade());
		hero.collect(new CommonArrow(50));
		
		QuickSlot.cleanStorage();
		
	}

	private static void initCommon( Hero hero ) {
		(hero.belongings.armor = new ClothArmor()).identify();
		hero.collect(new Ration());
		
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
	
	private static void initWarrior( Hero hero ) {
		hero.STR = hero.STR + 1;
		
		(hero.belongings.weapon = new ShortSword()).identify();
		hero.collect(new Dart( 8 ).identify());
		
		QuickSlot.selectItem(Dart.class,0);
		
		new PotionOfStrength().setKnown();
	}
	
	private static void initMage( Hero hero ) {	
		(hero.belongings.weapon = new Knuckles()).identify();
		
		WandOfMagicMissile wand = new WandOfMagicMissile();
		hero.collect(wand.identify());
		
		QuickSlot.selectItem(wand,0);
		
		new ScrollOfIdentify().setKnown();
	}
	
	private static void initRogue( Hero hero ) {
		(hero.belongings.weapon = new Dagger()).identify();
		(hero.belongings.ring1 = new RingOfShadows()).upgrade().identify();
		
		hero.collect(new Dart( 8 ).identify());
		
		hero.belongings.ring1.activate( hero );
		
		QuickSlot.selectItem(Dart.class,0);
		
		new ScrollOfMagicMapping().setKnown();
	}
	
	private static void initHuntress( Hero hero ) {
		
		hero.HP = (hero.HT -= 5);
		
		(hero.belongings.weapon = new Dagger()).identify();
		Boomerang boomerang = new Boomerang();
		hero.collect(boomerang.identify());
		
		hero.collect(new TomeOfMastery());
		
		QuickSlot.selectItem(boomerang,0);
	}
	
	public String title() {
		return title;
	}
	
	public static String spritesheet(Hero hero) {
		
		if(hero.subClass.name() == HeroSubClass.BERSERKER.name()){
			if(hero.inFury()) {
				return Assets.WARRIOR_BERSERK;
			}
		}
		
		switch (hero.heroClass) {
		case WARRIOR:
			return Assets.WARRIOR;
		case MAGE:
			return Assets.MAGE;
		case ROGUE:
			return Assets.ROGUE;
		case HUNTRESS:
			switch(hero.subClass) {
			case SNIPER:
				return Assets.SNIPER;
			case WARDEN:
				return Assets.WARDEN;
				
			default:
				return Assets.HUNTRESS;
			}
		case ELF:
			return Assets.ELF;
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
		}
		
		return null;
	}

	public int getGender(){
		switch (this) {
		case WARRIOR:
		case MAGE:
		case ROGUE:
			return Utils.MASCULINE;
		case HUNTRESS:
			return Utils.FEMININE;
		}
		return Utils.NEUTER;
	}
	
	private static final String CLASS = "class";
	
	public void storeInBundle( Bundle bundle ) {
		bundle.put( CLASS, toString() );
	}
	
	public static HeroClass restoreInBundle( Bundle bundle ) {
		String value = bundle.getString( CLASS );
		return value.length() > 0 ? valueOf( value ) : ROGUE;
	}

	public static boolean isSpriteSheet(String spriteKind) {
		if (spriteKind.equals(Assets.WARRIOR_BERSERK))
			return true;
		
		if (spriteKind.equals(Assets.WARRIOR))
			return true;

		if (spriteKind.equals(Assets.MAGE))
			return true;

		if (spriteKind.equals(Assets.ROGUE))
			return true;

		if (spriteKind.equals(Assets.HUNTRESS))
			return true;

		if (spriteKind.equals(Assets.SNIPER))
			return true;
		
		if (spriteKind.equals(Assets.WARDEN))
			return true;
		
		return false;
	}
}
