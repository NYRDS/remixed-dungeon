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
import com.watabou.pixeldungeon.actors.CharUtils;
import com.watabou.pixeldungeon.sprites.CharSprite;
import com.watabou.pixeldungeon.ui.BuffIndicator;
import com.watabou.pixeldungeon.utils.GLog;

import org.jetbrains.annotations.NotNull;

public class Fury extends Buff {
	
	public static float LEVEL	= 0.4f;
	
	@Override
	public boolean act() {
		if (target.hp() > target.ht() * LEVEL) {
			detach();
		}
		
		spend( TICK );
		
		return true;
	}

	@Override
	public boolean attachTo(@NotNull Char target) {
		if (CharUtils.isVisible(target)) {
			GLog.w(StringsManager.getVar(R.string.Brute_Enraged), target.getName() );
			target.showStatus( CharSprite.NEGATIVE, StringsManager.getVar(R.string.Brute_StaEnraged));
		}
		return super.attachTo(target);
	}

	@Override
	public int attackProc(Char attacker, Char defender, int damage) {
		return damage * 2;
	}

	@Override
	public int icon() {
		return BuffIndicator.FURY;
	}
}
