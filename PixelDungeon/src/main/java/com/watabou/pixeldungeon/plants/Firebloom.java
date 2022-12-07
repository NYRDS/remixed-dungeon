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

import com.nyrds.retrodungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.blobs.Blob;
import com.watabou.pixeldungeon.actors.blobs.Fire;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Burning;
import com.watabou.pixeldungeon.actors.buffs.Speed;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.effects.CellEmitter;
import com.watabou.pixeldungeon.effects.particles.FlameParticle;
import com.watabou.pixeldungeon.items.food.Food;
import com.watabou.pixeldungeon.items.potions.PotionOfLiquidFlame;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.pixeldungeon.utils.Utils;

public class Firebloom extends Plant {

	private static final String TXT_NAME = Game.getVar(R.string.Firebloom_Name);
	private static final String TXT_DESC = Game.getVar(R.string.Firebloom_Desc);

	public Firebloom() {
		image = 0;
		plantName = TXT_NAME;
	}

	public void effect(int pos, Char ch) {
		GameScene.add(Blob.seed(pos, 2, Fire.class));

		if (Dungeon.visible[pos]) {
			CellEmitter.get(pos).burst(FlameParticle.FACTORY, 5);
		}
	}

	@Override
	public String desc() {
		return TXT_DESC;
	}

	public static class Seed extends Plant.Seed {
		{
			plantName = TXT_NAME;

			name = Utils.format(TXT_SEED, plantName);
			image = ItemSpriteSheet.SEED_FIREBLOOM;

			plantClass = Firebloom.class;
			alchemyClass = PotionOfLiquidFlame.class;
		}

		@Override
		public String desc() {
			return TXT_DESC;
		}

		@Override
		public void execute(Hero hero, String action) {

			super.execute(hero, action);

			if (action.equals(Food.AC_EAT)) {
				Buff.affect(hero, Burning.class).reignite(hero);
				Buff.affect(hero, Speed.class, Speed.DURATION);
			}
		}
	}
}
