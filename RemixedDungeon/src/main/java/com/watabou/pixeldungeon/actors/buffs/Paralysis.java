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

import com.nyrds.pixeldungeon.mechanics.NamedEntityKind;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.CharUtils;
import com.watabou.pixeldungeon.items.rings.RingOfElements.Resistance;
import com.watabou.pixeldungeon.sprites.CharSprite;
import com.watabou.pixeldungeon.ui.BuffIndicator;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.utils.Random;

import org.jetbrains.annotations.NotNull;

public class Paralysis extends FlavourBuff {

	private static final float DURATION	= 10f;
	
	@Override
	public boolean attachTo(@NotNull Char target ) {
		if (super.attachTo( target )) {
			target.paralyse(true);
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	public void detach() {
		target.paralyse(false);
		super.detach();
	}

	@Override
	public int damage(int damage, NamedEntityKind src) {
		if (Random.Int(damage) >= Random.Int(target.hp())) {
			detach();
			if (CharUtils.isVisible(target)) {
				GLog.i(StringsManager.getVar(R.string.Char_OutParalysis), target.getName_objective());
			}
		}

		return super.damage(damage, src);
	}

	@Override
	public int icon() {
		return BuffIndicator.PARALYSIS;
	}
	
	public static float duration( Char ch ) {
		Resistance r = ch.buff( Resistance.class );
		return r != null ? r.durationFactor() * DURATION : DURATION;
	}

	@Override
	public CharSprite.State charSpriteStatus() {
		return CharSprite.State.PARALYSED;
	}

	@Override
	public void attachVisual() {
		target.getSprite().add(CharSprite.State.PARALYSED);
        target.getSprite().showStatus(CharSprite.NEGATIVE, StringsManager.getVar(R.string.Char_StaParalysed));
	}
}
