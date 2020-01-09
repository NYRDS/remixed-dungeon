package com.nyrds.pixeldungeon.mechanics.spells;

import com.nyrds.pixeldungeon.ml.EventCollector;
import com.watabou.noosa.audio.Sample;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.effects.particles.WindParticle;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.mechanics.Ballistica;

public class WindGust extends Spell{

	public WindGust() {
		targetingType = SpellHelper.TARGET_CELL;
		magicAffinity = SpellHelper.AFFINITY_ELEMENTAL;

		level = 1;
		image = 0;
		spellCost = 1;
	}

	@Override
	public boolean cast(Char chr, int cell)  {
		Level level = chr.level();

		if (level.cellValid(cell)) {
			Char ch;
			boolean triggered = false;

			Ballistica.cast(chr.getPos(), cell, true, false);
			for (int i = 1; i < Math.min(chr.skillLevel() + 2, Ballistica.distance-1); i++) {
				int c = Ballistica.trace[i];

				if ((ch = Actor.findChar(c)) != null) {
					int next = Ballistica.trace[i + 1];

					if(!level.cellValid(next)) {
						EventCollector.logException("invalid cell");
					}

					triggered = true;

					ch.getSprite().emitter().burst( WindParticle.FACTORY, 5 + chr.skillLevel() * 2);
					ch.getSprite().burst( 0xFF99FFFF, 3 + chr.skillLevel());
					Sample.INSTANCE.play( Assets.SND_MELD );

					if (ch.isMovable() && (level.passable[next] || level.avoid[next])
							&& Actor.findChar(next) == null) {
						ch.move(next);
						ch.getSprite().move(ch.getPos(), next);
						Dungeon.observe();
					} else {
						ch.damage(chr.skillLevel() * 2 , this);
					}
				}

				if (triggered) {
					castCallback(chr);
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public String texture(){
		return "spellsIcons/elemental.png";
	}
}
