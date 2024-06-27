
package com.watabou.pixeldungeon.items.armor.glyphs;

import com.nyrds.pixeldungeon.ml.R;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.items.armor.Armor;
import com.watabou.pixeldungeon.items.armor.Armor.Glyph;
import com.watabou.pixeldungeon.items.wands.WandOfBlink;
import com.watabou.pixeldungeon.sprites.Glowing;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.utils.Random;

public class Displacement extends Glyph {
	
	private static final Glowing BLUE = new Glowing( 0x66AAFF );
	
	@Override
	public int defenceProc(Armor armor, Char attacker, Char defender, int damage ) {

		if (Dungeon.bossLevel()) {
			return damage;
		}
		
		int nTries = (armor.level() < 0 ? 1 : armor.level() + 1) * 5;
		for (int i=0; i < nTries; i++) {
			int pos = Random.Int( Dungeon.level.getLength() );
			if (Dungeon.isCellVisible(pos) && Dungeon.level.passable[pos] && Actor.findChar( pos ) == null) {
				
				WandOfBlink.appear( defender, pos );
				Dungeon.level.press( pos, defender );
				defender.observe();

				break;
			}
		}
		
		return damage;
	}
	
	@Override
	public String name( String weaponName) {
        return Utils.format(R.string.Displacement_Txt, weaponName );
	}

	@Override
	public Glowing glowing() {
		return BLUE;
	}
}
