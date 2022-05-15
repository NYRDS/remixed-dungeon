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
package com.watabou.pixeldungeon.actors.mobs.npcs;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.actors.CharUtils;
import com.watabou.pixeldungeon.sprites.ImpSprite;
import com.watabou.pixeldungeon.utils.Utils;

public class ImpShopkeeper extends Shopkeeper {

	{
		spriteClass = ImpSprite.class;
	}
	
	private boolean seenBefore = false;
	
	@Override
    public boolean act() {

		if (!seenBefore && CharUtils.isVisible(this)) {
            say( Utils.format(R.string.ImpShopkeeper_Greetings) );
			seenBefore = true;
		}
		
		return super.act();
	}
	
}
