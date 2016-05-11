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

import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.blobs.ToxicGas;
import com.watabou.pixeldungeon.actors.buffs.Burning;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.sprites.CharSprite;
import com.watabou.pixeldungeon.sprites.HeroSpriteDef;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import java.util.HashSet;
import java.util.Set;

public class MirrorImage extends NPC {

	// for restoreFromBundle
	public MirrorImage() {
		state = HUNTING;
		setEnemy(DUMMY);
	}

	public MirrorImage(Hero hero) {
		this();
		attack = hero.attackSkill( hero );
		damage = hero.damageRoll();

		lookDesc = hero.getHeroSprite().getLayersDesc();
	}

	private int                attack;
	private int                damage;
	private String[]           lookDesc;

	private static final String ATTACK	= "attack";
	private static final String DAMAGE	= "damage";
	private static final String LOOK    = "look";


	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( ATTACK, attack );
		bundle.put( DAMAGE, damage );
		bundle.put(LOOK, lookDesc);
	}
	
	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		attack = bundle.getInt( ATTACK );
		damage = bundle.getInt( DAMAGE );
		lookDesc = bundle.getStringArray(LOOK);
	}
	
	@Override
	public int attackSkill( Char target ) {
		return attack;
	}
	
	@Override
	public int damageRoll() {
		return damage;
	}
	
	@Override
	public int attackProc( Char enemy, int damage ) {
		int dmg = super.attackProc( enemy, damage );

		destroy();
		getSprite().die();
		
		return dmg;
	}
	
	protected Char chooseEnemy() {
		
		if (getEnemy() == DUMMY || !getEnemy().isAlive()) {
			HashSet<Mob> enemies = new HashSet<>();
			for (Mob mob:Dungeon.level.mobs) {
				if (mob.hostile && Dungeon.level.fieldOfView[mob.getPos()] && !mob.isPet()) {
					enemies.add( mob );
				}
			}
			
			setEnemy(enemies.size() > 0 ? Random.element( enemies ) : DUMMY);
		}
		
		return getEnemy();
	}
		
	@Override
	public CharSprite sprite() {
		if(lookDesc!=null) {
			return new HeroSpriteDef(lookDesc);
		} else { // handle old saves
			if(Dungeon.hero != null) {
				lookDesc = Dungeon.hero.getHeroSprite().getLayersDesc();
			} else { // dirty hack here
				Hero hero = new Hero();
				lookDesc = new HeroSpriteDef(hero, false).getLayersDesc();
			}

			return sprite();
		}
	}

	@Override
	public boolean interact(final Hero hero) {
		
		int curPos = getPos();
		
		moveSprite( getPos(), hero.getPos() );
		move( hero.getPos() );
		
		hero.getSprite().move( hero.getPos(), curPos );
		hero.move( curPos );
		
		hero.spend( 1 / hero.speed() );
		hero.busy();
		
		return true;
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