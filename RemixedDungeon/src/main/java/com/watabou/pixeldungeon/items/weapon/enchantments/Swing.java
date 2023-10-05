
package com.watabou.pixeldungeon.items.weapon.enchantments;

import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.items.weapon.Weapon;
import com.watabou.pixeldungeon.items.weapon.Weapon.Enchantment;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.utils.Random;

public class Swing extends Enchantment {
	
	@Override
	public boolean proc( Weapon weapon, Char attacker, Char defender, int damage ) {
		
		int level = Math.max( 0, weapon.level() );
		
		int maxDamage = (int)(damage * Math.pow( 2, -1d / (level + 1) ));
		if (maxDamage >= 1) {
			
			int p = attacker.getPos();

			for (int n : Level.NEIGHBOURS8) {
				Char ch = Actor.findChar( n + p );
				if (ch != null && ch != defender && ch.isAlive()) {
					
					int dr = ch.defenceRoll(attacker);
					int dmg = Random.Int( 1, maxDamage );
					int effectiveDamage = Math.max( dmg - dr, 0 );
					
					ch.damage( effectiveDamage, this );
					
					ch.getSprite().bloodBurstA( attacker.getSprite().center(), effectiveDamage );
					ch.getSprite().flash();
				}
			}
			
			return true;
			
		} else {
		
			return false;
			
		}
	}
}
