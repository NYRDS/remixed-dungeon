package com.nyrds.retrodungeon.mechanics.spells;

import com.watabou.noosa.audio.Sample;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Roots;
import com.watabou.pixeldungeon.effects.particles.EarthParticle;
import com.watabou.pixeldungeon.mechanics.Ballistica;

public class RootSpell extends Spell{

	RootSpell() {
		targetingType = SpellHelper.TARGET_CELL;
		magicAffinity = SpellHelper.AFFINITY_ELEMENTAL;

		level = 2;
		imageIndex = 2;
		duration = 10f;
		spellCost = 2;
	}

	@Override
	public boolean cast(Char chr, int cell){

		if(Dungeon.level.cellValid(cell)) {
			if(Ballistica.cast(chr.getPos(), cell, false, true) == cell) {
				Char ch = Actor.findChar( cell );
				if (ch != null) {
					ch.getSprite().emitter().burst( EarthParticle.FACTORY, 5 );
					ch.getSprite().burst( 0xFF99FFFF, 3 );

					Buff.prolong( ch, Roots.class, duration );
					Sample.INSTANCE.play( Assets.SND_PUFF );
				}

				castCallback(chr);
				return true;
			}
		}
		return false;
	}

	@Override
	public String texture(){
		return "spellsIcons/elemental.png";
	}
}
