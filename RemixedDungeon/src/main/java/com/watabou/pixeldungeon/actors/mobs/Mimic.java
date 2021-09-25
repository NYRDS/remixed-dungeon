/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
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


import com.nyrds.pixeldungeon.ai.Hunting;
import com.nyrds.pixeldungeon.ai.MobAi;
import com.nyrds.pixeldungeon.ai.Wandering;
import com.nyrds.pixeldungeon.mobs.common.IDepthAdjustable;
import com.nyrds.platform.audio.Sample;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.CharUtils;
import com.watabou.pixeldungeon.effects.CellEmitter;
import com.watabou.pixeldungeon.effects.Pushing;
import com.watabou.pixeldungeon.effects.Speck;
import com.watabou.pixeldungeon.items.Gold;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.scrolls.ScrollOfPsionicBlast;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.utils.Random;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class Mimic extends Mob implements IDepthAdjustable {

	private int level;

	public Mimic() {
		addImmunity(ScrollOfPsionicBlast.class);
		adjustStats(Dungeon.depth);
	}

	@Override
	public int attackProc(@NotNull Char enemy, int damage) {
		if (enemy == Dungeon.hero && Random.Int(3) == 0) {
			int gp = Random.Int(1, hp());
			if (gp > 0) {
				new Gold(gp).doDrop(this);
			}
		}
		return super.attackProc(enemy, damage);
	}

	public void adjustStats(int level) {
		this.level = level;

		hp(ht((3 + level) * 4));
		exp = 2 + 2 * (level - 1) / 5;
		baseDefenseSkill = 9 + level / 2;
		baseAttackSkill = 9 + level;
		dmgMin = ht()/10;
		dmgMax = ht()/4;

		enemySeen = true;
	}

	@Override
	public boolean reset() {
		setState(MobAi.getStateByClass(Wandering.class));
		return true;
	}

	public static Mimic spawnAt(int pos, List<Item> items) {
		Level level = Dungeon.level;
		Char ch = Actor.findChar(pos);
		if (ch != null) {
			int newPos = level.getEmptyCellNextTo(pos);

			if (level.cellValid(newPos)) {

				Actor.addDelayed(new Pushing(ch, ch.getPos(), newPos), -1);

				ch.setPos(newPos);
				level.press(newPos, ch);

			} else {
				return null;
			}
		}

		Mimic m = new Mimic();

		for(Item item:items) {
			m.collect(item);
		}
		m.hp(m.ht());
		m.setPos(pos);
		m.setState(MobAi.getStateByClass(Hunting.class));
		level.spawnMob(m, 1);

		m.getSprite().turnTo(pos, Dungeon.hero.getPos());

		if (CharUtils.isVisible(m)) {
			CellEmitter.get(pos).burst(Speck.factory(Speck.STAR), 10);
			Sample.INSTANCE.play(Assets.SND_MIMIC);
		}

		return m;
	}

	@Override
	public boolean canBePet() {
		return false;
	}
}
