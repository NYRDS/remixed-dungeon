
package com.watabou.pixeldungeon.items.weapon.enchantments;

import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.items.weapon.Weapon;
import com.watabou.pixeldungeon.sprites.Glowing;
import com.watabou.utils.Random;

public class Slow extends Weapon.Enchantment {
	
	private static Glowing BLUE = new Glowing( 0x0044FF );
	
	@Override
	public boolean proc( Weapon weapon, Char attacker, Char defender, int damage ) {
		// lvl 0 - 25%
		// lvl 1 - 40%
		// lvl 2 - 50%
		int level = Math.max( 0, weapon.level() );
		
		if (Random.Int( level + 4 ) >= 3) {
			
			Buff.prolong( defender, com.watabou.pixeldungeon.actors.buffs.Slow.class, 
				Random.Float( 1, 1.5f + level ) );
			
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	public Glowing glowing() {
		return BLUE;
	}	
}
