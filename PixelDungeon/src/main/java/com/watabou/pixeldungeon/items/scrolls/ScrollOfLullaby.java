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
import com.watabou.pixeldungeon.actors.buffs.Sleep;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.effects.Speck;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;

public class ScrollOfLullaby extends Scroll {
	
	@Override
	protected void doRead() {
		
		getCurUser().getSprite().centerEmitter().start( Speck.factory( Speck.NOTE ), 0.3f, 5 );
		Sample.INSTANCE.play( Assets.SND_LULLABY );
		
		int count = 0;
		Mob affected = null;
		for (Mob mob : Dungeon.level.mobs.toArray(new Mob[Dungeon.level.mobs.size()])) {
			if (Dungeon.level.fieldOfView[mob.getPos()]) {
				Buff.affect( mob, Sleep.class );
				if (mob.buff( Sleep.class ) != null) {
					affected = mob;
					count++;
				}
			}
		}
		
		switch (count) {
		case 0:
			GLog.i(Game.getVar(R.string.ScrollOfLullaby_Info1));
			break;
		case 1:
			GLog.i(Utils.format(Game.getVar(R.string.ScrollOfLullaby_Info2), affected.getName()));
			break;
		default:
			GLog.i(Game.getVar(R.string.ScrollOfLullaby_Info3));
		}
		setKnown();
		
		getCurUser().spendAndNext( TIME_TO_READ );
	}

	@Override
	public int price() {
		return isKnown() ? 50 * quantity() : super.price();
	}
}
