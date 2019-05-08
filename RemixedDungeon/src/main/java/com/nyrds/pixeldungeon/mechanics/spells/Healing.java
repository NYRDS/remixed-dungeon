package com.nyrds.pixeldungeon.mechanics.spells;

import com.watabou.pixeldungeon.actors.Char;

import org.jetbrains.annotations.NotNull;

/**
 * Created by mike on 05.09.2017.
 * This file is part of Remixed Pixel Dungeon.
 */

public class Healing extends Spell{

	Healing() {
		targetingType = SpellHelper.TARGET_SELF;
		magicAffinity = SpellHelper.AFFINITY_COMMON;

		level = 3;
		image = 1;
		spellCost = 10;
	}

	@Override
	public boolean cast(@NotNull Char chr){
		if (super.cast(chr)){
			if (chr.isAlive()) {
				castCallback(chr);
				chr.heal((int) (chr.ht()*0.3), this);
				return true;
			}
		}
		return false;
	}
}
