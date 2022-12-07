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

import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Sample;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Dungeon;
import com.nyrds.retrodungeon.ml.R;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.hero.HeroClass;
import com.watabou.pixeldungeon.actors.hero.HeroSubClass;
import com.watabou.pixeldungeon.effects.Speck;
import com.watabou.pixeldungeon.sprites.CharSprite;
import com.watabou.pixeldungeon.sprites.ItemSpriteSheet;

public class Dewdrop extends Item {

	private static final String TXT_VALUE	= "%+dHP";
	
	{
		name = Game.getVar(R.string.Dewdrop_Name);
		image = ItemSpriteSheet.DEWDROP;
		
		stackable = true;
	}
	
	@Override
	public boolean doPickUp( Hero hero ) {
		boolean collected = false;

		if(hero.hp() < hero.ht()) {
			int value = 1 + (Dungeon.depth - 1) / 5;

			if (hero.heroClass == HeroClass.HUNTRESS || hero.heroClass == HeroClass.ELF) {
				value++;
			}

			if(hero.subClass == HeroSubClass.WARDEN || hero.subClass == HeroSubClass.SHAMAN) {
				value++;
			}

			int effect = Math.min( hero.ht() - hero.hp(), value * quantity() );
			if (effect > 0) {
				hero.hp(hero.hp() + effect);
				hero.getSprite().emitter().burst( Speck.factory( Speck.HEALING ), 1 );
				hero.getSprite().showStatus( CharSprite.POSITIVE, TXT_VALUE, effect );
				collected = true;
			}
		}

		DewVial vial = hero.belongings.getItem( DewVial.class );

		if (!collected && vial != null && !vial.isFull()) {
			vial.collectDew( this );
			collected = true;
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
}
