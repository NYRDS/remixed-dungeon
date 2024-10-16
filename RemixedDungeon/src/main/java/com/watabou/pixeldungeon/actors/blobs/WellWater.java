
package com.watabou.pixeldungeon.actors.blobs;

import com.nyrds.LuaInterface;
import com.nyrds.pixeldungeon.items.ItemUtils;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.Journal;
import com.watabou.pixeldungeon.Journal.Feature;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.items.Heap;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.utils.Bundle;

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
		setVolume(off[pos] = cur[pos]);
		
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

	@LuaInterface
	public boolean affect() {

		Heap heap;

		final Hero hero = Dungeon.hero;

		if (pos == hero.getPos() && affectHero(hero)) {
			setVolume(off[pos] = cur[pos] = 0);
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
					heap.sprite.link(heap);
					setVolume(off[pos] = cur[pos] = 0);
					return true;
				} else {
					ItemUtils.throwItemAway(pos);
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
		setVolume(cur[pos] = amount);
	}
}
