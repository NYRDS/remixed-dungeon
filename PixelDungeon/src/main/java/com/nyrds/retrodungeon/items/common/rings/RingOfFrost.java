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
package com.nyrds.retrodungeon.items.common.rings;

import com.nyrds.retrodungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.items.rings.Artifact;
import com.watabou.pixeldungeon.sprites.ItemSprite;
import com.watabou.pixeldungeon.ui.BuffIndicator;

public class RingOfFrost extends Artifact {

	public RingOfFrost() {
		imageFile = "items/rings.png";
		image = 13;
		identify();
	}

	@Override
	public ItemSprite.Glowing glowing() {
		return new ItemSprite.Glowing( 0x00FFFF );
	}

	@Override
	protected ArtifactBuff buff( ) {
		return new FrostAura();
	}

	@Override
	public boolean isUpgradable() {
		return true;
	}

	public class FrostAura extends ArtifactBuff {
		@Override
		public int icon() {
			return BuffIndicator.FROSTAURA;
		}

		@Override
		public String toString() {
			return Game.getVar(R.string.FrostAura_Buff);
		}
	}
}
