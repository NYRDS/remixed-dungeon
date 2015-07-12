package com.watabou.pixeldungeon.items.weapon.missiles;

import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.hero.HeroClass;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.weapon.melee.Bow;
import com.watabou.utils.Random;


public abstract class Arrow extends MissileWeapon {

	protected double baseAcu = 1;
	protected double baseDly = 1;
	protected double baseMax = 1;
	protected double baseMin = 1;
	
	protected Bow firedFrom;
	
	public Arrow() {
		this( 1 );
	}
	
	public Arrow( int number ) {
		super();
		STR = 9;
		quantity(number);
	
	}
	
	protected void updateStatsForInfo() {
		MAX = (int) baseMax;
		MIN = (int) baseMin;
		ACU = (float) baseAcu;
		DLY = (float) baseDly;
	}
	
	@Override
	public Item random() {
		quantity(Random.Int( 10, 25 ));
		return this;
	}
	
	@Override
	protected void onThrow( int cell ) {
		if (curUser.bowEquiped()) {
			
			if(Dungeon.level.adjacent(curUser.pos, cell) && curUser.heroClass != HeroClass.ELF) {
				miss( cell );
				return;
			}
			
			firedFrom = (Bow)curUser.belongings.weapon;
			
			MAX = (int) (baseMax * firedFrom.dmgFactor());
			MIN = (int) (baseMin * firedFrom.dmgFactor());
			ACU = (float) (baseAcu * firedFrom.acuFactor());
			DLY = (float) (baseDly * firedFrom.dlyFactor());
			
			float sDelta = curUser.effectiveSTR() - firedFrom.STR;
			
			if (sDelta < 0) {
				DLY += sDelta * 0.5;
				ACU -= sDelta * 0.1;
			}
			
			if (sDelta > 2) {
				MAX += MIN;
			}
			
			if (curUser.heroClass == HeroClass.ELF) {
				ACU *= 1.1;
				DLY *= 0.9;
			}
			
			firedFrom.usedForHit();
			firedFrom.useArrowType(this);
			
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
