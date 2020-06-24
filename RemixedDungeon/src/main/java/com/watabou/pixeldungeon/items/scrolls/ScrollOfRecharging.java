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
import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Sample;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Invisibility;
import com.watabou.pixeldungeon.effects.SpellSprite;
import com.watabou.pixeldungeon.effects.particles.EnergyParticle;
import com.watabou.pixeldungeon.utils.GLog;

import org.jetbrains.annotations.NotNull;

public class ScrollOfRecharging extends Scroll {
	
	@Override
	protected void doRead(@NotNull Char reader) {

		int count = reader.getBelongings().charge( true );
		charge( reader );
		
		Sample.INSTANCE.play( Assets.SND_READ );
		Invisibility.dispel(reader);
		
		if (count > 0) {
			GLog.i((count > 1 ? Game.getVar(R.string.ScrollOfRecharging_Info1b) 
					          : Game.getVar(R.string.ScrollOfRecharging_Info1a)) );
			SpellSprite.show( reader, SpellSprite.CHARGE );
		} else {
			GLog.i(Game.getVar(R.string.ScrollOfRecharging_Info2));
		}
		setKnown();

		reader.spendAndNext( TIME_TO_READ );
	}

	public static void charge(Char hero ) {
		hero.getSprite().centerEmitter().burst( EnergyParticle.FACTORY, 15 );
	}
	
	@Override
	public int price() {
		return isKnown() ? 40 * quantity() : super.price();
	}
}
