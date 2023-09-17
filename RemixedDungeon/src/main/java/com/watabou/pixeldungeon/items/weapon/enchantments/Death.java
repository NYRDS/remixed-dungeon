
package com.watabou.pixeldungeon.items.weapon.enchantments;

import com.watabou.pixeldungeon.Badges;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.effects.particles.ShadowParticle;
import com.watabou.pixeldungeon.items.weapon.Weapon;
import com.watabou.pixeldungeon.sprites.Glowing;
import com.watabou.utils.Random;

public class Death extends Weapon.Enchantment {

	private static final Glowing BLACK = new Glowing( 0x000000 );
	
	@Override
	public boolean proc( Weapon weapon, Char attacker, Char defender, int damage ) {
		// lvl 0 - 8%
		// lvl 1 ~ 9%
		// lvl 2 ~ 10%
		int level = Math.max( 0, weapon.level() );
		
		if (Random.Int( level + 100 ) >= 92) {
			
			defender.damage( defender.hp(), this );
			defender.getSprite().emitter().burst( ShadowParticle.UP, 5 );
			
			if (!defender.isAlive() && attacker instanceof Hero) {
				Badges.validateGrimWeapon();
			}
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	public Glowing glowing() {
		return BLACK;
	}
}
