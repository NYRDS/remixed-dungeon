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
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.blobs.ToxicGas;
import com.watabou.pixeldungeon.ui.BuffIndicator;

public class GasesImmunity extends FlavourBuff {
	
	public static final float DURATION	= 5f;
	
	@Override
	public int icon() {
		return BuffIndicator.IMMUNITY;
	}
	
	@Override
	public String toString() {
		return Game.getVar(R.string.GasesImmunity_Info);
	}


	@Override
	public boolean attachTo(Char target) {
		if(super.attachTo(target)) {
			target.addImmunity(Paralysis.class);
			target.addImmunity(ToxicGas.class);
			target.addImmunity(Vertigo.class);
			return true;
		}
		return false;
	}

	@Override
	public void detach() {
		super.detach();

		target.removeImmunity(Paralysis.class);
		target.removeImmunity(ToxicGas.class);
		target.removeImmunity(Vertigo.class);
	}
}
