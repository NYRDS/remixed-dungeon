package com.nyrds.pixeldungeon.mobs.spiders;

import com.nyrds.pixeldungeon.ai.Hunting;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.CharUtils;
import com.watabou.pixeldungeon.actors.buffs.Barkskin;
import com.watabou.pixeldungeon.actors.buffs.Blessed;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Fury;
import com.watabou.pixeldungeon.actors.buffs.Speed;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.effects.CellEmitter;
import com.watabou.pixeldungeon.effects.particles.ShaftParticle;
import com.watabou.pixeldungeon.items.food.MysteryMeat;
import com.watabou.pixeldungeon.plants.Earthroot;
import com.watabou.pixeldungeon.plants.Sungrass;
import com.watabou.utils.Random;

import org.jetbrains.annotations.NotNull;

import lombok.val;

public class SpiderMind extends Mob {

	private static final String[] BuffsForFriends = {
		Speed.class.getSimpleName(),
		Barkskin.class.getSimpleName(),
		Blessed.class.getSimpleName(),
		Sungrass.Health.class.getSimpleName(),
		Earthroot.Armor.class.getSimpleName(),
		"ManaShield",
		Fury.class.getSimpleName()
	};

	public SpiderMind() {
		hp(ht(5));
		baseDefenseSkill = 1;
		baseAttackSkill  = 10;
		baseSpeed = 1f;
		dmgMin = 0;
		dmgMax = 0;
		dr = 0;
		
		expForKill = 6;
		maxLvl = 9;
		
		loot(new MysteryMeat(), 0.067f);
	}
	
	@Override
    public boolean canAttack(@NotNull Char enemy) {
		return CharUtils.canDoOnlyRangedAttack(this, enemy);
	}
	
	@Override
	public int zapProc(@NotNull Char enemy, int damage ) {

		for (val mob : level().mobs){
			if(mob != this) {
				int mobPos = mob.getPos();
				if(level().fieldOfView[mobPos] && mob.friendly(this)) {
					Buff.prolong(mob, Random.oneOf(BuffsForFriends), 3);

					if (Dungeon.isCellVisible(mobPos)) {
						CellEmitter.get(mobPos).start(ShaftParticle.FACTORY, 0.2f, 3);
					}

					break;
				}
			}
		}

		return 0;
	}
	
	@Override
	public boolean getCloser(int target) {
		if (getState() instanceof Hunting) {
			return enemySeen && getFurther( target );
		} else {
			return super.getCloser( target );
		}
	}
}
