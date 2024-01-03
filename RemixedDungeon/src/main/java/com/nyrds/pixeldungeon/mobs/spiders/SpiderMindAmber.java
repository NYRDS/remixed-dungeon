package com.nyrds.pixeldungeon.mobs.spiders;

import com.nyrds.pixeldungeon.ai.Hunting;
import com.nyrds.pixeldungeon.mobs.common.MultiKindMob;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.CharUtils;
import com.watabou.pixeldungeon.actors.buffs.Blindness;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.FlavourBuff;
import com.watabou.pixeldungeon.actors.buffs.Slow;
import com.watabou.pixeldungeon.actors.buffs.Weakness;
import com.watabou.pixeldungeon.items.food.MysteryMeat;
import com.watabou.utils.Random;

import org.jetbrains.annotations.NotNull;

public class SpiderMindAmber extends MultiKindMob {

	private static final Class<?>[] BuffsForEnemy = {
		Blindness.class,
		Slow.class,
		Weakness.class
	};

	public SpiderMindAmber() {
		hp(ht(30));
		baseDefenseSkill = 5;
		baseAttackSkill  = 15;
		baseSpeed = 1f;
		dmgMin = 1;
		dmgMax = 1;
		dr = 10;

		expForKill = 9;
		maxLvl = 10;

		kind = 1;

		loot(new MysteryMeat(), 0.067f);
	}
	
	@Override
    public boolean canAttack(@NotNull Char enemy) {
		return CharUtils.canDoOnlyRangedAttack(this, enemy);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public int zapProc(@NotNull Char enemy, int damage ) {
		Class <? extends FlavourBuff> buffClass = (Class<? extends FlavourBuff>) Random.oneOf(BuffsForEnemy);
		Buff.prolong( enemy, buffClass, 3 );

		return damage;
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
