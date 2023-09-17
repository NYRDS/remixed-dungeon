
package com.watabou.pixeldungeon.actors.mobs;

import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Bleeding;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.utils.Random;

import org.jetbrains.annotations.NotNull;

public class Albino extends Rat {

	{
		hp(ht(15));
	}

	@Override
	public int attackProc(@NotNull Char enemy, int damage ) {
		if (Random.Int( 2 ) == 0) {
			Buff.affect( enemy, Bleeding.class ).level( damage );
		}
		
		return damage;
	}
}
