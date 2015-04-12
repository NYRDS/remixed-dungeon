package com.watabou.pixeldungeon.items.weapon.missiles;

import com.watabou.pixeldungeon.items.weapon.melee.Bow;


public abstract class Arrow extends MissileWeapon {

	protected float baseAcu = 1f;
	protected float baseDly = 1f;
	protected float baseMax = 1f;
	protected float baseMin = 1f;
	
	public Arrow() {
		this( 1 );
	}
	
	public Arrow( int number ) {
		super();
		quantity = number;
	
	}
	
	@Override
	protected void onThrow( int cell ) {
		if (curUser.bowEquiped()) {
			Bow bow = (Bow)curUser.belongings.weapon;
			
			MAX = (int) (baseMax * bow.dmgFactor());
			MIN = (int) (baseMin * bow.dmgFactor());
			ACU = (float) (baseAcu * bow.acuFactor());
			DLY = (float) (baseDly * bow.dlyFactor());
			
			super.onThrow(cell);
		} else {
			miss( cell );
		}
	}
	
}
