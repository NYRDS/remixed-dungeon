
package com.watabou.pixeldungeon.items.weapon.missiles;

import androidx.annotation.Keep;

public class CommonArrow extends Arrow {

	@Keep
	public CommonArrow() {
		this( 1 );
	}
	
	public CommonArrow( int number ) {
		super();
		quantity(number);
		
		baseMin = 2;
		baseMax = 5;
		baseDly = 0.75;
		
		image = COMMON_ARROW_IMAGE;
		
		updateStatsForInfo();
	}
		
	@Override
	public int price() {
		return quantity() * 3;
	}
}
