
package com.watabou.pixeldungeon.items.armor.glyphs;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.actors.mobs.npcs.MirrorImage;
import com.watabou.pixeldungeon.items.armor.Armor;
import com.watabou.pixeldungeon.items.armor.Armor.Glyph;
import com.watabou.pixeldungeon.items.wands.WandOfBlink;
import com.watabou.pixeldungeon.sprites.Glowing;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.utils.Random;

public class Multiplicity extends Glyph {

	private static Glowing PINK = new Glowing( 0xCCAA88 );
	
	@Override
	public int defenceProc(Armor armor, Char attacker, Char defender, int damage) {

		int level = Math.max( 0, armor.level() );
		
		if (Random.Int( level / 2 + 6 ) >= 5) {
			
			int imgCell = attacker.level().getEmptyCellNextTo(defender.getPos());

			if (attacker.level().cellValid(imgCell)) {
				if(defender instanceof Hero) {
					MirrorImage img = new MirrorImage((Hero) defender);
					WandOfBlink.appear( img, imgCell );
					defender.damage( Random.IntRange( 1, defender.ht() / 6 ), this );
				}

				if(defender instanceof Mob) {
					((Mob) defender).split(imgCell, damage);
				}

				checkOwner( defender );
			}
			
		}
		
		return damage;
	}
	
	@Override
	public String name( String weaponName) {
        return Utils.format(R.string.Multiplicity_Txt, weaponName );
	}

	@Override
	public Glowing glowing() {
		return PINK;
	}
}
