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
package com.watabou.pixeldungeon.items.armor.glyphs;

import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.effects.Pushing;
import com.watabou.pixeldungeon.items.armor.Armor;
import com.watabou.pixeldungeon.items.armor.Armor.Glyph;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.utils.Random;

import org.jetbrains.annotations.NotNull;

public class Bounce extends Glyph {

	@Override
	public int defenceProc(Armor armor, @NotNull Char attacker, Char defender, int damage) {

		int armorLevel = Math.max( 0, armor.level() );

		Level level = Dungeon.level;

		if (attacker.isMovable() && attacker.adjacent(defender) && Random.Int( armorLevel + 5) >= 4) {
			
			for (int i=0; i < Level.NEIGHBOURS8.length; i++) {
				int ofs = Level.NEIGHBOURS8[i];
				if (attacker.getPos() - defender.getPos() == ofs) {
					int newPos = attacker.getPos() + ofs;
					if (!level.cellValid(newPos)){
						newPos = defender.getPos();
					}
					if ((level.passable[newPos] || level.avoid[newPos]) && Actor.findChar( newPos ) == null) {
						
						Actor.addDelayed( new Pushing( attacker, attacker.getPos(), newPos ), -1 );
						
						attacker.setPos(newPos);

						level.press(newPos, attacker);
					}
					break;
				}
			}

		}
		
		return damage;
	}
	
	@Override
	public String name( String armorName) {
		return Utils.format( Game.getVar(R.string.Bounce_Txt), armorName );
	}

}
