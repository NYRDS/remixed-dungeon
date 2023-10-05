
package com.watabou.pixeldungeon.items.weapon.missiles;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.utils.Random;

public class Shuriken extends MissileWeapon {

	public Shuriken() {
		this( 1 );
	}
	
	public Shuriken( int number ) {
		super();
		
		image = ItemSpriteSheet.SHURIKEN;
		
		setSTR(13);
		
		MIN = 2;
		MAX = 6;
		
		DLY = 0.5f;
		
		quantity(number);
	}
	
	@Override
	public String desc() {
        return StringsManager.getVar(R.string.Shuriken_Info);
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
	
	@Override
	public boolean isFliesStraight() {
		return false;
	}

	@Override
	public boolean isFliesFastRotating() {
		return true;
	}
}
