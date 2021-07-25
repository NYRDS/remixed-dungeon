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
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.mobs.Fraction;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.sprites.CharSprite;
import com.watabou.pixeldungeon.ui.BuffIndicator;

import org.jetbrains.annotations.NotNull;

public class Terror extends FlavourBuff {

	public static final float DURATION = 10f;

	@Override
	public int icon() {
		return BuffIndicator.TERROR;
	}

	public static void recover( Char target ) {
		Terror terror = target.buff( Terror.class );
		if (terror != null && terror.cooldown() < DURATION) {
			target.remove( terror );
		}
	}

	@Override
	public boolean attachTo(@NotNull Char target) {
		if(super.attachTo(target)) {
			if(target instanceof Mob && target.fraction!=Fraction.NEUTRAL) {
				Mob tgt = (Mob)target;
				tgt.releasePet();
			}
			return true;
		}
		return false;
	}

	@Override
	public void attachVisual() {
        target.getSprite().showStatus(CharSprite.NEGATIVE, StringsManager.getVar(R.string.Char_StaFrightened));
	}
}
