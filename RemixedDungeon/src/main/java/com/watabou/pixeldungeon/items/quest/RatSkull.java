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

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.items.rings.Artifact;
import com.watabou.pixeldungeon.items.rings.ArtifactBuff;
import com.watabou.pixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.pixeldungeon.ui.BuffIndicator;

public class RatSkull extends Artifact {

	public RatSkull() {
		image = ItemSpriteSheet.SKULL;
	}

	@Override
	public boolean isIdentified() {
		return true;
	}

	@Override
	protected ArtifactBuff buff() {
		return new RatterAura();
	}

	@Override
	public String info() {
        return super.info() + "\n\n" + StringsManager.getVar(R.string.RatSkull_Info2);
	}

	public static class RatterAura extends ArtifactBuff {
		@Override
		public int icon() {
			return BuffIndicator.RAT_SKULL;
		}
	}

	@Override
	public int price() {
		return 100;
	}
}
