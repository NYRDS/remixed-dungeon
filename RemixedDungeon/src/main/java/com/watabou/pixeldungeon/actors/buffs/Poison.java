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
import com.watabou.pixeldungeon.Badges;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.ResultDescriptions;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.hero.Doom;
import com.watabou.pixeldungeon.effects.CellEmitter;
import com.watabou.pixeldungeon.effects.particles.PoisonParticle;
import com.watabou.pixeldungeon.items.rings.RingOfElements.Resistance;
import com.watabou.pixeldungeon.sprites.CharSprite;
import com.watabou.pixeldungeon.ui.BuffIndicator;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;

public class Poison extends Buff implements Doom {

	@Override
	public int icon() {
		return BuffIndicator.POISON;
	}

	@Override
	public void charAct() {
		float timeLeft = cooldown();
		target.damage( (int)(timeLeft / 3) + 1, this );
	}

	@Override
	public boolean act() {
		detach();
		return true;
	}

	public static float durationFactor(Char ch ) {
		if(ch==null) { //primary to mask bug in Remixed Additions
			return 1;
		}

		Resistance r = ch.buff( Resistance.class );
		return r != null ? r.durationFactor() : 1;
	}

	@Override
	public void onDeath() {
		Badges.validateDeathFromPoison();
		
		Dungeon.fail( Utils.format( ResultDescriptions.getDescription(ResultDescriptions.Reason.POISON), Dungeon.depth ) );
        GLog.n(StringsManager.getVar(R.string.Poison_Death));
	}

	@Override
	public void attachVisual() {
		CellEmitter.center(target.getPos()).burst(PoisonParticle.SPLASH, 5);
        target.showStatus(CharSprite.NEGATIVE, StringsManager.getVar(R.string.Char_StaPoisoned));
	}
}
