
package com.watabou.pixeldungeon.actors.buffs;

import com.nyrds.Packable;
import com.nyrds.pixeldungeon.game.GameLoop;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.Badges;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.ResultDescriptions;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.hero.Doom;
import com.watabou.pixeldungeon.actors.hero.HeroClass;
import com.watabou.pixeldungeon.items.rings.RingOfSatiety;
import com.watabou.pixeldungeon.ui.BuffIndicator;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.utils.GameMath;
import com.watabou.utils.Random;

import org.jetbrains.annotations.NotNull;

public class Hunger extends Buff implements Doom {

	private static final float STEP	= 10f;
	
	public static final float HUNGRY	= 280f;
	public static final float STARVING	= 400f;

	@Packable
	private float hungerLevel;

	@Override
	public boolean act() {
		if (target.isAlive()) {

			int difficulty = GameLoop.getDifficulty();

			if (!target.level().isSafe() && isStarving()) {


				if (Random.Float() < 0.3f && (target.hp() > 1 || !target.paralysed)) {

					if(target==Dungeon.hero) {
						GLog.n(StringsManager.getVars(R.array.Hunger_Starving)[target.getGender()]);
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

				float delta = Math.max(STEP - bonus, 1);

				delta *= GameLoop.getDifficultyFactor() / 1.5f;

				if(target.level().isSafe()){
					delta = 0;
				}
				
				float newLevel = hungerLevel + delta;

				if(target==Dungeon.hero) {
					boolean statusUpdated = false;
					if (newLevel >= STARVING) {

						GLog.n(StringsManager.getVars(R.array.Hunger_Starving)[target.getGender()]);
						statusUpdated = true;

					} else if (newLevel >= HUNGRY && hungerLevel < HUNGRY) {
						GLog.w(StringsManager.getVars(R.array.Hunger_Hungry)[target.getGender()]);
						statusUpdated = true;

					}

					hungerLevel = GameMath.gate(0, newLevel, STARVING);

					if (statusUpdated) {
						target.buffsUpdated();
					}
				}
				
			}
			
			float step = target.getHeroClass() == HeroClass.ROGUE ? STEP * 1.2f : STEP;
			step *= target.hasBuff(Shadows.class) ? 1.5f : 1;
			step *= Dungeon.realtime() ? 10f : 1;

			spend( step );
		} else {
			deactivateActor();
		}
		
		return true;
	}
	
	public void satisfy( float energy ) {
		hungerLevel -= energy;

		hungerLevel = GameMath.gate(0, hungerLevel, STARVING);

		target.buffsUpdated();
	}
	
	public boolean isStarving() {
		return hungerLevel >= STARVING;
	}
	
	@Override
	public int icon() {
		if (hungerLevel < HUNGRY) {
			return BuffIndicator.NONE;
		} else if (hungerLevel < STARVING) {
			return BuffIndicator.HUNGER;
		} else {
			return BuffIndicator.STARVATION;
		}
	}
	
	@Override
	public String name() {
		if (hungerLevel < STARVING) {
            return StringsManager.getVar(R.string.HungerBuff_Name1);
        } else {
            return StringsManager.getVar(R.string.HungerBuff_Name2);
        }
	}

	@Override
	public String desc() {
		if (hungerLevel < STARVING) {
            return StringsManager.getVar(R.string.HungerBuff_Info1);
        } else {
            return StringsManager.getVar(R.string.HungerBuff_Info2);
        }
	}

	@Override
	public boolean attachTo(@NotNull Char target ) {
		return target.hasBuff(Hunger.class) || super.attachTo(target);
	}

	@Override
	public void onHeroDeath() {
		
		Badges.validateDeathFromHunger();
		
		Dungeon.fail( Utils.format( ResultDescriptions.getDescription(ResultDescriptions.Reason.HUNGER), Dungeon.depth ) );
        GLog.n(StringsManager.getVar(R.string.Hunger_Death));
	}

	public float getHungerLevel() {
		return hungerLevel;
	}
}
