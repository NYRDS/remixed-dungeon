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
package com.watabou.pixeldungeon.actors.blobs;

import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.Journal;
import com.watabou.pixeldungeon.Journal.Feature;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.items.Heap;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.levels.Terrain;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

public class WellWater extends Blob {

	protected int pos;
	
	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		
		for (int i=0; i < getLength(); i++) {
			if (cur[i] > 0) {
				pos = i;
				break;
			}
		}
	}
	
	@Override
	protected void evolve() {
		volume = off[pos] = cur[pos];
		
		if (Dungeon.isCellVisible(pos)) {
			if (this instanceof WaterOfAwareness) {
				Journal.add( Feature.WELL_OF_AWARENESS.desc() );
			} else if (this instanceof WaterOfHealth) {
				Journal.add( Feature.WELL_OF_HEALTH.desc() );
			} else if (this instanceof WaterOfTransmutation) {
				Journal.add( Feature.WELL_OF_TRANSMUTATION.desc() );
			}
		}
	}
	
	protected boolean affect() {

		Heap heap;
		
		if (pos == Dungeon.hero.getPos() && affectHero( Dungeon.hero )) {
			volume = off[pos] = cur[pos] = 0;
			return true;
			
		} else {
			final Level level = Dungeon.level;

			if ((heap = level.getHeap( pos )) != null) {

				Item oldItem = heap.peek();
				Item newItem = affectItem( oldItem );

				if (newItem != null) {
					if (newItem != oldItem) {
						if (oldItem.quantity() > 1) {
							oldItem.quantity( oldItem.quantity() - 1 );
							heap.drop( newItem );
						} else {
							heap.replace( oldItem, newItem );
						}
					}
					heap.sprite.link();
					volume = off[pos] = cur[pos] = 0;
					return true;
				} else {
					int newPlace;
					do {
						newPlace = pos + Level.NEIGHBOURS8[Random.Int( 8 )];
					} while (!level.passable[newPlace] && !level.avoid[newPlace]);
					level.animatedDrop( heap.pickUp(), newPlace );

					return false;
				}
			} else {
				return false;
			}
		}
	}
	
	protected boolean affectHero( Hero hero ) {
		return false;
	}
	
	protected Item affectItem( Item item ) {
		return null;
	}
	
	@Override
	public void seed( int cell, int amount ) {
		checkSeedCell(cell);
		cur[pos] = 0;
		pos = cell;
		volume = cur[pos] = amount;
	}
	
	public static void affectCell( int cell ) {
		
		Class<?>[] waters = {WaterOfHealth.class, WaterOfAwareness.class, WaterOfTransmutation.class};
		
		for (Class<?>waterClass : waters) {
			final Level level = Dungeon.level;
			WellWater water = (WellWater) level.blobs.get( waterClass );
			if (water != null && 
				water.volume > 0 && 
				water.pos == cell && 
				water.affect()) {
					level.set( cell, Terrain.EMPTY_WELL );
					GameScene.updateMap( cell );
					return;
			}
		}
	}
}
