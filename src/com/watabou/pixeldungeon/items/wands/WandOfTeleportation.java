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
package com.watabou.pixeldungeon.items.wands;

import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Sample;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Dungeon;
import com.nyrds.pixeldungeon.ml.R;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.effects.MagicMissile;
import com.watabou.pixeldungeon.items.scrolls.ScrollOfTeleportation;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.utils.Callback;

public class WandOfTeleportation extends Wand {

	@Override
	protected void onZap( int cell ) {
		
		Char ch = Actor.findChar( cell );
		
		if (ch == curUser) {
			
			setKnown();
			ScrollOfTeleportation.teleportHero( curUser );
			
		} else if (ch != null) {
			
			ch.pos = Dungeon.level.randomRespawnCell();
			ch.getSprite().place(ch.pos);
			ch.getSprite().visible = Dungeon.visible[ch.pos];
			GLog.i(String.format(
					Game.getVar(R.string.WandOfTeleportation_Info1),
					curUser.name, ch.name_objective));
		} else {
			GLog.i(Game.getVar(R.string.WandOfTeleportation_Info2));
		}
	}
	
	protected void fx( int cell, Callback callback ) {
		MagicMissile.coldLight( curUser.getSprite().parent, curUser.pos, cell, callback );
		Sample.INSTANCE.play( Assets.SND_ZAP );
	}
	
	@Override
	public String desc() {
		return Game.getVar(R.string.WandOfTeleportation_Info);
	}
}
