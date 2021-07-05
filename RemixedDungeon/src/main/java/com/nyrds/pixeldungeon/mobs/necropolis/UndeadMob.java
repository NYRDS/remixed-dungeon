package com.nyrds.pixeldungeon.mobs.necropolis;

import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.blobs.ToxicGas;
import com.watabou.pixeldungeon.actors.buffs.Amok;
import com.watabou.pixeldungeon.actors.buffs.Bleeding;
import com.watabou.pixeldungeon.actors.buffs.Blindness;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Burning;
import com.watabou.pixeldungeon.actors.buffs.Paralysis;
import com.watabou.pixeldungeon.actors.buffs.Poison;
import com.watabou.pixeldungeon.actors.buffs.Regeneration;
import com.watabou.pixeldungeon.actors.buffs.Sleep;
import com.watabou.pixeldungeon.actors.buffs.Stun;
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
		addImmunity(Paralysis.class);
		addImmunity(Stun.class);
		addImmunity(ToxicGas.class);
		addImmunity(Terror.class);
		addImmunity(Death.class);
		addImmunity(Amok.class);
		addImmunity(Blindness.class);
		addImmunity(Sleep.class);
		addImmunity(Poison.class);
		addImmunity(Vertigo.class);
		addImmunity(Bleeding.class);
		addImmunity(Regeneration.class);
	}

	@Override
	public boolean add(Buff buff) {
		if (!Dungeon.isLoading()) {
			if (buff instanceof Burning) {
				damage(Random.NormalIntRange(1, ht() / 8), buff);
			}
		}

		return super.add(buff);
    }
}