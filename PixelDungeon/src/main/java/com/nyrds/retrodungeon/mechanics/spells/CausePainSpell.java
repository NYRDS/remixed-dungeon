package com.nyrds.retrodungeon.mechanics.spells;

import com.nyrds.retrodungeon.mechanics.buffs.Pain;
import com.watabou.noosa.audio.Sample;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.effects.particles.BloodParticle;
import com.watabou.pixeldungeon.mechanics.Ballistica;

public class CausePainSpell extends Spell{

	CausePainSpell() {
		targetingType = SpellHelper.TARGET_CELL;
		magicAffinity = SpellHelper.AFFINITY_NECROMANCY;

		level = 0;
		imageIndex = 1;
		spellCost = 0;
	}

	@Override
	public boolean cast(Char chr, int cell){
		if(Dungeon.level.cellValid(cell)) {
			if(Ballistica.cast(chr.getPos(), cell, false, true) == cell) {
				Char ch = Actor.findChar( cell );
				if (ch != null) {
					ch.getSprite().emitter().burst( BloodParticle.FACTORY, 5 );
					ch.getSprite().burst( 0xFF99FFFF, 3 );

					Buff.affect(ch, Pain.class).set(getLevelModifier(chr), getLevelModifier(chr) * 5);
					Sample.INSTANCE.play( Assets.SND_HIT );
				}
				castCallback(chr);
				return true;
			}
		}
		return false;
	}

	@Override
	public String texture(){
		return "spellsIcons/necromancy.png";
	}
}
