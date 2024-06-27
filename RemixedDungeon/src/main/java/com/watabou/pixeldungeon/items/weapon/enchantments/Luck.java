
package com.watabou.pixeldungeon.items.weapon.enchantments;

import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.items.weapon.Weapon;
import com.watabou.pixeldungeon.sprites.Glowing;

public class Luck extends Weapon.Enchantment {
	
	private static final Glowing GREEN = new Glowing( 0x00FF00 );
	
	@Override
	public boolean proc( Weapon weapon, Char attacker, Char defender, int damage ) {
		int level = Math.max( 0, weapon.level() );
		
		int dmg = damage;
		for (int i=1; i <= level+1; i++) {
			dmg = Math.max( dmg, attacker.damageRoll() - i );
		}
		
		if (dmg > damage) {
			defender.damage( dmg - damage, this );
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	public Glowing glowing() {
		return GREEN;
	}
}
