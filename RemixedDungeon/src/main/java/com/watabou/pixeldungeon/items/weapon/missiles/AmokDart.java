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
package com.watabou.pixeldungeon.items.weapon.missiles;

import com.nyrds.pixeldungeon.mechanics.buffs.RageBuff;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Amok;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Speed;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.utils.Random;

public class AmokDart extends Dart {

	public AmokDart() {
		this( 1 );
	}

	public AmokDart(int number ) {
		super();

		image = 16;
		
		setSTR(14);
		
		MIN = 1;
		MAX = 3;
		
		quantity(number);
	}
	
	@Override
	public void attackProc(Char attacker, Char defender, int damage ) {
		Buff.affect(defender, Amok.class,20);
		Buff.affect(defender, Speed.class,20);
		Buff rage = new RageBuff();
		rage.attachTo(defender);

		super.attackProc( attacker, defender, damage );
	}

	@Override
	public Item random() {
		quantity(Random.Int( 2, 5 ));
		return this;
	}
	
	@Override
	public int price() {
		return 12 * quantity();
	}
}
