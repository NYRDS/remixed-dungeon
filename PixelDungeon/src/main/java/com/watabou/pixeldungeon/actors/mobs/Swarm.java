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
package com.watabou.pixeldungeon.actors.mobs;

import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Burning;
import com.watabou.pixeldungeon.actors.buffs.Poison;
import com.watabou.pixeldungeon.effects.Pushing;
import com.watabou.pixeldungeon.items.potions.PotionOfHealing;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.levels.Terrain;
import com.watabou.pixeldungeon.levels.features.Door;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.sprites.SwarmSprite;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class Swarm extends Mob {

	{
		spriteClass = SwarmSprite.class;
		
		hp(ht(80));
		defenseSkill = 5;
		
		maxLvl = 10;
		
		flying = true;
	}
	
	private static final float SPLIT_DELAY	= 1f;
	
	int generation	= 0;
	
	private static final String GENERATION	= "generation";
	
	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( GENERATION, generation );
	}
	
	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		generation = bundle.getInt( GENERATION );
	}
	
	@Override
	public int damageRoll() {
		return Random.NormalIntRange( 1, 4 );
	}
	
	@Override
	public int defenseProc( Char enemy, int damage ) {

		if (hp() >= damage + 2) {
			ArrayList<Integer> candidates = new ArrayList<>();
			boolean[] passable = Dungeon.level.passable;
			
			for (int n : Level.NEIGHBOURS4) {
				int p = n + getPos();
				if (passable[p] && Actor.findChar( p ) == null) {
					candidates.add( p );
				}
			}
	
			if (candidates.size() > 0) {
				
				Swarm clone = split();
				clone.hp((hp() - damage) / 2);
				clone.setPos(Random.element( candidates ));
				clone.state = clone.HUNTING;
				
				if (Dungeon.level.map[clone.getPos()] == Terrain.DOOR) {
					Door.enter( clone.getPos() );
				}
				Dungeon.level.spawnMob(clone,SPLIT_DELAY);
				Actor.addDelayed( new Pushing( clone, getPos(), clone.getPos() ), -1 );
				
				hp(hp() - clone.hp());
			}
		}
		
		return damage;
	}
	
	@Override
	public int attackSkill( Char target ) {
		return 12;
	}
	
	private Swarm split() {
		Swarm clone = new Swarm();
		clone.generation = generation + 1;
		if (buff( Burning.class ) != null) {
			Buff.affect( clone, Burning.class ).reignite( clone );
		}
		if (buff( Poison.class ) != null) {
			Buff.affect( clone, Poison.class ).set( 2 );
		}
		
		if(isPet()) {
			Mob.makePet(clone, Dungeon.hero);
		}
		
		return clone;
	}
	
	@Override
	protected void dropLoot() {
		if (Random.Int( 5 * (generation + 1) ) == 0) {
			Dungeon.level.drop( new PotionOfHealing(), getPos() ).sprite.drop();
		}
	}

}
