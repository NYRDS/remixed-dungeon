
package com.watabou.pixeldungeon.items.armor.glyphs;

import com.nyrds.pixeldungeon.ml.R;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.items.armor.Armor;
import com.watabou.pixeldungeon.items.armor.Armor.Glyph;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.utils.Random;

import org.jetbrains.annotations.NotNull;

public class Bounce extends Glyph {

	@Override
	public int defenceProc(Armor armor, @NotNull Char attacker, Char defender, int damage) {

		int armorLevel = Math.max( 0, armor.level() );

		if (attacker.isMovable() && attacker.adjacent(defender) && Random.Int( armorLevel + 5) >= 4) {
			attacker.push(defender);
		}
		
		return damage;
	}
	
	@Override
	public String name( String armorName) {
        return Utils.format(R.string.Bounce_Txt, armorName );
	}

}
