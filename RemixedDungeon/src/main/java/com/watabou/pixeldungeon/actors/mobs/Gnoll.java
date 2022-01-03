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

import com.nyrds.pixeldungeon.mechanics.NamedEntityKind;
import com.nyrds.pixeldungeon.mobs.npc.ScarecrowNPC;
import com.watabou.pixeldungeon.actors.mobs.npcs.Ghost;
import com.watabou.pixeldungeon.items.Gold;

import org.jetbrains.annotations.NotNull;

public class Gnoll extends Mob {
	
	{
		hp(ht(12));
		baseDefenseSkill = 4;
		baseAttackSkill  = 11;
		dmgMin = 2;
		dmgMax = 5;
		dr = 2;

		exp = 2;
		maxLvl = 12;
		
		loot(Gold.class, 0.5f);
	}

	@Override
	public void die(@NotNull NamedEntityKind cause) {
		ScarecrowNPC.Quest.process( getPos() );
		Ghost.Quest.process( getPos() );
		super.die( cause );
	}

}
