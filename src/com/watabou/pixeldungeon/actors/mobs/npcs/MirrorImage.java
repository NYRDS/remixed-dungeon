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

import java.util.HashSet;

import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.blobs.ToxicGas;
import com.watabou.pixeldungeon.actors.buffs.Burning;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.hero.HeroClass;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.sprites.CharSprite;
import com.watabou.pixeldungeon.sprites.MirrorSprite;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

public class MirrorImage extends NPC {
	
	{
		spriteClass = MirrorSprite.class;
		
		state = HUNTING;
		
		enemy = DUMMY;
	}
	
	public int tier;
	
	private int attack;
	private int damage;
	private String spriteKind;
	
	private static final String TIER	= "tier";
	private static final String ATTACK	= "attack";
	private static final String DAMAGE	= "damage";
	private static final String SPRITE  = "spriteKind";
	
	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( TIER, tier );
		bundle.put( ATTACK, attack );
		bundle.put( DAMAGE, damage );
		bundle.put( SPRITE, spriteKind );
	}
	
	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		tier = bundle.getInt( TIER );
		attack = bundle.getInt( ATTACK );
		damage = bundle.getInt( DAMAGE );
		spriteKind = bundle.getString( SPRITE );
		
		if(! HeroClass.isSpriteSheet(spriteKind)) {
			spriteKind = Assets.WARRIOR;
		}
	}
	
	public void duplicate( Hero hero ) {
		tier = hero.tier();
		attack = hero.attackSkill( hero );
		damage = hero.damageRoll();
		
		spriteKind = HeroClass.spritesheet(hero);
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
		
		if (enemy == DUMMY || !enemy.isAlive()) {
			HashSet<Mob> enemies = new HashSet<Mob>();
			for (Mob mob:Dungeon.level.mobs) {
				if (mob.hostile && Level.fieldOfView[mob.pos]) {
					enemies.add( mob );
				}
			}
			
			enemy = enemies.size() > 0 ? Random.element( enemies ) : DUMMY;
		}
		
		return enemy;
	}
		
	@Override
	public CharSprite sprite() {
		assert (spriteKind != null);
		MirrorSprite s = new MirrorSprite();
		s.texture(spriteKind);
		s.updateArmor(tier);
		return s;
	}

	@Override
	public void interact(final Hero hero) {
		
		int curPos = pos;
		
		moveSprite( pos, hero.pos );
		move( hero.pos );
		
		hero.getSprite().move( hero.pos, curPos );
		hero.move( curPos );
		
		hero.spend( 1 / hero.speed() );
		hero.busy();
	}
	
	private static final HashSet<Class<?>> IMMUNITIES = new HashSet<Class<?>>();
	static {
		IMMUNITIES.add( ToxicGas.class );
		IMMUNITIES.add( Burning.class );
	}
	
	@Override
	public HashSet<Class<?>> immunities() {
		return IMMUNITIES;
	}
}