
package com.watabou.pixeldungeon.items.armor.glyphs;

import com.nyrds.pixeldungeon.ml.R;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Hunger;
import com.watabou.pixeldungeon.items.armor.Armor;
import com.watabou.pixeldungeon.items.armor.Armor.Glyph;
import com.watabou.pixeldungeon.sprites.CharSprite;
import com.watabou.pixeldungeon.sprites.Glowing;
import com.watabou.pixeldungeon.ui.BuffIndicator;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.utils.Random;

public class Metabolism extends Glyph {

	private static Glowing RED = new Glowing( 0xCC0000 );
	
	@Override
	public int defenceProc(Armor armor, Char attacker, Char defender, int damage) {

		int level = Math.max( 0, armor.level() );
		if (Random.Int( level / 2 + 5 ) >= 4) {
			
			int healing = Math.min( defender.ht() - defender.hp(), Random.Int( 1, defender.ht() / 5 ) );

			if (healing > 0) {
				if (!defender.isStarving()) {

					defender.hunger().satisfy(-Hunger.STARVING / 10);
					BuffIndicator.refreshHero();

					defender.heal(healing,this);
					defender.showStatus( CharSprite.POSITIVE, Integer.toString( healing ) );
				}
			}
		}
		
		return damage;
	}
	
	@Override
	public String name( String weaponName) {
        return Utils.format(R.string.Metabolism_Txt, weaponName );
	}

	@Override
	public Glowing glowing() {
		return RED;
	}
}
