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
package com.watabou.pixeldungeon.actors.hero;

import com.nyrds.pixeldungeon.items.common.ItemFactory;
import com.nyrds.pixeldungeon.mechanics.NamedEntityKind;
import com.nyrds.pixeldungeon.mechanics.spells.Spell;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.nyrds.util.JsonHelper;
import com.nyrds.util.ModdingMode;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.CharModifier;
import com.watabou.pixeldungeon.actors.buffs.Combo;
import com.watabou.pixeldungeon.actors.buffs.SnipersMark;
import com.watabou.pixeldungeon.items.EquipableItem;
import com.watabou.pixeldungeon.items.armor.ClassArmor;
import com.watabou.pixeldungeon.items.scrolls.ScrollOfRecharging;
import com.watabou.pixeldungeon.items.wands.Wand;
import com.watabou.pixeldungeon.items.weapon.melee.MeleeWeapon;
import com.watabou.pixeldungeon.sprites.CharSprite;
import com.watabou.pixeldungeon.ui.BuffIndicator;
import com.watabou.pixeldungeon.ui.QuickSlot;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import clone.org.json.JSONException;
import clone.org.json.JSONObject;
import lombok.SneakyThrows;

public enum HeroSubClass implements CharModifier {

	NONE( null, null,"ClassArmor"),
	GLADIATOR( R.string.HeroSubClass_NameGlad,   R.string.HeroSubClass_DescGlad, "GladiatorArmor"),
	BERSERKER( R.string.HeroSubClass_NameBers,   R.string.HeroSubClass_DescBers, "BerserkArmor"),
	WARLOCK(   R.string.HeroSubClass_NameWarL,   R.string.HeroSubClass_DescWarL, "WarlockArmor"),
	BATTLEMAGE(R.string.HeroSubClass_NameBatM,   R.string.HeroSubClass_DescBatM, "BattleMageArmor"),
	ASSASSIN(  R.string.HeroSubClass_NameAssa,   R.string.HeroSubClass_DescAssa, "AssasinArmor"),
	FREERUNNER(R.string.HeroSubClass_NameFreR,   R.string.HeroSubClass_DescFreR, "FreeRunnerArmor"),
	SNIPER(    R.string.HeroSubClass_NameSnip,   R.string.HeroSubClass_DescSnip, "SniperArmor"),
	WARDEN(    R.string.HeroSubClass_NameWard,   R.string.HeroSubClass_DescWard, "WardenArmor"),
	SCOUT(     R.string.HeroSubClass_NameScout,  R.string.HeroSubClass_DescScout, "ScoutArmor"),
	SHAMAN(    R.string.HeroSubClass_NameShaman, R.string.HeroSubClass_DescShaman, "ShamanArmor"),
	LICH(      R.string.HeroSubClass_NameLich,   R.string.BlackSkullOfMastery_BecomeLichDesc, "NecromancerArmor"),
	WITCHDOCTOR(R.string.HeroSubClass_NameWitchdoctor,   R.string.HeroSubClass_DescWitchdoctor, "WitchdoctorArmor"),
	GUARDIAN(R.string.HeroSubClass_NameGuardian,   R.string.HeroSubClass_DescGuardian, "GuardianArmor");

	private final Integer                     titleId;
	private final Integer                     descId;
	private final String                      armorClass;

	private final Set<String> immunities       = new HashSet<>();
	private final Set<String> resistances      = new HashSet<>();

	HeroSubClass(Integer titleId, Integer descId, String armorClass) {
		this.titleId = titleId;
		this.descId  = descId;
		this.armorClass = armorClass;

		try {
			if (HeroClass.initHeroes.has(name())) {
				JSONObject classDesc = HeroClass.initHeroes.getJSONObject(name());
				JsonHelper.readStringSet(classDesc, Char.IMMUNITIES,immunities);
				JsonHelper.readStringSet(classDesc, Char.RESISTANCES,resistances);
			}
		} catch (JSONException e) {
			throw ModdingMode.modException("bad InitHero.json",e);
		}
	}

	public String title() {
        return StringsManager.getVar(titleId);
    }

	public String desc() {
        return StringsManager.getVar(descId);
    }

	private static final String SUBCLASS = "subClass";

