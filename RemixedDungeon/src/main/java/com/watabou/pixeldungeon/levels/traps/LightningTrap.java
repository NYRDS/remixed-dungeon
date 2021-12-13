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
package com.watabou.pixeldungeon.levels.traps;

import com.nyrds.pixeldungeon.levels.objects.ITrigger;
import com.nyrds.pixeldungeon.mechanics.NamedEntityKind;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.noosa.Camera;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.ResultDescriptions;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.effects.CellEmitter;
import com.watabou.pixeldungeon.effects.Lightning;
import com.watabou.pixeldungeon.effects.particles.SparkParticle;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.utils.Random;

import org.jetbrains.annotations.Nullable;

public class LightningTrap implements ITrigger{

	// 00x66CCEE
	
	public static void trigger( int pos, @Nullable Char ch ) {
		if (ch == null){
			ch = Actor.findChar(pos);
		}
		if (ch != null) {
			ch.damage( Math.max( 1, Random.Int( ch.hp() / 3, 2 * ch.hp() / 3 ) ), LIGHTNING );
			if (ch == Dungeon.hero) {
				
				Camera.main.shake( 2, 0.3f );
				
				if (!ch.isAlive()) {
                    Dungeon.fail( Utils.format( ResultDescriptions.getDescription(ResultDescriptions.Reason.TRAP), StringsManager.getVar(R.string.LightningTrap_Name), Dungeon.depth ) );
                    GLog.n(StringsManager.getVar(R.string.LightningTrap_Desc));
				} else {
					ch.getBelongings().charge( false );
				}
			}
			
			int[] points = new int[2];
			
			points[0] = pos - Dungeon.level.getWidth();
			points[1] = pos + Dungeon.level.getWidth();
			GameScene.addToMobLayer( new Lightning( points) );
			
			points[0] = pos - 1;
			points[1] = pos + 1;
			GameScene.addToMobLayer( new Lightning( points) );
		}
		
		CellEmitter.center( pos ).burst( SparkParticle.FACTORY, Random.IntRange( 3, 4 ) );
		
	}
	
	public static final Electricity LIGHTNING = new Electricity();
	public static class Electricity implements NamedEntityKind {
		@Override
		public String getEntityKind() {
			return getClass().getSimpleName();
		}

		@Override
		public String name() {
			return getEntityKind();
		}
	}

	@Override
	public void doTrigger(int cell, Char ch) {
		trigger(cell,ch);
	}
}
