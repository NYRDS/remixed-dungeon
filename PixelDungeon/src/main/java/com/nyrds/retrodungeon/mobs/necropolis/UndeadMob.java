package com.nyrds.retrodungeon.mobs.necropolis;

import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.blobs.ToxicGas;
import com.watabou.pixeldungeon.actors.buffs.Amok;
import com.watabou.pixeldungeon.actors.buffs.Blindness;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Burning;
import com.watabou.pixeldungeon.actors.buffs.Paralysis;
import com.watabou.pixeldungeon.actors.buffs.Poison;
import com.watabou.pixeldungeon.actors.buffs.Sleep;
import com.watabou.pixeldungeon.actors.buffs.Terror;
import com.watabou.pixeldungeon.actors.buffs.Vertigo;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.items.weapon.enchantments.Death;
import com.watabou.utils.Random;

/**
 * Created by DeadDie on 07.07.2016
 */
public class UndeadMob extends Mob {
	{
		IMMUNITIES.add(Paralysis.class);
		IMMUNITIES.add(ToxicGas.class);
		IMMUNITIES.add(Terror.class);
		IMMUNITIES.add(Death.class);
		IMMUNITIES.add(Amok.class);
		IMMUNITIES.add(Blindness.class);
		IMMUNITIES.add(Sleep.class);
		IMMUNITIES.add(Poison.class);
		IMMUNITIES.add(Vertigo.class);
	}

	@Override
	public void add(Buff buff) {
		if (!Dungeon.isLoading()) {
			if (buff instanceof Burning) {
				damage(Random.NormalIntRange(1, ht() / 8), buff);
			}
		}
		super.add(buff);
	}
}