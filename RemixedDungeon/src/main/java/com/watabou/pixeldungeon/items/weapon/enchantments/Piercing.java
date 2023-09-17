
package com.watabou.pixeldungeon.items.weapon.enchantments;

import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.items.weapon.Weapon;
import com.watabou.pixeldungeon.items.weapon.Weapon.Enchantment;
import com.watabou.utils.Random;

public class Piercing extends Enchantment {
	
	@Override
	public boolean proc( Weapon weapon, Char attacker, Char defender, int damage ) {
		
		int level = Math.max( 0, weapon.level() );
		
		int maxDamage = (int)(damage * Math.pow( 2, -1d / (level + 1) ));
		if (maxDamage >= 1) {
			
			int d = defender.getPos() - attacker.getPos();
			int pos = defender.getPos() + d;
			
			do {
				
				Char ch = Actor.findChar( pos );
				if (ch == null) {
					break;
				}
				
				int dr = ch.defenceRoll(attacker);
				int dmg = Random.Int( 1, maxDamage );
				int effectiveDamage = Math.max( dmg - dr, 0 );
				
				ch.damage( effectiveDamage, this );
				
				ch.getSprite().bloodBurstA( attacker.getSprite().center(), effectiveDamage );
				ch.getSprite().flash();
				
				pos += d;
			} while (pos >= 0 && pos < Dungeon.level.getLength());
			
			return true;
			
		} else {
		
			return false;
			
		}
	}
}
