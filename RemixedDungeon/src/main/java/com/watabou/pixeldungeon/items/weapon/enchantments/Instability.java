
package com.watabou.pixeldungeon.items.weapon.enchantments;

import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.items.weapon.Weapon;
import com.watabou.pixeldungeon.items.weapon.Weapon.Enchantment;

public class Instability extends Weapon.Enchantment {
	
	@Override
	public boolean proc( Weapon weapon, Char attacker, Char defender, int damage ) {
		Enchantment ench = random();
		if ("Boomerang".equals(weapon.getEntityKind())) {
			while (ench instanceof Piercing || ench instanceof Swing) {
				ench = Enchantment.random();
			}
		}
		return ench.proc( weapon, attacker, defender, damage );
	}
}
