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

import com.nyrds.android.util.TrackedRuntimeException;
import com.nyrds.retrodungeon.items.common.armor.NecromancerArmor;
import com.nyrds.retrodungeon.mechanics.ablities.Abilities;
import com.nyrds.retrodungeon.mechanics.ablities.Ordinary;
import com.nyrds.retrodungeon.mechanics.ablities.Undead;
import com.nyrds.retrodungeon.ml.R;
import com.watabou.noosa.Game;
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
import com.watabou.utils.Bundle;

public enum HeroSubClass {

	NONE( null, null,ClassArmor.class, Ordinary.instance),
	GLADIATOR( Game.getVar(R.string.HeroSubClass_NameGlad),   Game.getVar(R.string.HeroSubClass_DescGlad), GladiatorArmor.class, Ordinary.instance),
	BERSERKER( Game.getVar(R.string.HeroSubClass_NameBers),   Game.getVar(R.string.HeroSubClass_DescBers), BerserkArmor.class, Ordinary.instance),
	WARLOCK(   Game.getVar(R.string.HeroSubClass_NameWarL),   Game.getVar(R.string.HeroSubClass_DescWarL), WarlockArmor.class, Ordinary.instance),
	BATTLEMAGE(Game.getVar(R.string.HeroSubClass_NameBatM),   Game.getVar(R.string.HeroSubClass_DescBatM), BattleMageArmor.class, Ordinary.instance),
	ASSASSIN(  Game.getVar(R.string.HeroSubClass_NameAssa),   Game.getVar(R.string.HeroSubClass_DescAssa), AssasinArmor.class, Ordinary.instance),
	FREERUNNER(Game.getVar(R.string.HeroSubClass_NameFreR),   Game.getVar(R.string.HeroSubClass_DescFreR), FreeRunnerArmor.class, Ordinary.instance),
	SNIPER(    Game.getVar(R.string.HeroSubClass_NameSnip),   Game.getVar(R.string.HeroSubClass_DescSnip), SniperArmor.class, Ordinary.instance),
	WARDEN(    Game.getVar(R.string.HeroSubClass_NameWard),   Game.getVar(R.string.HeroSubClass_DescWard), WardenArmor.class, Ordinary.instance),
	SCOUT(     Game.getVar(R.string.HeroSubClass_NameScout),  Game.getVar(R.string.HeroSubClass_DescScout), ScoutArmor.class, Ordinary.instance),
	SHAMAN(    Game.getVar(R.string.HeroSubClass_NameShaman), Game.getVar(R.string.HeroSubClass_DescShaman), ShamanArmor.class, Ordinary.instance),
	LICH(      Game.getVar(R.string.HeroSubClass_NameLich), Game.getVar(R.string.BlackSkullOfMastery_BecomeLichDesc), NecromancerArmor.class, Undead.instance);

	private String                      title;
	private String                      desc;
	private Class<? extends ClassArmor> armorClass;
	private Abilities                   abilities;

	HeroSubClass(String title, String desc, Class<? extends ClassArmor> armorClass, Abilities abilities) {
		this.title = title;
		this.desc  = desc;
		this.armorClass = armorClass;
		this.abilities = abilities;
	}

	public String title() {
		return title;
	}

	public String desc() {
		return desc;
	}

	private static final String SUBCLASS = "subClass";

	public void storeInBundle( Bundle bundle ) {
		bundle.put( SUBCLASS, toString() );
	}

	public static HeroSubClass restoreFromBundle(Bundle bundle) {
		String value = bundle.getString( SUBCLASS );
		try {
			return valueOf( value );
		} catch (Exception e) {
			return NONE;
		}
	}

	public ClassArmor classArmor() {
		try {
			return armorClass.newInstance();
		} catch (Exception e) {
			throw new TrackedRuntimeException(e);
		}
	}

	public Abilities getAbilities() {
		return abilities;
	}
}
