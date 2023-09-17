
package com.watabou.pixeldungeon.items.weapon.enchantments;

import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.items.weapon.Weapon;
import com.watabou.pixeldungeon.items.weapon.Weapon.Enchantment;
import com.watabou.pixeldungeon.items.weapon.missiles.Boomerang;

public class Instability extends Weapon.Enchantment {
	
	@Override
	public boolean proc( Weapon weapon, Char attacker, Char defender, int damage ) {
		Enchantment ench = random();
		if (weapon instanceof Boomerang) {
			while (ench instanceof Piercing || ench instanceof Swing) {
				ench = Enchantment.random();
			}
		}
		return ench.proc( weapon, attacker, defender, damage );
	}
}
