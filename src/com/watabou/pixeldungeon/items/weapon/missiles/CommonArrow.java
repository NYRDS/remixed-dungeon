
package com.watabou.pixeldungeon.items.weapon.missiles;

import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.utils.Random;

public class CommonArrow extends Arrow {

	public CommonArrow() {
		this( 1 );
	}
	
	public CommonArrow( int number ) {
		super();
		quantity = number;
		
		baseMin = 2;
		baseMax = 6;
		baseDly = 0.75;
		
		image = ItemSpriteSheet.ARROW_COMMON;
	}
		
	@Override
	public Item random() {
		quantity = Random.Int( 5, 15 );
		return this;
	}
	
	@Override
	public int price() {
		return quantity * 3;
	}
	
	@Override
	public Item burn(int cell) {
		return null;
	}
}
