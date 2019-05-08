package com.nyrds.pixeldungeon.mechanics.spells;

import com.watabou.noosa.particles.Emitter;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.effects.particles.FlameParticle;

import org.jetbrains.annotations.NotNull;

/**
 * Created by mike on 05.09.2017.
 * This file is part of Remixed Pixel Dungeon.
 */

public class MagicTorch extends Spell{

	MagicTorch() {
		targetingType = SpellHelper.TARGET_SELF;
		magicAffinity = SpellHelper.AFFINITY_COMMON;

		image = 0;
		spellCost = 1;
	}

	@Override
	public boolean cast(@NotNull Char chr){
		if (super.cast(chr)){
			castCallback(chr);
			Buff.affect(chr, com.watabou.pixeldungeon.actors.buffs.Light.class, 80);

			Emitter emitter = chr.getSprite().centerEmitter();
			emitter.start(FlameParticle.FACTORY, 0.2f, 3);
			return true;
		}
		return false;
	}
}
