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

import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.Dungeon;
import com.nyrds.pixeldungeon.ml.R;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Vertigo;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.effects.CellEmitter;
import com.watabou.pixeldungeon.effects.Speck;
import com.watabou.pixeldungeon.items.food.Food;
import com.watabou.pixeldungeon.items.potions.PotionOfMindVision;
import com.watabou.pixeldungeon.items.scrolls.ScrollOfTeleportation;
import com.watabou.pixeldungeon.sprites.ItemSpriteSheet;

public class Fadeleaf extends Plant {

	private static final String TXT_NAME = Game.getVar(R.string.Fadeleaf_Name);
	private static final String TXT_DESC = Game.getVar(R.string.Fadeleaf_Desc);
	
	{
		image = 6;
		plantName = TXT_NAME;
	}
	
	@Override
	public void activate( Char ch ) {
		super.activate( ch );
		
		if (ch instanceof Hero) {
			
			ScrollOfTeleportation.teleportHero( (Hero)ch );
			((Hero)ch).curAction = null;
			
		} else if (ch instanceof Mob) {
			
			// Why do I try to choose a new position 10 times?
			// I don't remember...
			int count = 10;
			int newPos;
			do {
				newPos = Dungeon.level.randomRespawnCell();
				if (count-- <= 0) {
					break;
				}
			} while (newPos == -1);
			
			if (newPos != -1) {
			
				ch.pos = newPos;
				ch.getSprite().place( ch.pos );
				ch.getSprite().visible = Dungeon.visible[pos];
				
			}
						
		}
		
		if (Dungeon.visible[pos]) {
			CellEmitter.get( pos ).start( Speck.factory( Speck.LIGHT ), 0.2f, 3 );
		}
	}
	
	@Override
	public String desc() {
		return TXT_DESC;
	}
	
	public static class Seed extends Plant.Seed {
		{
			plantName = TXT_NAME;
			
			name = String.format(TXT_SEED, plantName);
			image = ItemSpriteSheet.SEED_FADELEAF;
			
			plantClass = Fadeleaf.class;
			alchemyClass = PotionOfMindVision.class;
		}
		
		@Override
		public String desc() {
			return TXT_DESC;
		}
		
		@Override
		public void execute( Hero hero, String action ) {
			
			super.execute( hero, action );
			
			if (action.equals( Food.AC_EAT )) {
				ScrollOfTeleportation.teleportHero( hero );
				hero.spendAndNext(1);
				Buff.affect(hero, Vertigo.class, Vertigo.DURATION * 2);
			}
		}
	}
}
