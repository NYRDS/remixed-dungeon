package com.watabou.pixeldungeon.items.weapon.missiles;

import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Burning;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.weapon.melee.Bow;
import com.watabou.utils.Random;


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
	public Item random() {
		quantity = Random.Int( 5, 15 );
		return this;
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
	
	@Override
	public Item burn(int cell) {
		return null;
	}
}
