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
import com.watabou.noosa.tweeners.AlphaTweener;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Dungeon;
import com.nyrds.retrodungeon.ml.R;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.effects.MagicMissile;
import com.watabou.pixeldungeon.effects.Speck;
import com.watabou.pixeldungeon.mechanics.Ballistica;
import com.watabou.utils.Callback;

public class WandOfBlink extends Wand {

	@Override
	protected int getDestinationCell(Integer target) {
		int cell = super.getDestinationCell(target);

		int level = effectiveLevel();

		int userPos = wandUser.getPos();

		int newCell = Ballistica.trace[Ballistica.distance-1];

		if (Ballistica.distance > level + 4) {
			newCell = Ballistica.trace[level + 3];
		} else if (Actor.findChar( newCell ) != null && Ballistica.distance > 1) {
			newCell = Ballistica.trace[Ballistica.distance - 2];
		}

		if(Dungeon.level.distance(userPos,cell)<Dungeon.level.distance(userPos,newCell)) {
			newCell = cell;
		}
		return newCell;
	}

	@Override
	protected void onZap( int cell ) {

		wandUser.getSprite().setVisible(true);
		appear( wandUser, cell);
		Dungeon.observe();
	}
	
	@Override
	protected void fx( int cell, Callback callback ) {
		wandUser.getSprite().setVisible(false);
		MagicMissile.whiteLight( wandUser.getSprite().getParent(), wandUser.getPos(), cell, callback );
		Sample.INSTANCE.play( Assets.SND_ZAP );
	}
	
	public static void appear( Char ch, int pos ) {
		ch.getSprite().interruptMotion();
		
		ch.move( pos );
		ch.getSprite().place( pos );
		
		if (ch.invisible == 0) {
			ch.getSprite().alpha( 0 );
			ch.getSprite().getParent().add( new AlphaTweener( ch.getSprite(), 1, 0.4f ) );
		}
		
		ch.getSprite().emitter().start( Speck.factory( Speck.LIGHT ), 0.2f, 3 );
		Sample.INSTANCE.play( Assets.SND_TELEPORT );
	}
	
	@Override
	public String desc() {
		return Game.getVar(R.string.WandOfBlink_Info);
	}
	
	@Override
	public boolean affectTarget() {
		return false;
	}
}