	public void storeInBundle( Bundle bundle ) {
		bundle.put( SUBCLASS, toString() );
		bundle.put(toString()+Char.IMMUNITIES,immunities.toArray(new String[0]));
		bundle.put(toString()+Char.RESISTANCES,resistances.toArray(new String[0]));
	}

	public static HeroSubClass restoreFromBundle(Bundle bundle) {
		HeroSubClass ret;
		String value = bundle.getString( SUBCLASS );

		try {
			ret = valueOf( value );
		} catch (Exception e) {
			ret = NONE;
		}

		Collections.addAll(ret.immunities,bundle.getStringArray(value+Char.IMMUNITIES));
		Collections.addAll(ret.resistances,bundle.getStringArray(value+Char.RESISTANCES));
		return ret;
	}

	@SneakyThrows
	public ClassArmor classArmor() {
		return (ClassArmor) ItemFactory.itemByName(armorClass);
	}

	@Override
	public int drBonus() {
		return 0;
	}

	@Override
	public int stealthBonus() {
		return 0;
	}

	@Override
	public float speedMultiplier() {
		return 1;
	}

	@Override
	public int defenceProc(Char defender, Char enemy, int damage) {
		EquipableItem primaryItem =  defender.getItemFromSlot(Belongings.Slot.WEAPON);
		EquipableItem secondaryItem = defender.getItemFromSlot(Belongings.Slot.LEFT_HAND);

		switch (this) {
			case GUARDIAN:
				final int skillLevel = defender.skillLevel();
				if(primaryItem.getEntityKind().contains("Shield") || secondaryItem.getEntityKind().contains("Shield")) {
					enemy.damage((int) (Random.Float(1, skillLevel+1)/(skillLevel+3) * damage), defender);
				}
				break;
			default:
		}

		return Math.max(damage,0);
	}

	@Override
	public int attackProc(Char attacker, Char defender, int damage) {
		EquipableItem primaryItem = attacker.getActiveWeapon();
		EquipableItem secondaryItem = attacker.getItemFromSlot(Belongings.Slot.LEFT_HAND);

		switch (this) {
			case GLADIATOR:
				if (primaryItem instanceof MeleeWeapon) {
					damage += Buff.affect(attacker, Combo.class).hit(defender, damage);
				}
				break;
			case BATTLEMAGE:
				if (primaryItem instanceof Wand) {
					Wand wand = (Wand) primaryItem;
					if (wand.curCharges() < wand.maxCharges() && damage > 0) {

						wand.curCharges(wand.curCharges() + 1);
						QuickSlot.refresh(attacker);

						ScrollOfRecharging.charge(attacker);
					}
					damage += wand.curCharges();
				}
				break;
			case SNIPER:
				if (attacker.rangedWeapon.valid()) {
					Buff.prolong(defender, SnipersMark.class, attacker.attackDelay() * 1.1f);
				}
				break;
			case SHAMAN:
				if (primaryItem instanceof Wand) {
					Wand wand = (Wand) primaryItem;
					if (wand.affectTarget()) {
						if (Random.Int(4) == 0) {
							wand.zapCell(attacker, defender.getPos());
						}
					}
				}
				break;
			default:
		}

		return damage;
	}

	@Override
	public int charGotDamage(int damage, NamedEntityKind src) {
		return damage;
	}

	@Override
	public int regenerationBonus() {
		return 0;
	}

	@Override
	public int manaRegenerationBonus() {
		return 0;
	}

	@Override
	public void charAct() { }

	@Override
	public void spellCasted(Char caster, Spell spell) {
		switch (this) {
			case WITCHDOCTOR:
				Buff.affect(caster, "ManaShield", caster.skillLevel());
				break;
			default:
		}
	}

	@Override
	public int dewBonus() {
		switch (this) {
			case WARDEN:
			case SHAMAN:
				return 1;
			default:
				return 0;
		}
	}

	@Override
	public Set<String> resistances() {
		return resistances;
	}

	@Override
	public Set<String> immunities() {
		return immunities;
	}

	@Override
	public CharSprite.State charSpriteStatus() {
		return CharSprite.State.NONE;
	}

	@Override
	public int icon() {
		return BuffIndicator.NONE;
	}

	@Override
	public String textureSmall() {
		return Assets.BUFFS_SMALL;
	}

	@Override
	public String textureLarge() {
		return Assets.BUFFS_LARGE;
	}

}
