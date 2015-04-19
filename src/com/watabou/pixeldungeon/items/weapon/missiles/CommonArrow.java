
package com.watabou.pixeldungeon.items.weapon.missiles;

import com.watabou.pixeldungeon.sprites.ItemSpriteSheet;

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
		
		updateStatsForInfo();
	}
	
	@Override
	public int price() {
		return quantity * 3;
	}
}
