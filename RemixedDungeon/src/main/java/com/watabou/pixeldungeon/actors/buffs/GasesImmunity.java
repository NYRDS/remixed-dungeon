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
package com.watabou.pixeldungeon.actors.buffs;

import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.actors.blobs.ToxicGas;
import com.watabou.pixeldungeon.ui.BuffIndicator;

import java.util.HashSet;
import java.util.Set;

public class GasesImmunity extends FlavourBuff {
	
	public static final float DURATION	= 5f;

	private static final Set<String> FULL;
	static {
		FULL = new HashSet<>();

		FULL.add( ToxicGas.class.getSimpleName() );
		FULL.add( Paralysis.class.getSimpleName() );
		FULL.add( Vertigo.class.getSimpleName() );
	}

	@Override
	public int icon() {
		return BuffIndicator.IMMUNITY;
	}
	
	@Override
	public String name() {
		return Game.getVar(R.string.GasesImmunity_Info);
	}

	@Override
	public Set<String> immunities() {
		return FULL;
	}
}
