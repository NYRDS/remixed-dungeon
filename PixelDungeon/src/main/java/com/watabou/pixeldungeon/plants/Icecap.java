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
package com.watabou.pixeldungeon.plants;

import com.nyrds.retrodungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.blobs.Fire;
import com.watabou.pixeldungeon.actors.blobs.Freezing;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Frost;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.effects.Speck;
import com.watabou.pixeldungeon.items.food.Food;
import com.watabou.pixeldungeon.items.potions.PotionOfFrost;
import com.watabou.pixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.pixeldungeon.utils.BArray;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

public class Icecap extends Plant {

	private static final String TXT_NAME = Game.getVar(R.string.Icecap_Name);
	private static final String TXT_DESC = Game.getVar(R.string.Icecap_Desc);
	
	public Icecap() {
		image = 1;
		plantName = TXT_NAME;
	}
	
	@Override
	public void effect(int pos, Char ch ) {
		PathFinder.buildDistanceMap( pos, BArray.not( Dungeon.level.losBlocking, null ), 1 );
		
		Fire fire = (Fire)Dungeon.level.blobs.get( Fire.class );
		
		for (int i=0; i < Dungeon.level.getLength(); i++) {
			if (PathFinder.distance[i] < Integer.MAX_VALUE) {

				Freezing.affect( i, fire );
			}
		}
	}
	
	@Override
	public String desc() {
		return TXT_DESC;
	}
	
	public static class Seed extends Plant.Seed {
		{
			plantName = TXT_NAME;
			
			name = Utils.format(TXT_SEED, plantName);
			image = ItemSpriteSheet.SEED_ICECAP;
			
			plantClass = Icecap.class;
			alchemyClass = PotionOfFrost.class;
		}
		
		@Override
		public String desc() {
			return TXT_DESC;
		}
		
		@Override
		public void execute( Hero hero, String action ) {
			
			super.execute( hero, action );
			
			if (action.equals( Food.AC_EAT )) {
				
				Buff.prolong( hero, Frost.class, Frost.duration( hero ) * 2);
				
				hero.hp(hero.hp() + Random.Int(0, Math.max((hero.ht() - hero.hp()) / 4, 10) ));
				if (hero.hp() > hero.ht()) {
					hero.hp(hero.ht());
				}
				hero.getSprite().emitter().start( Speck.factory( Speck.HEALING ), 0.4f, 4 );
			}
		}
	}
}
