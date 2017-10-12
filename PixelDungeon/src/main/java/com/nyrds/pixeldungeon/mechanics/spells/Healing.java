package com.nyrds.pixeldungeon.mechanics.spells;

import com.watabou.noosa.particles.Emitter;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.effects.Speck;
import com.watabou.pixeldungeon.effects.particles.FlameParticle;

/**
 * Created by mike on 05.09.2017.
 * This file is part of Remixed Pixel Dungeon.
 */

public class Healing extends Spell{

	Healing() {
		targetingType = SpellHelper.TARGET_SELF;
		magicAffinity = SpellHelper.AFFINITY_COMMON;

		level = 3;
		imageIndex = 1;
		spellCost = 5;
	}

	@Override
	public boolean cast(Char chr){
		if (super.cast(chr)){
			if (chr != null && chr.isAlive()) {
				if (chr instanceof Hero) {
					Hero hero = (Hero) chr;

					castCallback(hero);
				}
				chr.hp((int) Math.min(chr.ht(),chr.hp()+chr.ht()*0.3));
				chr.getSprite().emitter().start( Speck.factory( Speck.HEALING ), 0.4f, 4 );

				return true;
			}
		}
		return false;
	}
}
