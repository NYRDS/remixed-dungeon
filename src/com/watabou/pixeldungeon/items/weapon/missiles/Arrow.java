package com.watabou.pixeldungeon.items.weapon.missiles;

import com.watabou.pixeldungeon.items.weapon.melee.Bow;


public abstract class Arrow extends MissileWeapon {

	protected double baseAcu = 1;
	protected double baseDly = 1;
	protected double baseMax = 1;
	protected double baseMin = 1;
	
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
			
			bow.usedForHit();
			bow.useArrowType(this);
			
			super.onThrow(cell);
		} else {
			miss( cell );
		}
	}
	
}
