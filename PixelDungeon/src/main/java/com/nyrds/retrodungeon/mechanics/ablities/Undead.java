package com.nyrds.retrodungeon.mechanics.ablities;

import com.watabou.pixeldungeon.actors.blobs.ToxicGas;
import com.watabou.pixeldungeon.actors.buffs.Amok;
import com.watabou.pixeldungeon.actors.buffs.Blindness;
import com.watabou.pixeldungeon.actors.buffs.Paralysis;
import com.watabou.pixeldungeon.actors.buffs.Poison;
import com.watabou.pixeldungeon.actors.buffs.Sleep;
import com.watabou.pixeldungeon.actors.buffs.Terror;
import com.watabou.pixeldungeon.actors.buffs.Vertigo;
import com.watabou.pixeldungeon.items.weapon.enchantments.Death;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by mike on 12.02.2017.
 * This file is part of Remixed Pixel Dungeon.
 */

public class Undead implements Abilities {

	private static final Set<Class<?>> undead_immunities = new HashSet<>();

	static {
		undead_immunities.add(Paralysis.class);
		undead_immunities.add(ToxicGas.class);
		undead_immunities.add(Terror.class);
		undead_immunities.add(Death.class);
		undead_immunities.add(Amok.class);
		undead_immunities.add(Blindness.class);
		undead_immunities.add(Sleep.class);
		undead_immunities.add(Poison.class);
		undead_immunities.add(Vertigo.class);
	}


	@Override
	public Set<Class<?>> immunities() {
		return undead_immunities;
	}

	public static Undead instance = new Undead();
}
