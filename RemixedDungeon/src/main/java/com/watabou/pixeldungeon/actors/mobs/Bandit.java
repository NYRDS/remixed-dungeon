
package com.watabou.pixeldungeon.actors.mobs;

import com.nyrds.pixeldungeon.ai.MobAi;
import com.nyrds.pixeldungeon.ai.ThiefFleeing;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.CharUtils;
import com.watabou.pixeldungeon.actors.buffs.Blindness;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.utils.Random;

import org.jetbrains.annotations.NotNull;


public class Bandit extends Thief {

	@Override
	public int attackProc(@NotNull Char enemy, int damage) {
		if (CharUtils.steal(this, enemy)) {
			setState(MobAi.getStateByClass(ThiefFleeing.class));
			Buff.prolong(getEnemy(), Blindness.class, Random.Int(5, 12));
			enemy.observe();
		}
		return damage;
	}
}
