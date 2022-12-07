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
package com.watabou.pixeldungeon.items.scrolls;

import com.nyrds.retrodungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Sample;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Invisibility;
import com.watabou.pixeldungeon.actors.buffs.Terror;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.effects.Flare;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;

public class ScrollOfTerror extends Scroll {

	@Override
	protected void doRead() {
		
		new Flare( 5, 32 ).color( 0xFF0000, true ).show( getCurUser().getSprite(), 2f );
		Sample.INSTANCE.play( Assets.SND_READ );
		Invisibility.dispel(getCurUser());
		
		int count = 0;
		Mob affected = null;
		for (Mob mob : Dungeon.level.mobs.toArray(new Mob[Dungeon.level.mobs.size()])) {
			if (Dungeon.level.fieldOfView[mob.getPos()]) {
				Terror terror = Buff.affect( mob, Terror.class, Terror.DURATION );
				terror.source = getCurUser();
				
				count++;
				affected = mob;
			}
		}
		
		switch (count) {
		case 0:
			GLog.i(Game.getVar(R.string.ScrollOfTerror_Info1));
			break;
		case 1:
			GLog.i(Utils.format(Game.getVar(R.string.ScrollOfTerror_Info2), affected.getName()));
			break;
		default:
			GLog.i(Game.getVar(R.string.ScrollOfTerror_Info3));
		}
		setKnown();
		
		getCurUser().spendAndNext( TIME_TO_READ );
	}
	
	@Override
	public String desc() {
		return Game.getVar(R.string.ScrollOfTerror_Info);
	}
	
	@Override
	public int price() {
		return isKnown() ? 50 * quantity() : super.price();
	}
}
