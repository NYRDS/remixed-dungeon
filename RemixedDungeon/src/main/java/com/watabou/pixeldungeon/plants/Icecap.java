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

import com.nyrds.pixeldungeon.levels.objects.Presser;
import com.nyrds.pixeldungeon.mechanics.CommonActions;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.blobs.Freezing;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Frost;
import com.watabou.pixeldungeon.items.potions.PotionOfFrost;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.utils.BArray;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import org.jetbrains.annotations.NotNull;

public class Icecap extends Plant {

	public Icecap() {
		imageIndex = 1;
	}
	
	@Override
	public void effect(int pos, Presser ch ) {
		final Level level = Dungeon.level;

		PathFinder.buildDistanceMap( pos, BArray.not( level.losBlocking, null ), 1 );

		for (int i = 0; i < level.getLength(); i++) {
			if (PathFinder.distance[i] < Integer.MAX_VALUE) {
				Freezing.affect( i );
			}
		}
	}

	public static class Seed extends com.watabou.pixeldungeon.plants.Seed {
		{
            plantName = StringsManager.getVar(R.string.Icecap_Name);

            name = Utils.format(StringsManager.getVar(R.string.Plant_Seed), plantName);
			image = 1;
			
			plantClass = Icecap.class;
			alchemyClass = PotionOfFrost.class;
		}
		
		@Override
		public String desc() {
            return StringsManager.getVar(R.string.Icecap_Desc);
        }
		
		@Override
		public void _execute(@NotNull Char chr, @NotNull String action ) {
			
			super._execute(chr, action );
			
			if (action.equals( CommonActions.AC_EAT )) {

				Buff.prolong(chr, Frost.class, Frost.duration(chr) * 2);
				chr.heal( Random.Int(0, Math.max((chr.ht() - chr.hp()) / 4, 10) ), this);
			}
		}
	}
}
