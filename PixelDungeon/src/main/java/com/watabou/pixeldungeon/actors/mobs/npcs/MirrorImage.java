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
package com.watabou.pixeldungeon.actors.mobs.npcs;

import android.support.annotation.NonNull;

import com.nyrds.Packable;
import com.nyrds.pixeldungeon.ml.EventCollector;
import com.nyrds.pixeldungeon.ai.Hunting;
import com.nyrds.pixeldungeon.ai.MobAi;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.blobs.ToxicGas;
import com.watabou.pixeldungeon.actors.buffs.Burning;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.sprites.CharSprite;
import com.watabou.pixeldungeon.sprites.HeroSpriteDef;

import java.util.HashSet;
import java.util.Set;

public class MirrorImage extends Mob {

	// for restoreFromBundle
	public MirrorImage() {
		setState(MobAi.getStateByClass(Hunting.class));
		setEnemy(DUMMY);
	}

	public MirrorImage(Hero hero) {
		this();

		makePet(this, hero);

		attack = hero.attackSkill( hero );
		damage = hero.damageRoll();

		look = hero.getHeroSprite().getLayersDesc();
	}

	@Packable
	private int                attack;
	@Packable
	private int                damage;
	@Packable
	private String[] look = new String[0];
	@Packable
	private String deathEffect;


	@Override
	public int attackSkill( Char target ) {
		return attack;
	}
	
	@Override
	public int damageRoll() {
		return damage;
	}
	
	@Override
	public int attackProc(@NonNull Char enemy, int damage ) {
		int dmg = super.attackProc( enemy, damage );

		destroy();
		getSprite().die();
		
		return dmg;
	}

	@Override
	public CharSprite sprite() {
		if(look.length > 0 && deathEffect!=null && !deathEffect.isEmpty()) {
			return new HeroSpriteDef(look, deathEffect);
		} else { // first sprite generation
			if(Dungeon.hero != null) {
				look = Dungeon.hero.getHeroSprite().getLayersDesc();
				deathEffect = Dungeon.hero.getHeroSprite().getDeathEffect();
			} else { // dirty hack here
				EventCollector.logException(new Exception("MirrorImage sprite created before hero"));
				Hero hero = new Hero();
				HeroSpriteDef spriteDef = new HeroSpriteDef(hero);
				look = spriteDef.getLayersDesc();
				deathEffect = spriteDef.getDeathEffect();
			}

			return sprite();
		}
	}

	private static final HashSet<Class<?>> IMMUNITIES = new HashSet<>();
	static {
		IMMUNITIES.add( ToxicGas.class );
		IMMUNITIES.add( Burning.class );
	}
	
	@Override
	public Set<Class<?>> immunities() {
		return IMMUNITIES;
	}
}