package com.nyrds.pixeldungeon.mobs.elementals;

import com.nyrds.pixeldungeon.mobs.common.IDepthAdjustable;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.blobs.ToxicGas;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Paralysis;
import com.watabou.pixeldungeon.actors.buffs.Roots;
import com.watabou.pixeldungeon.actors.buffs.Slow;
import com.watabou.pixeldungeon.actors.buffs.Stun;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.items.food.FrozenCarpaccio;
import com.watabou.utils.Random;

import org.jetbrains.annotations.NotNull;

public class IceElemental extends Mob implements IDepthAdjustable {

	public IceElemental() {
		adjustStats(Dungeon.depth);

		loot(new FrozenCarpaccio(), 0.1f);
	}

	public void adjustStats(int depth) {
		hp(ht(depth * 10 + 1));
		baseDefenseSkill = depth * 2 + 1;
		exp = depth + 1;
		maxLvl = depth + 2;
		dr = exp;
		baseAttackSkill = baseDefenseSkill / 3 + 1;
		dmgMin = hp()/6;
		dmgMax = hp()/6;


		addImmunity(Roots.class);
		addImmunity(Paralysis.class);
		addImmunity(Stun.class);
		addImmunity(ToxicGas.class);
	}

	@Override
	public int attackProc(@NotNull Char enemy, int damage) {
		//Buff proc
		if (Random.Int(3) == 1){
			if(enemy instanceof Hero) {
				Buff.prolong( enemy, Slow.class, 3 );
			}
		}
		return damage;
	}
}
