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

import com.nyrds.pixeldungeon.items.Treasury;
import com.nyrds.pixeldungeon.levels.objects.LevelObject;
import com.nyrds.pixeldungeon.levels.objects.Presser;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Barkskin;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.hero.HeroSubClass;
import com.watabou.pixeldungeon.effects.CellEmitter;
import com.watabou.pixeldungeon.effects.particles.LeafParticle;
import com.watabou.pixeldungeon.items.Dewdrop;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.utils.Random;

public class Plant extends LevelObject {

	public Plant(int pos) {
		super(pos);
		textureFile = Assets.PLANTS;
	}

	public Plant(){
		this(Level.INVALID_CELL);
	}


	@Override
	public boolean stepOn(Char chr) {
		interact(chr);

		if (chr instanceof Hero) {
			Hero hero = (Hero) chr;
			hero.interrupt();
		}
		return true;
	}

	@Override
	public boolean interact(Char ch) {
		if (ch.getSubClass() == HeroSubClass.WARDEN) {
			Buff.affect(ch, Barkskin.class).level(ch.ht() / 3);

			if (Random.Int(5) == 0) {
				Treasury.getLevelTreasury().random(Treasury.Category.SEED).doDrop(ch);;
			}
			if (Random.Int(5) == 0) {
				new Dewdrop().doDrop(ch);
			}
		}

		return true;
	}


	@Override
	public void bump(Presser presser) {
		if(presser instanceof Char) {
			interact((Char)presser);
		}

		wither();
		effect(getPos(),presser);
	}

	private void wither() {
		Dungeon.level.remove(this);

		sprite.kill();
		if (Dungeon.isCellVisible(pos)) {
			CellEmitter.get(pos).burst(LeafParticle.GENERAL, 6);
		}
	}

	public String desc() {
		return Utils.getClassParam(this.getClass().getSimpleName(), "Desc", Utils.EMPTY_STRING, true);
	}

	@Override
	public String name() {
		return Utils.getClassParam(this.getClass().getSimpleName(), "Name", Utils.EMPTY_STRING, true);
	}

	public void effect(int pos, Presser ch) {
		
	}

	@Override
	public boolean nonPassable(Char ch) {
		return ch instanceof Hero;
	}

}
