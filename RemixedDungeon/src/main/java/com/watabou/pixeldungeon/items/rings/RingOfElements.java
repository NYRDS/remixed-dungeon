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
package com.watabou.pixeldungeon.items.rings;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.actors.blobs.LiquidFlame;
import com.watabou.pixeldungeon.actors.blobs.ToxicGas;
import com.watabou.pixeldungeon.actors.buffs.Burning;
import com.watabou.pixeldungeon.actors.buffs.Poison;
import com.watabou.pixeldungeon.actors.mobs.Eye;
import com.watabou.pixeldungeon.actors.mobs.Warlock;
import com.watabou.pixeldungeon.actors.mobs.Yog;
import com.watabou.pixeldungeon.levels.traps.LightningTrap;
import com.watabou.utils.Random;

import java.util.HashSet;
import java.util.Set;

public class RingOfElements extends Ring {

	@Override
	public ArtifactBuff buff( ) {
		return new Resistance();
	}
	
	@Override
	public String desc() {
        return isKnown() ? StringsManager.getVar(R.string.RingOfElements_Info) : super.desc();
	}


	public class Resistance extends RingBuff {

		private final Set<String> FULL;
		{
			FULL = new HashSet<>();
			FULL.add( Burning.class.getSimpleName() );
			FULL.add( ToxicGas.class.getSimpleName() );
			FULL.add( Poison.class.getSimpleName() );
			FULL.add( LightningTrap.Electricity.class.getSimpleName() );
			FULL.add( Warlock.class.getSimpleName() );
			FULL.add( Eye.class.getSimpleName() );
			FULL.add( Yog.BurningFist.class.getSimpleName() );
			FULL.add( LiquidFlame.class.getSimpleName() );
		}


		public Set<String> resistances() {
			if (Random.Int( level() + 3 ) >= 3) {
				return FULL;
			} else {
				return EMPTY_STRING_SET;
			}
		}
		
		public float durationFactor() {
			return level() < 0 ? 1 : (2 + 0.5f * level()) / (2 + level());
		}
	}
}
