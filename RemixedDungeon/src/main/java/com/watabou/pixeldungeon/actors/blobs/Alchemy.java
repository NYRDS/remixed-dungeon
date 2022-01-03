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

import com.nyrds.LuaInterface;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.Journal;
import com.watabou.pixeldungeon.effects.BlobEmitter;
import com.watabou.pixeldungeon.effects.Speck;
import com.watabou.pixeldungeon.items.Heap;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.levels.Level;

public class Alchemy extends Blob {

	@Override
	protected void evolve() {

		volume = 0;
		for (int i = 0;i<getLength();i++) {
			volume += cur[i];
			if (cur[i] > 0 && Dungeon.isCellVisible(i)) {
				Journal.add(Journal.Feature.ALCHEMY.desc());
			}
		}
	}
	
	@Override
	public void seed( int cell, int amount ) {
		checkSeedCell(cell);
		cur[cell] = amount;
		volume += amount;

	}

	@LuaInterface
	public static void transmute( int cell ) {
		final Level level = Dungeon.level;

		Heap heap = level.getHeap( cell );
		if (heap != null) {
			
			Item result = heap.transmute();
			if (result != null) {
				level.animatedDrop( result, cell );
			}
		}
	}
	
	@Override
	public void use( BlobEmitter emitter ) {
		super.use( emitter );	
		emitter.start( Speck.factory( Speck.BUBBLE ), 0.4f, 0 );
	}
}
