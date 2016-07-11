package com.nyrds.pixeldungeon.mobs.necropolis;

import com.nyrds.pixeldungeon.mobs.common.MultiKindMob;
import com.watabou.pixeldungeon.actors.blobs.ToxicGas;
import com.watabou.pixeldungeon.actors.buffs.Amok;
import com.watabou.pixeldungeon.actors.buffs.Blindness;
import com.watabou.pixeldungeon.actors.buffs.Paralysis;
import com.watabou.pixeldungeon.actors.buffs.Sleep;
import com.watabou.pixeldungeon.actors.buffs.Terror;
import com.watabou.pixeldungeon.items.weapon.enchantments.Death;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;

public class RunicSkull extends MultiKindMob {

	protected boolean activated = false;
	private boolean zapping     = false;

	protected static final int RED_SKULL	 = 0;
	protected static final int BLUE_SKULL	 = 1;
	protected static final int GREEN_SKULL = 2;

	public RunicSkull() {

		hp(ht(50));
		EXP = 5;
		defenseSkill = 10;

		pacified = true;
		kind = Random.Int(3);
		state = WANDERING;

		IMMUNITIES.add( Paralysis.class );
		IMMUNITIES.add( ToxicGas.class );
		IMMUNITIES.add( Terror.class );
		IMMUNITIES.add( Death.class );
		IMMUNITIES.add( Amok.class );
		IMMUNITIES.add( Blindness.class );
		IMMUNITIES.add( Sleep.class );
	}

	static public RunicSkull makeNewSkull(int k) {
		RunicSkull skull = new RunicSkull();
		skull.kind = k;
		return skull;
	}

	public void Activate() {
		this.activated = true;
	}

	public void Deactivate() {
		this.activated = false;
	}

	@Override
	public boolean act()
	{
		if (activated){
			if (!zapping) {
				PlayZap();
				zapping = true;
			}
		} else{
			getSprite().idle();
			zapping = false;
		}
		return super.act();
	}

	@Override
	public int getKind() {
		return kind;
	}

	@Override
	protected boolean getCloser( int target ) {
		return false;
	}

	@Override
	protected boolean getFurther( int target ) {
		return false;
	}

	@Override
	public void onZapComplete() {
		PlayZap();
	}

	public void PlayZap() {
		getSprite().zap(
				getEnemy().getPos(),
				new Callback() {
					@Override
					public void call() {
					}
				});
	}
}
