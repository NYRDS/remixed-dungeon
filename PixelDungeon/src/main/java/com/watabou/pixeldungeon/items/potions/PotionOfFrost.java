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
package com.watabou.pixeldungeon.items.potions;

import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Sample;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Dungeon;
import com.nyrds.retrodungeon.ml.R;
import com.watabou.pixeldungeon.actors.blobs.Fire;
import com.watabou.pixeldungeon.actors.blobs.Freezing;
import com.watabou.pixeldungeon.items.weapon.missiles.Arrow;
import com.watabou.pixeldungeon.items.weapon.missiles.FrostArrow;
import com.watabou.pixeldungeon.utils.BArray;
import com.watabou.utils.PathFinder;

public class PotionOfFrost extends UpgradablePotion {
	
	private static final int DISTANCE	= 2;
	
	@Override
	public void shatter( int cell ) {
		
		if( !canShatter() ) {
			return;
		}
		
		PathFinder.buildDistanceMap( cell, BArray.not( Dungeon.level.losBlocking, null ), (int) (DISTANCE * qualityFactor()));
		
		Fire fire = (Fire)Dungeon.level.blobs.get( Fire.class );
		
		for (int i=0; i < Dungeon.level.getLength(); i++) {
			if (PathFinder.distance[i] < Integer.MAX_VALUE) {
				Freezing.affect( i, fire );
			}
		}
		
		splash( cell );
		Sample.INSTANCE.play( Assets.SND_SHATTER );
		
		setKnown();
	}
	
	@Override
	public String desc() {
		return Game.getVar(R.string.PotionOfFrost_Info);
	}

	@Override
	public int basePrice() {
		return 50;
	}
	
	@Override
	protected void moistenArrow(Arrow arrow) {
		int quantity = reallyMoistArrows(arrow);
		
		FrostArrow moistenArrows = new FrostArrow(quantity);
		getCurUser().collect(moistenArrows);
	}
}
