
package com.watabou.pixeldungeon.items.weapon.missiles;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Cripple;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.utils.Random;

public class Javelin extends MissileWeapon {
	
	public Javelin() {
		this( 1 );
	}
	
	public Javelin( int number ) {
		super();

		image = ItemSpriteSheet.JAVELIN;
		
		setSTR(15);
		
		MIN = 2;
		MAX = 15;
		
		quantity(number);
	}
	
	@Override
	public void attackProc(Char attacker, Char defender, int damage ) {
		super.attackProc( attacker, defender, damage );
		Buff.prolong( defender, Cripple.class, Cripple.DURATION );
	}
	
	@Override
	public String desc() {
        return StringsManager.getVar(R.string.Javelin_Info);
    }
	
	@Override
	public Item random() {
		quantity(Random.Int( 5, 15 ));
		return this;
	}
	
	@Override
	public int price() {
		return 15 * quantity();
	}
}
