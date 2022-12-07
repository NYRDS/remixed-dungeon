package com.nyrds.retrodungeon.mobs.elementals;

import android.support.annotation.NonNull;

import com.nyrds.retrodungeon.mobs.common.IDepthAdjustable;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.blobs.ToxicGas;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Paralysis;
import com.watabou.pixeldungeon.actors.buffs.Roots;
import com.watabou.pixeldungeon.actors.buffs.Slow;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.items.food.FrozenCarpaccio;
import com.watabou.utils.Random;

public class IceElemental extends Mob implements IDepthAdjustable {

	public IceElemental() {
		adjustStats(Dungeon.depth);

		loot = new FrozenCarpaccio();
		lootChance = 0.1f;
	}

	public void adjustStats(int depth) {
		hp(ht(depth * 10 + 1));
		defenseSkill = depth * 2 + 1;
		exp = depth + 1;
		maxLvl = depth + 2;
		
		IMMUNITIES.add(Roots.class);
		IMMUNITIES.add(Paralysis.class);
		IMMUNITIES.add(ToxicGas.class);
	}

	@Override
	public int damageRoll() {
		return Random.NormalIntRange(hp() / 6, ht() / 6);
	}

	@Override
	public int attackSkill(Char target) {
		return defenseSkill / 3;
	}

	@Override
	public int dr() {
		return exp;
	}

	@Override
	public int attackProc(@NonNull Char enemy, int damage) {
		//Buff proc
		if (Random.Int(3) == 1){
			if(enemy instanceof Hero) {
				Buff.prolong( enemy, Slow.class, 3 );
			}
		}
		return damage;
	}
}
