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
package com.watabou.pixeldungeon.items;

import com.nyrds.pixeldungeon.mechanics.NamedEntityKind;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.pixeldungeon.windows.WndResurrect;

import org.jetbrains.annotations.NotNull;

public class Ankh extends Item {

	{
		stackable = true;
        name = StringsManager.getVar(R.string.Ankh_Name);
		image = ItemSpriteSheet.ANKH;
	}

	public static boolean resurrect(@NotNull Char chr, NamedEntityKind cause) {
		Ankh ankh = chr.getBelongings().getItem(Ankh.class);

		if(ankh == null || !ankh.valid()) {
			return false;
		}

		chr.getBelongings().removeItem(ankh);

		if(! (chr instanceof Hero)) {
			chr.resurrect();
			return true;
		}

		GameScene.show(new WndResurrect(ankh, cause));
		return true;
	}

	@Override
	public boolean isUpgradable() {
		return false;
	}
	
	@Override
	public boolean isIdentified() {
		return true;
	}
	
	@Override
	public int price() {
		return 50 * quantity();
	}
}
