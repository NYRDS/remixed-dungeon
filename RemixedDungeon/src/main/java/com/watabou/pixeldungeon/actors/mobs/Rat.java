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

import com.nyrds.pixeldungeon.ai.Fleeing;
import com.nyrds.pixeldungeon.ai.MobAi;
import com.nyrds.pixeldungeon.mechanics.NamedEntityKind;
import com.nyrds.pixeldungeon.mobs.npc.PlagueDoctorNPC;
import com.nyrds.pixeldungeon.mobs.npc.ScarecrowNPC;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Terror;
import com.watabou.pixeldungeon.actors.mobs.npcs.Ghost;
import com.watabou.pixeldungeon.effects.Flare;
import com.watabou.pixeldungeon.items.quest.RatSkull;

import org.jetbrains.annotations.NotNull;

public class Rat extends Mob {

	public Rat() {
		hp(ht(8));
		baseDefenseSkill = 3;
		baseAttackSkill  = 8;
		dmgMin = 1;
		dmgMax = 5;
		dr = 1;

		maxLvl = 7;
	}

	@Override
    public boolean canAttack(@NotNull Char enemy) {
		if(enemy.hasBuff(RatSkull.RatterAura.class)) {
			setState(MobAi.getStateByClass(Fleeing.class));
			if(!hasBuff(Terror.class)) {
				new Flare(5, 32).color(0xFF0000, true).show(getSprite(), 2f);
				Buff.affect(this, Terror.class, Terror.DURATION);
				return false;
			}
		}
		return super.canAttack(enemy);
}

	@Override
	public void die(@NotNull NamedEntityKind cause) {
		ScarecrowNPC.Quest.process( getPos() );
		Ghost.Quest.process( getPos() );
		PlagueDoctorNPC.Quest.process( getPos() );
		
		super.die( cause );
	}

}
