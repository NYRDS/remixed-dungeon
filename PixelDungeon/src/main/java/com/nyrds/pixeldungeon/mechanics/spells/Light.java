package com.nyrds.pixeldungeon.mechanics.spells;

import com.watabou.noosa.particles.Emitter;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.blobs.Blob;
import com.watabou.pixeldungeon.actors.blobs.LiquidFlame;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.effects.particles.FlameParticle;
import com.watabou.pixeldungeon.scenes.GameScene;

/**
 * Created by mike on 05.09.2017.
 * This file is part of Remixed Pixel Dungeon.
 */

public class Light extends Spell{

	Light() {
		targetingType = SpellHelper.TARGET_SELF;
		magicAffinity = SpellHelper.AFFINITY_COMMON;

		imageIndex = 0;
		duration = 80f;
		spellCost = 1;
	}

	@Override
	public boolean cast(Char chr){
		if (super.cast(chr)){
			if (chr != null && chr.isAlive()) {
				if (chr instanceof Hero) {
					Hero hero = (Hero) chr;

					castCallback(hero);
				}
				Buff.affect(chr, com.watabou.pixeldungeon.actors.buffs.Light.class, duration);

				Emitter emitter = chr.getSprite().centerEmitter();
				emitter.start(FlameParticle.FACTORY, 0.2f, 3);
				return true;
			}
		}
		return false;
	}
}
