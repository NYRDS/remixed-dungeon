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

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.audio.Sample;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Invisibility;
import com.watabou.pixeldungeon.actors.buffs.Terror;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.effects.Flare;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;

import org.jetbrains.annotations.NotNull;

public class ScrollOfTerror extends Scroll {

	@Override
	protected void doRead(@NotNull Char reader) {

		new Flare( 5, 32 ).color( 0xFF0000, true ).show( reader.getSprite(), 2f );
		Sample.INSTANCE.play( Assets.SND_READ );
		Invisibility.dispel(reader);

		Level level = reader.level();

		int count = 0;
		Mob affected = null;
		for (Mob mob : level.getCopyOfMobsArray()) {
			if (level.fieldOfView[mob.getPos()] && mob.getOwnerId() != reader.getId()) {
				Buff.affect( mob, Terror.class, Terror.DURATION ).setSource(reader);
				count++;
				affected = mob;
			}
		}
		
		switch (count) {
		case 0:
            GLog.i(StringsManager.getVar(R.string.ScrollOfTerror_Info1));
			break;
		case 1:
            GLog.i(Utils.format(R.string.ScrollOfTerror_Info2, affected.getName()));
			break;
		default:
            GLog.i(StringsManager.getVar(R.string.ScrollOfTerror_Info3));
		}
		setKnown();

		reader.spend( TIME_TO_READ );
	}
	
	@Override
	public String desc() {
        return StringsManager.getVar(R.string.ScrollOfTerror_Info);
    }
	
	@Override
	public int price() {
		return isKnown() ? 50 * quantity() : super.price();
	}
}
