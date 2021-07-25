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

import com.nyrds.platform.audio.Sample;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.effects.Speck;
import com.watabou.pixeldungeon.items.quest.DriedRose;
import com.watabou.pixeldungeon.items.rings.RingOfElements.Resistance;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.ui.BuffIndicator;

import org.jetbrains.annotations.NotNull;

public class Charm extends FlavourBuff {
	
	@Override
	public boolean attachTo(@NotNull Char target ) {

		if(target.hasBuff(DriedRose.OneWayLoveBuff.class)){
			return false;
		}

		if (super.attachTo( target )) {
			if(GameScene.isSceneReady()) {
				target.getSprite().centerEmitter().start(Speck.factory(Speck.HEART), 0.2f, 5);
				Sample.INSTANCE.play(Assets.SND_CHARMS);
			}
			target.pacified = true;
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	public void detach() {
		target.pacified = false;
		super.detach();
	}
	
	@Override
	public int icon() {
		return BuffIndicator.HEART;
	}

	public static float durationFactor(@NotNull Char ch ) {

		if(ch.hasBuff(DriedRose.OneWayLoveBuff.class)) {
			return 0;
		}

		if(ch.hasBuff(DriedRose.OneWayCursedLoveBuff.class)) {
			return 2;
		}

		Resistance r = ch.buff( Resistance.class );
		return r != null ? r.durationFactor() : 1;
	}
}
