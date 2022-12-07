package com.nyrds.retrodungeon.mechanics.spells;

import android.support.annotation.NonNull;

import com.watabou.noosa.particles.Emitter;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.effects.particles.FlameParticle;

/**
 * Created by mike on 05.09.2017.
 * This file is part of Remixed Pixel Dungeon.
 */

public class MagicTorch extends Spell{

	MagicTorch() {
		targetingType = SpellHelper.TARGET_SELF;
		magicAffinity = SpellHelper.AFFINITY_COMMON;

		imageIndex = 0;
		duration = 80f;
		spellCost = 1;
	}

	@Override
	public boolean cast(@NonNull Char chr){
		if (super.cast(chr)){
			if (chr.isAlive()) {
				castCallback(chr);
				Buff.affect(chr, com.watabou.pixeldungeon.actors.buffs.Light.class, duration);

				Emitter emitter = chr.getSprite().centerEmitter();
				emitter.start(FlameParticle.FACTORY, 0.2f, 3);
				return true;
			}
		}
		return false;
	}
}
