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

import com.nyrds.android.util.JsonHelper;
import com.nyrds.android.util.ModdingMode;
import com.nyrds.pixeldungeon.items.common.armor.NecromancerArmor;
import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.CharModifier;
import com.watabou.pixeldungeon.actors.buffs.Combo;
import com.watabou.pixeldungeon.actors.buffs.SnipersMark;
import com.watabou.pixeldungeon.items.EquipableItem;
import com.watabou.pixeldungeon.items.armor.AssasinArmor;
import com.watabou.pixeldungeon.items.armor.BattleMageArmor;
import com.watabou.pixeldungeon.items.armor.BerserkArmor;
import com.watabou.pixeldungeon.items.armor.ClassArmor;
import com.watabou.pixeldungeon.items.armor.FreeRunnerArmor;
import com.watabou.pixeldungeon.items.armor.GladiatorArmor;
import com.watabou.pixeldungeon.items.armor.ScoutArmor;
import com.watabou.pixeldungeon.items.armor.ShamanArmor;
import com.watabou.pixeldungeon.items.armor.SniperArmor;
import com.watabou.pixeldungeon.items.armor.WardenArmor;
import com.watabou.pixeldungeon.items.armor.WarlockArmor;
import com.watabou.pixeldungeon.items.scrolls.ScrollOfRecharging;
import com.watabou.pixeldungeon.items.wands.Wand;
import com.watabou.pixeldungeon.items.weapon.melee.MeleeWeapon;
import com.watabou.pixeldungeon.sprites.CharSprite;
import com.watabou.pixeldungeon.ui.BuffIndicator;
import com.watabou.pixeldungeon.ui.QuickSlot;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import lombok.SneakyThrows;

public enum HeroSubClass implements CharModifier {

	NONE( null, null,ClassArmor.class),
	GLADIATOR( R.string.HeroSubClass_NameGlad,   R.string.HeroSubClass_DescGlad, GladiatorArmor.class),
	BERSERKER( R.string.HeroSubClass_NameBers,   R.string.HeroSubClass_DescBers, BerserkArmor.class),
	WARLOCK(   R.string.HeroSubClass_NameWarL,   R.string.HeroSubClass_DescWarL, WarlockArmor.class),
	BATTLEMAGE(R.string.HeroSubClass_NameBatM,   R.string.HeroSubClass_DescBatM, BattleMageArmor.class),
	ASSASSIN(  R.string.HeroSubClass_NameAssa,   R.string.HeroSubClass_DescAssa, AssasinArmor.class),
	FREERUNNER(R.string.HeroSubClass_NameFreR,   R.string.HeroSubClass_DescFreR, FreeRunnerArmor.class),
	SNIPER(    R.string.HeroSubClass_NameSnip,   R.string.HeroSubClass_DescSnip, SniperArmor.class),
	WARDEN(    R.string.HeroSubClass_NameWard,   R.string.HeroSubClass_DescWard, WardenArmor.class),
	SCOUT(     R.string.HeroSubClass_NameScout,  R.string.HeroSubClass_DescScout, ScoutArmor.class),
	SHAMAN(    R.string.HeroSubClass_NameShaman, R.string.HeroSubClass_DescShaman, ShamanArmor.class),
	LICH(      R.string.HeroSubClass_NameLich,   R.string.BlackSkullOfMastery_BecomeLichDesc, NecromancerArmor.class);

	private Integer                     titleId;
	private Integer                     descId;
	private Class<? extends ClassArmor> armorClass;

	private Set<String> immunities       = new HashSet<>();
	private Set<String> resistances      = new HashSet<>();

	HeroSubClass(Integer titleId, Integer descId, Class<? extends ClassArmor> armorClass) {
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
		return Game.getVar(titleId);
	}

	public String desc() {
		return Game.getVar(descId);
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
		return armorClass.newInstance();
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
		return damage;
	}

	@Override
	public int attackProc(Char attacker, Char defender, int damage) {
		EquipableItem wep = attacker.getActiveWeapon();

		switch (this) {
			case GLADIATOR:
				if (wep instanceof MeleeWeapon) {
					damage += Buff.affect(attacker, Combo.class).hit(defender, damage);
				}
				break;
			case BATTLEMAGE:
				if (wep instanceof Wand) {
					Wand wand = (Wand) wep;
					if (wand.curCharges() < wand.maxCharges() && damage > 0) {

						wand.curCharges(wand.curCharges() + 1);
						QuickSlot.refresh(attacker);

						ScrollOfRecharging.charge(attacker);
					}
					damage += wand.curCharges();
				}
				break;
			case SNIPER:
				if (attacker.rangedWeapon != null) {
					Buff.prolong(defender, SnipersMark.class, attacker.attackDelay() * 1.1f);
				}
				break;
			case SHAMAN:
				if (wep instanceof Wand) {
					Wand wand = (Wand) wep;
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
	public int regenerationBonus() {
		return 0;
	}

	@Override
	public void charAct() {

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
	public Object textureLarge() {
		return Assets.BUFFS_LARGE;
	}

}
