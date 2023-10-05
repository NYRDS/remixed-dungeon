
package com.watabou.pixeldungeon.items.armor.glyphs;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.blobs.Blob;
import com.watabou.pixeldungeon.actors.blobs.ToxicGas;
import com.watabou.pixeldungeon.items.armor.Armor;
import com.watabou.pixeldungeon.items.armor.Armor.Glyph;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.sprites.Glowing;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.utils.Random;

public class Stench extends Glyph {

	private static Glowing GREEN = new Glowing( 0x22CC44 );
	
	@Override
	public int defenceProc(Armor armor, Char attacker, Char defender, int damage) {

		int level = Math.max( 0, armor.level() );
		
		if (attacker.adjacent(defender) && Random.Int( level + 5 ) >= 4) {
			GameScene.add( Blob.seed( attacker.getPos(), 20, ToxicGas.class ) );
		}
		
		return damage;
	}
	
	@Override
	public String name( String weaponName) {
        return Utils.format(R.string.Stench_Txt, weaponName );
	}
	
	@Override
	public Glowing glowing() {
		return GREEN;
	}

}
