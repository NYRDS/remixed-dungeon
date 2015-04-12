package com.watabou.pixeldungeon.items.weapon.missiles;


public abstract class Arrow extends MissileWeapon {

	public Arrow() {
		this( 1 );
	}
	
	public Arrow( int number ) {
		super();
		quantity = number;
	}
	
	@Override
	protected void onThrow( int cell ) {
		if (! curUser.bowEquiped()) {
			miss( cell );
		} else {
			super.onThrow(cell);
		}
	}
	
}
