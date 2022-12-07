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
import com.watabou.pixeldungeon.items.rings.Artifact;
import com.watabou.pixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.pixeldungeon.ui.BuffIndicator;

public class DriedRose extends Artifact {

	public DriedRose() {
		image = ItemSpriteSheet.ROSE;
	}

	@Override
	protected ArtifactBuff buff() {
		if (!cursed) {
			return new OneWayLoveBuff();
		} else {
			return new OneWayCursedLoveBuff();
		}
	}

	@Override
	public boolean isIdentified() {
		return true;
	}

	@Override
	public String info() {
		return super.info() + "\n\n" + Game.getVar(R.string.DriedRose_Info2);
	}

	public class OneWayLoveBuff extends ArtifactBuff {
		@Override
		public int icon() {
			return BuffIndicator.ROSE;
		}

		@Override
		public String toString() {
			return Game.getVar(R.string.DriedRose_Buff);
		}
	}

	public class OneWayCursedLoveBuff extends ArtifactBuff {
		@Override
		public int icon() {
			return BuffIndicator.CURSED_ROSE;
		}

		@Override
		public String toString() {
			return Game.getVar(R.string.DriedRose_CursedBuff);
		}
	}
}
