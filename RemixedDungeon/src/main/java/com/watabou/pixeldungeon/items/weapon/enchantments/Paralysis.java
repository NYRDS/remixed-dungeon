
package com.watabou.pixeldungeon.items.weapon.enchantments;

import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Stun;
import com.watabou.pixeldungeon.items.weapon.Weapon;
import com.watabou.pixeldungeon.sprites.Glowing;
import com.watabou.utils.Random;

public class Paralysis extends Weapon.Enchantment {
	
	private static Glowing YELLOW = new Glowing( 0xCCAA44 );
	
	@Override
	public boolean proc( Weapon weapon, Char attacker, Char defender, int damage ) {
		// lvl 0 - 13%
		// lvl 1 - 22%
		// lvl 2 - 30%
		int level = Math.max( 0, weapon.level() );
		
		if (Random.Int( level + 8 ) >= 7) {
			
			Buff.prolong( defender, Stun.class,
				Random.Float( 1, 1.5f + level ) );
			
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	public Glowing glowing() {
		return YELLOW;
	}
}
