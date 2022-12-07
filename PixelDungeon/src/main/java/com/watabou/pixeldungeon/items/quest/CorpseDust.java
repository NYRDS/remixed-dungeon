/*
 * Pixel Dungeon
 * Copyright (C) 2012-2014  Oleg Dolya
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package com.watabou.pixeldungeon.items.quest;

import com.nyrds.retrodungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Sample;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.effects.particles.ShadowParticle;
import com.watabou.pixeldungeon.items.rings.Artifact;
import com.watabou.pixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.pixeldungeon.ui.BuffIndicator;

public class CorpseDust extends Artifact {

	public CorpseDust() {
		image = ItemSpriteSheet.DUST;

		cursed = true;
		cursedKnown = true;
	}

	@Override
	public boolean isIdentified() {
		return true;
	}

	@Override
	protected ArtifactBuff buff() {
		return new UndeadRageAuraBuff();
	}

	@Override
	public String info() {
		return super.info() + "\n\n" + Game.getVar(R.string.CorpseDust_Info2);
	}

	public class UndeadRageAuraBuff extends ArtifactBuff {
		@Override
		public boolean act() {
			if (target.isAlive()) {
				if (target.hp() > target.ht() / 5 && Math.random() < 0.1f) {
					target.damage((int) (Math.random() * 5), this);
					target.getSprite().emitter().burst(ShadowParticle.CURSE, 6);
					Sample.INSTANCE.play(Assets.SND_CURSED);
				}
			} else {
				deactivate();
			}
			spend(1);
			return true;
		}

		@Override
		public int icon() {
			return BuffIndicator.BLOODLUST;
		}

		@Override
		public String toString() {
			return Game.getVar(R.string.CorpseDust_Buff);
		}
	}
}
