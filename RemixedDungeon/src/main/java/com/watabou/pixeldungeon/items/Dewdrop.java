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

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.audio.Sample;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.sprites.CharSprite;
import com.watabou.pixeldungeon.sprites.ItemSpriteSheet;

import org.jetbrains.annotations.NotNull;

public class Dewdrop extends Item {

	private static final String TXT_VALUE	= "%+dHP";
	
	{
        name = StringsManager.getVar(R.string.Dewdrop_Name);
		image = ItemSpriteSheet.DEWDROP;
		
		stackable = true;
	}
	
	@Override
	public boolean doPickUp(@NotNull Char hero ) {
		boolean collected = false;

		DewVial vial = hero.getBelongings().getItem( DewVial.class );

		if (vial != null && !vial.isFull()) {
			vial.collectDew( this );
			collected = true;
		}

		if(!collected && hero.hp() < hero.ht()) {
			final int[] value = {1 + (Dungeon.depth - 1) / 5};

			hero.forEachBuff(b-> value[0] +=b.dewBonus());

			int effect = Math.min( hero.ht() - hero.hp(), value[0] * quantity() );
			if (effect > 0) {
				hero.heal(effect, this);
				hero.showStatus( CharSprite.POSITIVE, TXT_VALUE, effect );
				collected = true;
			}
		}

		if (collected) {
			Sample.INSTANCE.play(Assets.SND_DEWDROP);
			hero.spendAndNext(TIME_TO_PICK_UP);
		}
		
		return collected;
	}
	
	@Override
	public Item burn(int cell){
		return null;
	}

	@Override
	public boolean announcePickUp() {
		return false;
	}
}
