
package com.watabou.pixeldungeon.items.armor.glyphs;

import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Camera;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.effects.Lightning;
import com.watabou.pixeldungeon.items.armor.Armor;
import com.watabou.pixeldungeon.items.armor.Armor.Glyph;
import com.watabou.pixeldungeon.levels.traps.LightningTrap;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.sprites.Glowing;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.utils.Random;

public class Potential extends Glyph {

	private static final Glowing BLUE = new Glowing( 0x66CCEE );
	
	@Override
	public int defenceProc(Armor armor, Char attacker, Char defender, int damage) {

		int level = Math.max( 0, armor.level() );
		
		if (attacker.adjacent(defender) && Random.Int( level + 7 ) >= 6) {
			
			int dmg = Random.IntRange( 1, damage );
			attacker.damage( dmg, LightningTrap.LIGHTNING );
			dmg = Random.IntRange( 1, dmg );
			defender.damage( dmg, LightningTrap.LIGHTNING );
			
			checkOwner( defender );
			if (defender == Dungeon.hero) {
				Camera.main.shake( 2, 0.3f );
			}
			
			int[] points = {attacker.getPos(), defender.getPos()};
			GameScene.addToMobLayer( new Lightning( points ) );
		}
		
		return damage;
	}
	
	@Override
	public String name( String weaponName) {
        return Utils.format(R.string.Potential_Txt, weaponName );
	}

	@Override
	public Glowing glowing() {
		return BLUE;
	}
}
