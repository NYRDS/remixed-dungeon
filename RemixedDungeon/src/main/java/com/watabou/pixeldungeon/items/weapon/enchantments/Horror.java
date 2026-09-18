
package com.watabou.pixeldungeon.items.weapon.enchantments;
import com.nyrds.pixeldungeon.mechanics.buffs.BuffFactory;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.CharUtils;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.items.weapon.Weapon;
import com.watabou.pixeldungeon.sprites.Glowing;
import com.watabou.utils.Random;

public class Horror extends Weapon.Enchantment {

	private static final Glowing GREY = new Glowing( 0x222222 );
	
	@Override
	public boolean proc( Weapon weapon, Char attacker, Char defender, int damage ) {
		// lvl 0 - 20%
		// lvl 1 - 33%
		// lvl 2 - 43%
		int level = Math.max( 0, weapon.level() );
		
		if (Random.Int( level + 5 ) >= 4) {
			
			if (defender == Dungeon.hero) {
				Buff.affect( defender, BuffFactory.VERTIGO, CharUtils.durationFactor(defender) * 10f );
			} else {
				Buff.affect( defender, BuffFactory.TERROR, 10f );
			}
			
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	public Glowing glowing() {
		return GREY;
	}

}
