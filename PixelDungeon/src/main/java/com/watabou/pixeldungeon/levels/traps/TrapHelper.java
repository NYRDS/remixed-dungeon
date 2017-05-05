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

import android.support.annotation.Nullable;

import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Sample;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.effects.CellEmitter;
import com.watabou.pixeldungeon.effects.Speck;
import com.watabou.pixeldungeon.levels.Terrain;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.windows.WndOptions;

import java.util.Arrays;

public class TrapHelper {

	private static final String TXT_CHASM = Game.getVar(R.string.TrapWnd_Title);
	private static final String TXT_YES   = Game.getVar(R.string.Chasm_Yes);
	private static final String TXT_NO    = Game.getVar(R.string.Chasm_No);
	private static final String TXT_STEP  = Game.getVar(R.string.TrapWnd_Step);

	public static boolean stepConfirmed = false;

	private static final int[] TRAPS = {
			Terrain.TOXIC_TRAP,
			Terrain.ALARM_TRAP,
			Terrain.FIRE_TRAP,
			Terrain.GRIPPING_TRAP,
			Terrain.LIGHTNING_TRAP,
			Terrain.PARALYTIC_TRAP,
			Terrain.POISON_TRAP,
			Terrain.SUMMONING_TRAP
	};

	public static boolean CellIsTrap(int trapId){
		return Arrays.asList(TRAPS).contains(trapId);
	}

	public static void heroTriggerTrap( final Hero hero ) {
		GameScene.show(
				new WndOptions( TXT_CHASM, TXT_STEP, TXT_YES, TXT_NO ) {
					@Override
					protected void onSelect( int index ) {
						if (index == 0) {
							stepConfirmed = true;
							hero.resume();
						}
					}
				}
		);
	}
}
