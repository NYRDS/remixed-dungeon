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
package com.watabou.pixeldungeon.items.scrolls;

import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Sample;
import com.watabou.pixeldungeon.Assets;
import com.nyrds.retrodungeon.ml.R;
import com.watabou.pixeldungeon.actors.buffs.Invisibility;
import com.watabou.pixeldungeon.actors.buffs.Weakness;
import com.watabou.pixeldungeon.actors.hero.Belongings;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.effects.Flare;
import com.watabou.pixeldungeon.effects.particles.ShadowParticle;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.utils.GLog;

import java.util.Iterator;

public class ScrollOfRemoveCurse extends Scroll {

	private static final String TXT_PROCCED     = Game.getVar(R.string.ScrollOfRemoveCurse_Proced);
	private static final String TXT_NOT_PROCCED = Game.getVar(R.string.ScrollOfRemoveCurse_NoProced);

	@Override
	protected void doRead() {

		new Flare(6, 32).show(getCurUser().getSprite(), 2f);
		Sample.INSTANCE.play(Assets.SND_READ);
		Invisibility.dispel(getCurUser());

		boolean procced = uncurse(getCurUser().belongings);

		Weakness.detach(getCurUser(), Weakness.class);

		if (procced) {
			GLog.p(TXT_PROCCED);
			getCurUser().getSprite().emitter().start(ShadowParticle.UP, 0.05f, 10);
		} else {
			GLog.i(TXT_NOT_PROCCED);
		}

		setKnown();

		getCurUser().spendAndNext(TIME_TO_READ);
	}

	public static void uncurse(Hero hero, Item ... items) {

		boolean procced = false;
		for(Item item:items) {
			procced = uncurseItem(procced, item);
		}

		if (procced) {
			hero.getSprite().emitter().start(ShadowParticle.UP, 0.05f, 10);
		}
	}

	private static boolean uncurseItem(boolean procced, Item item) {
		if (item != null && item.cursed) {
			item.cursed = false;
			procced = true;
		}
		return procced;
	}

	private static boolean uncurse(Belongings belongings) {

		Iterator<Item> itemIterator = belongings.iterator();

		boolean procced = false;
		while (itemIterator.hasNext()) {
			procced = uncurseItem(procced, itemIterator.next());
		}

		return procced;
	}

	@Override
	public int price() {
		return isKnown() ? 30 * quantity() : super.price();
	}
}
