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
package com.watabou.pixeldungeon.levels.traps;

import android.support.annotation.Nullable;

import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.mobs.Bestiary;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.items.wands.WandOfBlink;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class SummoningTrap {

	private static final float DELAY = 2f;
	
	private static final Mob DUMMY = new Mob() {};
	
	// 0x770088
	
	public static void trigger( int pos, @Nullable Char c ) {
		
		if (Dungeon.bossLevel()) {
			return;
		}
		
		if (c != null) {
			Actor.occupyCell( c );
		}
		
		int nMobs = 1;
		if (Random.Int( 2 ) == 0) {
			nMobs++;
			if (Random.Int( 2 ) == 0) {
				nMobs++;
			}
		}
		
		// It's complicated here, because these traps can be activated in chain
		
		ArrayList<Integer> candidates = new ArrayList<>();
		
		for (int i=0; i < Level.NEIGHBOURS8.length; i++) {
			int p = pos + Level.NEIGHBOURS8[i];
			if (Actor.findChar( p ) == null && (Dungeon.level.passable[p] || Dungeon.level.avoid[p])) {
				candidates.add( p );
			}
		}
		
		ArrayList<Integer> respawnPoints = new ArrayList<>();
		
		while (nMobs > 0 && candidates.size() > 0) {
			int index = Random.index( candidates );
			
			DUMMY.setPos(candidates.get( index ));
			Actor.occupyCell( DUMMY );
			
			respawnPoints.add( candidates.remove( index ) );
			nMobs--;
		}
		
		for (Integer point : respawnPoints) {
			Mob mob;
			do {
				mob = Bestiary.mob();
			} while (mob.isWallWalker());
			
			mob.setState(mob.WANDERING);
			Dungeon.level.spawnMob(mob, DELAY);
			WandOfBlink.appear( mob, point );
		}
	}
}
