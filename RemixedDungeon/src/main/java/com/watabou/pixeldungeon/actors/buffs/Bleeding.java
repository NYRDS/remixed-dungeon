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

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.ResultDescriptions;
import com.watabou.pixeldungeon.effects.Splash;
import com.watabou.pixeldungeon.sprites.CharSprite;
import com.watabou.pixeldungeon.ui.BuffIndicator;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.utils.PointF;
import com.watabou.utils.Random;

public class Bleeding extends Buff {

	@Override
	public int icon() {
		return BuffIndicator.BLEEDING;
	}

	@Override
	public boolean act() {
		if (target.isAlive()) {
			
			if ((level = Random.Int( level / 2, level )) > 0) {
				
				target.damage( level, this );
				final CharSprite targetSprite = target.getSprite();

				if (targetSprite.getVisible()) {
					Splash.at( targetSprite.center(), -PointF.PI / 2, PointF.PI / 6,
							targetSprite.blood(), Math.min( 10 * level / target.ht(), 10 ) );
				}
				
				if (target == Dungeon.hero && !target.isAlive()) {
					Dungeon.fail( Utils.format( ResultDescriptions.getDescription(ResultDescriptions.Reason.BLEEDING), Dungeon.depth ) );
                    GLog.n(StringsManager.getVar(R.string.Bleeding_Death));
				}
				
				spend( TICK );
			} else {
				detach();
			}
		} else {
			detach();
		}
		return true;
	}

	@Override
	public void attachVisual() {
        target.getSprite().showStatus(CharSprite.NEGATIVE, StringsManager.getVar(R.string.Char_StaBleeding));
	}
}
