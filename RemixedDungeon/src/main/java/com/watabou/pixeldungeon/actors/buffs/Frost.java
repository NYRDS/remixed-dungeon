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
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.potions.Potion;
import com.watabou.pixeldungeon.items.rings.RingOfElements.Resistance;
import com.watabou.pixeldungeon.sprites.CharSprite;
import com.watabou.pixeldungeon.ui.BuffIndicator;
import com.watabou.pixeldungeon.utils.Utils;

import org.jetbrains.annotations.NotNull;

public class Frost extends FlavourBuff {
	private static final float DURATION	= 5f;
	
	class freezeItem implements ItemAction {
		public Item act(Item srcItem){
			return srcItem.freeze(target.getPos());
		}
		public void carrierFx(){
		}

		public String actionText(Item srcItem) {
			if(srcItem instanceof Potion) {
                return Utils.format(StringsManager.getVar(R.string.Frost_Shatter), srcItem.toString());
			}
			return null;
		}
	}
	
	@Override
	public boolean attachTo(@NotNull Char target ) {
		if (super.attachTo( target )) {
			
			target.paralyse(true);
			Burning.detach( target, Burning.class );
			
			applyToCarriedItems(new freezeItem());
			
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
		detach();
		return super.damage(damage, src);
	}

	@Override
	public int icon() {
		return BuffIndicator.FROST;
	}
	
	public static float duration( Char ch ) {
		Resistance r = ch.buff( Resistance.class );
		return r != null ? r.durationFactor() * DURATION : DURATION;
	}

	@Override
	public CharSprite.State charSpriteStatus() {
		return CharSprite.State.FROZEN;
	}
}
