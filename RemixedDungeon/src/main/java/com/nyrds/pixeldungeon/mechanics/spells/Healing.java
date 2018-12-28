package com.nyrds.pixeldungeon.mechanics.spells;

import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.effects.Speck;

import androidx.annotation.NonNull;

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
	public boolean cast(@NonNull Char chr){
		if (super.cast(chr)){
			if (chr.isAlive()) {
				castCallback(chr);
				chr.hp((int) Math.min(chr.ht(),chr.hp()+chr.ht()*0.3));
				chr.getSprite().emitter().start( Speck.factory( Speck.HEALING ), 0.4f, 4 );
				return true;
			}
		}
		return false;
	}
}
