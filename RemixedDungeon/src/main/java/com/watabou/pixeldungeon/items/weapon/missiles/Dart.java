
package com.watabou.pixeldungeon.items.weapon.missiles;

import com.watabou.pixeldungeon.items.Item;
import com.watabou.utils.Random;

public class Dart extends MissileWeapon {

	{
		image = 1;
		imageFile = "items/ammo.png";

		MIN = 1;
		MAX = 4;
	}
	
	public Dart() {
		this( 1 );
	}
	
	public Dart( int number ) {
		super();
		quantity(number);
	}

	@Override
	public Item random() {
		quantity(Random.Int( 5, 15 ));
		return this;
	}
	
	@Override
	public int price() {
		return quantity() * 2;
	}
}
