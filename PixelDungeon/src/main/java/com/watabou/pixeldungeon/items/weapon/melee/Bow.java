package com.watabou.pixeldungeon.items.weapon.melee;

import com.nyrds.retrodungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.items.weapon.missiles.Arrow;

public abstract class Bow extends MeleeWeapon {

	protected Class <? extends Arrow> arrowType = Arrow.class;
	
	public Bow( int tier, float acu, float dly ) {
		super(tier, acu, dly);
	}
	
	public void useArrowType(Arrow arrow){
		arrowType = arrow.getClass();
	}
	
	public Class <? extends Arrow> arrowType() {
		return arrowType;
	}
	
	@Override
	public String info() {
		return Game.getVar(R.string.Bow_Info1) + desc();
	}
	
	@Override
	protected int max() {
		return min();
	}
	
	public double acuFactor() {
		return 1;
	}

	public double dlyFactor() {
		return 1;
	}
	
	public double dmgFactor() {
		return 1;
	}

	public void onMiss() {
	}
}
