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

import com.nyrds.Packable;
import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.CommonActions;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Charm;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.effects.CellEmitter;
import com.watabou.pixeldungeon.effects.particles.ShaftParticle;
import com.watabou.pixeldungeon.items.potions.PotionOfHealing;
import com.watabou.pixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.pixeldungeon.ui.BuffIndicator;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.utils.Random;

public class Sungrass extends Plant {

	public Sungrass() {
		imageIndex = 4;
	}

	public void effect(int pos, Char ch) {
		if (ch != null) {
			Buff.affect(ch, Health.class);
		}

		if (Dungeon.visible[pos]) {
			CellEmitter.get(pos).start(ShaftParticle.FACTORY, 0.2f, 3);
		}
	}

	public static class Seed extends com.watabou.pixeldungeon.plants.Seed {
		{
			plantName = Game.getVar(R.string.Sungrass_Name);

			name = Utils.format(Game.getVar(R.string.Plant_Seed), plantName);
			image = ItemSpriteSheet.SEED_SUNGRASS;

			plantClass = Sungrass.class;
			alchemyClass = PotionOfHealing.class;
		}

		@Override
		public String desc() {
			return Game.getVar(R.string.Sungrass_Desc);
		}

		@Override
		public void execute(Hero hero, String action) {

			super.execute(hero, action);

			if (action.equals(CommonActions.AC_EAT)) {

				Buff.affect(hero, Charm.class, Charm.durationFactor(hero) * Random.IntRange(10, 15));

				hero.heal(Random.Int(0, Math.max((hero.ht() - hero.hp()) / 4, 15)), this);
			}
		}
	}

	public static class Health extends Buff {

		private static final float STEP = 5f;

		@Packable
		private int pos;

		@Override
		public boolean attachTo(Char target) {
			pos = target.getPos();
			return super.attachTo(target);
		}

		@Override
		public boolean act() {
			if (target.getPos() != pos || target.hp() >= target.ht()) {
				detach();
			} else {
				target.heal(Math.max( target.ht() / 10, 1),this);
			}
			spend(STEP);
			return true;
		}

		@Override
		public int icon() {
			return BuffIndicator.HEALING;
		}

		@Override
		public String toString() {
			return Game.getVar(R.string.Sungrass_Buff);
		}
	}
}
