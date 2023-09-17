
package com.watabou.pixeldungeon.actors.mobs;

import com.watabou.pixeldungeon.actors.Char;
import com.watabou.utils.Random;

public class Acidic extends Scorpio {

	@Override
	public int defenseProc( Char enemy, int damage ) {
		
		int dmg = Random.IntRange( 0, damage );
		if (dmg > 0) {
			enemy.damage( dmg, this );
		}
		
		return super.defenseProc( enemy, damage );
	}
}
