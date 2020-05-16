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
package com.watabou.pixeldungeon.items;

import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.hero.Belongings;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.utils.Random;

public class KindOfWeapon extends EquipableItem {

	public static final float TIME_TO_EQUIP = 1f;

	public static final String SWORD_ATTACK = "sword";
	public static final String SPEAR_ATTACK = "spear";
	public static final String BOW_ATTACK   = "bow";
	public static final String STAFF_ATTACK = "staff";
	public static final String HEAVY_ATTACK = "heavy";
	public static final String WAND_ATTACK  = STAFF_ATTACK;
	public static final String KUSARIGAMA_ATTACK  = "kusarigama";
    public static final String CROSSBOW_ATTACK = "crossbow";

    protected String animation_class = NO_ANIMATION;

	public int		MIN	= 0;
	public int		MAX = 1;

	@Override
	public Belongings.Slot slot(Belongings belongings) {

		if(belongings.slotBlocked(Belongings.Slot.WEAPON)
				&& belongings.slotBlocked(Belongings.Slot.LEFT_HAND)) {
			return Belongings.Slot.WEAPON;
		}

		if(belongings.slotBlocked(Belongings.Slot.WEAPON)) {
			return Belongings.Slot.LEFT_HAND;
		}
		return Belongings.Slot.WEAPON;
	}

	public int damageRoll(Char user) {
		return Random.NormalIntRange( MIN, MAX );
	}

	@Override
	public String getAttackAnimationClass() {
		return animation_class;
	}

	@Override
	public void equippedCursed() {
		GLog.n(Game.getVar(R.string.KindOfWeapon_EquipCursed), name());
	}

	@Override
	public boolean goodForMelee() {
		return true;
	}
}
