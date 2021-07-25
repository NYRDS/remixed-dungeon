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
import com.watabou.pixeldungeon.ui.BuffIndicator;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;

public class Ooze extends Buff {

	public int damage	= 1;
	
	@Override
	public int icon() {
		return BuffIndicator.OOZE;
	}

	@Override
	public boolean act() {
		if (target.isAlive()) {
			target.damage( damage, this );
			if (!target.isAlive() && target == Dungeon.hero) {
				Dungeon.fail( Utils.format( ResultDescriptions.getDescription(ResultDescriptions.Reason.OOZE), Dungeon.depth ) );
                GLog.n(StringsManager.getVar(R.string.Ooze_Death), name() );
			}
			spend( TICK );
		}
		if (Dungeon.level.water[target.getPos()]) {
			detach();
		}
		return true;
	}
}
