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
package com.watabou.pixeldungeon.plants;

import com.nyrds.pixeldungeon.levels.objects.Presser;
import com.nyrds.pixeldungeon.mechanics.CommonActions;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Charm;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.effects.CellEmitter;
import com.watabou.pixeldungeon.effects.particles.ShaftParticle;
import com.watabou.pixeldungeon.items.potions.PotionOfMana;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.utils.Random;

import org.jetbrains.annotations.NotNull;

public class Moongrace extends Plant {

	public Moongrace() {
		imageIndex = 8;
	}

	public void effect(int pos, Presser ch) {
		if (ch instanceof Char) {
			Buff.affect((Char)(ch), com.nyrds.pixeldungeon.mechanics.buffs.Moongrace.class);
		}

		if(ch instanceof Mob) {
			Mob mob = (Mob)ch;

			int cell = level().getEmptyCellNextTo(pos);
			if (level().cellValid(cell)) {
				mob.split(cell,0);
				if (Dungeon.visible[cell]) {
					CellEmitter.get(cell).start(ShaftParticle.FACTORY, 0.2f, 6);
				}
			}
		}

		if (Dungeon.visible[pos]) {
			CellEmitter.get(pos).start(ShaftParticle.FACTORY, 0.2f, 3);
		}
	}

	public static class Seed extends com.watabou.pixeldungeon.plants.Seed {
		{
            plantName = StringsManager.getVar(R.string.Moongrace_Name);

            name = Utils.format(StringsManager.getVar(R.string.Plant_Seed), plantName);
			image = 8;

			plantClass = Moongrace.class;
			alchemyClass = PotionOfMana.class;
		}

		@Override
		public String desc() {
            return StringsManager.getVar(R.string.Moongrace_Desc);
        }

		@Override
		public void _execute(@NotNull Char chr, @NotNull String action) {

			super._execute(chr, action);

			if (action.equals(CommonActions.AC_EAT)) {
				Buff.affect(chr, Charm.class, Charm.durationFactor(chr) * Random.IntRange(10, 15));
				chr.accumulateSkillPoints(Random.Int(0, Math.max((chr.getSkillPointsMax() - chr.getSkillPoints()) / 4, 15)));
			}
		}
	}

}
