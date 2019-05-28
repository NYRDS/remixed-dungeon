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
package com.watabou.pixeldungeon.actors.buffs;

import com.nyrds.Packable;
import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.Badges;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.RemixedDungeon;
import com.watabou.pixeldungeon.ResultDescriptions;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.hero.HeroClass;
import com.watabou.pixeldungeon.items.rings.RingOfSatiety;
import com.watabou.pixeldungeon.ui.BuffIndicator;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.utils.GameMath;
import com.watabou.utils.Random;

public class Hunger extends Buff implements Hero.Doom {

	private static final float STEP	= 10f;
	
	public static final float HUNGRY	= 250f;
	public static final float STARVING	= 400f;

	@Packable
	private float level;

	@Override
	public boolean act() {
		if (target.isAlive()) {

			int difficulty = Dungeon.hero.getDifficulty();

			if (!Dungeon.level.isSafe() && isStarving()) {

				if (Random.Float() < 0.3f && (target.hp() > 1 || !target.paralysed)) {

					if(target==Dungeon.hero) {
						GLog.n(Game.getVars(R.array.Hunger_Starving)[target.getGender()]);
					}

					if(difficulty >= 3) {
						target.damage(Math.max(target.effectiveSTR() - 10, 1), this);
					} else {
						target.damage( 1, this );
					}
				}
				
				if(difficulty >= 3) {
					if(Random.Float() < 0.01) {
						Buff.prolong(target, Weakness.class, Weakness.duration(target));
					}
					
					if(Random.Float() < 0.01) {
						Buff.prolong(target, Vertigo.class, Vertigo.duration(target));
					}
				}
				
			} else {	
				
				int bonus = target.buffLevel(RingOfSatiety.Satiety.class);

				float delta = Math.min(STEP - bonus, 1);

				delta *= RemixedDungeon.getDifficultyFactor() / 1.5f;

				if(Dungeon.level.isSafe()){
					delta = 0;
				}
				
				float newLevel = level + delta;

				if(target==Dungeon.hero) {
					boolean statusUpdated = false;
					if (newLevel >= STARVING) {

						GLog.n(Game.getVars(R.array.Hunger_Starving)[target.getGender()]);
						statusUpdated = true;

					} else if (newLevel >= HUNGRY && level < HUNGRY) {
						GLog.w(Game.getVars(R.array.Hunger_Hungry)[target.getGender()]);
						statusUpdated = true;

					}

					level = GameMath.gate(0, newLevel, STARVING);

					if (statusUpdated) {
						BuffIndicator.refreshHero();
					}
				}
				
			}
			
			float step = target.getHeroClass() == HeroClass.ROGUE ? STEP * 1.2f : STEP;
			step *= target.hasBuff(Shadows.class) ? 1.5f : 1;
			step *= Dungeon.realtime() ? 10f : 1;

			spend( step );
			
		} else {
			deactivate();
		}
		
		return true;
	}
	
	public void satisfy( float energy ) {
		level -= energy;

		level = GameMath.gate(0, level, STARVING);

		BuffIndicator.refreshHero();
	}
	
	public boolean isStarving() {
		return level >= STARVING;
	}
	
	@Override
	public int icon() {
		if (level < HUNGRY) {
			return BuffIndicator.NONE;
		} else if (level < STARVING) {
			return BuffIndicator.HUNGER;
		} else {
			return BuffIndicator.STARVATION;
		}
	}
	
	@Override
	public String name() {
		if (level < STARVING) {
			return Game.getVar(R.string.Hunger_Info1);
		} else {
			return Game.getVar(R.string.Hunger_Info2);
		}
	}

	@Override
	public boolean attachTo( Char target ) {
		return target.hasBuff(Hunger.class) || super.attachTo(target);
	}

	@Override
	public void onDeath() {
		
		Badges.validateDeathFromHunger();
		
		Dungeon.fail( Utils.format( ResultDescriptions.getDescription(ResultDescriptions.Reason.HUNGER), Dungeon.depth ) );
		GLog.n( Game.getVar(R.string.Hunger_Death) );
	}

	public float getLevel() {
		return level;
	}
}
