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
package com.watabou.pixeldungeon.actors.mobs;

import com.nyrds.pixeldungeon.ai.MobAi;
import com.nyrds.pixeldungeon.ai.ThiefFleeing;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.utils.ItemsList;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.CharUtils;
import com.watabou.pixeldungeon.items.Gold;
import com.watabou.pixeldungeon.items.rings.RingOfHaggler;
import com.watabou.pixeldungeon.utils.Utils;

import org.jetbrains.annotations.NotNull;

import lombok.val;

public class Thief extends Mob {
	{
		hp(ht(20));
		baseDefenseSkill = 12;
		baseAttackSkill  = 12;
		dmgMin = 1;
		dmgMax = 7;
		dr = 3;

		exp = 5;
		maxLvl = 10;
		
		loot(RingOfHaggler.class, 0.01f);
	}

	@Override
	protected float _attackDelay() {
		return 0.5f;
	}

	@Override
	public int attackProc(@NotNull Char enemy, int damage ) {
		if (CharUtils.steal(this, enemy )) {
			setState(MobAi.getStateByClass(ThiefFleeing.class));
		}
		
		return damage;
	}
	
	@Override
	public int defenseProc(Char enemy, int damage) {
		if (getState() instanceof ThiefFleeing) {
			new Gold().doDrop(this);
		}
		
		return damage;
	}

	@Override
	public String getDescription() {
        String desc = StringsManager.getVar(R.string.Thief_Desc);
		val item = getBelongings().randomUnequipped();
		if (item != ItemsList.DUMMY) {
            desc += Utils.format(R.string.Thief_Carries,
						Utils.capitalize( this.getName() ), item.name() );
		}
		
		return desc;
	}
}
