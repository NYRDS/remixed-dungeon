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
package com.watabou.pixeldungeon.items.food;

import com.nyrds.pixeldungeon.mechanics.CommonActions;
import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.items.Item;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

abstract public class Food extends Item {

	public static final float TIME_TO_EAT	= 3f;

	public float energy   = 0;
	public String message = Game.getVar(R.string.Food_Message);
	
	{
		stackable = true;
		setDefaultAction(CommonActions.AC_EAT);
	}
	
	@Override
	public ArrayList<String> actions(Char hero ) {
		ArrayList<String> actions = super.actions( hero );
		actions.add( CommonActions.AC_EAT );
		return actions;
	}


	@Override
	public void _execute(@NotNull Char chr, @NotNull String action ) {
		if (action.equals( CommonActions.AC_EAT )) {
			chr.eat(this, energy, message);
		} else {
			super._execute(chr, action );
		}
	}

	@Override
	public boolean isUpgradable() {
		return false;
	}
	
	@Override
	public boolean isIdentified() {
		return true;
	}
}
