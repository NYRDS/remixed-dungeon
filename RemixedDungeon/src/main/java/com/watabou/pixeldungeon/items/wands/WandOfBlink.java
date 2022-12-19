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

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.audio.Sample;
import com.nyrds.platform.util.StringsManager;
import com.watabou.noosa.tweeners.AlphaTweener;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.effects.MagicMissile;
import com.watabou.pixeldungeon.effects.Speck;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.mechanics.Ballistica;
import com.watabou.utils.Callback;

import org.jetbrains.annotations.NotNull;



public class WandOfBlink extends Wand {

	{
		hitObjects = true;
	}

	@Override
	protected int getDestinationCell(int src, int target) {
		int wandLevel = effectiveLevel();

		int newCell = super.getDestinationCell(src, target);

		if (Ballistica.distance > wandLevel + 4) {
			newCell = Ballistica.trace[wandLevel + 3];
		} else if ( !getOwner().level().isCellNonOccupied(target) && Ballistica.distance > 1) {
			newCell = Ballistica.trace[Ballistica.distance - 2];
		}

		return newCell;
	}

	@Override
	protected void onZap( int cell ) {

		getOwner().getSprite().setVisible(true);
		appear( getOwner(), cell);
		Dungeon.observe();
	}
	
	@Override
	protected void fx( int cell, Callback callback ) {
		getOwner().getSprite().setVisible(false);
		MagicMissile.whiteLight( getOwner().getSprite().getParent(), getOwner().getPos(), cell, callback );
		Sample.INSTANCE.play( Assets.SND_ZAP );
	}

	public static void appear(@NotNull Char ch, int pos ) {

		Level level = ch.level();

		if(!level.cellValid(ch.getPos())) { //ch not on level yet
			ch.setPos(pos);
			if(ch instanceof Mob) {
				level.spawnMob((Mob)ch);
			}
		}

		ch.placeTo(pos);
		var chSprite = ch.getSprite();

		chSprite.interruptMotion();
		chSprite.place( pos );

		if (ch.invisible == 0) {
			chSprite.alpha( 0 );
			if(chSprite.hasParent()) {
				chSprite.getParent().add(new AlphaTweener(chSprite, 1, 0.4f));
			}
		}

		chSprite.emitter().start( Speck.factory( Speck.LIGHT ), 0.2f, 3 );
		Sample.INSTANCE.play( Assets.SND_TELEPORT );
	}

	@Override
	public String desc() {
        return StringsManager.getVar(R.string.WandOfBlink_Info);
    }
	
	@Override
	public boolean affectTarget() {
		return false;
	}
}
