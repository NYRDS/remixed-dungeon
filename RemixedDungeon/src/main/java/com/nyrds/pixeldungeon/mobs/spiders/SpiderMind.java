package com.nyrds.pixeldungeon.mobs.spiders;

import com.nyrds.pixeldungeon.ai.Hunting;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.CharUtils;
import com.watabou.pixeldungeon.actors.buffs.Blindness;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.FlavourBuff;
import com.watabou.pixeldungeon.actors.buffs.Slow;
import com.watabou.pixeldungeon.actors.buffs.Weakness;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.items.food.MysteryMeat;
import com.watabou.utils.Random;

import org.jetbrains.annotations.NotNull;

public class SpiderMind extends Mob {

	private static final Class<?>[] BuffsForEnemy = {
			Blindness.class,
			Slow.class,
			Weakness.class
	};

	public SpiderMind() {
		hp(ht(5));
		baseDefenseSkill = 1;
		baseAttackSkill  = 10;
		baseSpeed = 1.5f;
		
		exp = 6;
		maxLvl = 9;
		
		loot(new MysteryMeat(), 0.067f);
	}
	
	@Override
    public boolean canAttack(@NotNull Char enemy) {
		return CharUtils.canDoOnlyRangedAttack(this, enemy);
	}
	
	@Override
	public int zapProc(@NotNull Char enemy, int damage ) {
		Class <? extends FlavourBuff> buffClass = (Class<? extends FlavourBuff>) Random.oneOf(BuffsForEnemy);
		Buff.prolong( enemy, buffClass, 3 );

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
	
	@Override
	public int damageRoll() {
		return 0;
	}

	@Override
	public int dr() {
		return 0;
	}
}
