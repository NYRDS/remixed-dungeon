package com.nyrds.pixeldungeon.mobs.necropolis;

import com.nyrds.pixeldungeon.ai.MobAi;
import com.nyrds.pixeldungeon.ai.Wandering;
import com.nyrds.pixeldungeon.mobs.common.MultiKindMob;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.blobs.ToxicGas;
import com.watabou.pixeldungeon.actors.buffs.Amok;
import com.watabou.pixeldungeon.actors.buffs.Blindness;
import com.watabou.pixeldungeon.actors.buffs.Paralysis;
import com.watabou.pixeldungeon.actors.buffs.Sleep;
import com.watabou.pixeldungeon.actors.buffs.Stun;
import com.watabou.pixeldungeon.actors.buffs.Terror;
import com.watabou.pixeldungeon.items.weapon.enchantments.Death;
import com.watabou.utils.Random;

import org.jetbrains.annotations.NotNull;

public class RunicSkull extends MultiKindMob {

	private boolean activated = false;
	private boolean zapping   = false;

	protected static final int RED_SKULL	 = 0;
	protected static final int BLUE_SKULL	 = 1;
	protected static final int GREEN_SKULL   = 2;
	protected static final int PURPLE_SKULL  = 3;

	public RunicSkull() {

		hp(ht(70));
		exp = 5;
		baseDefenseSkill = 15;
		baseAttackSkill = 1;

		pacified = true;
		kind = Random.Int(4);
		setState(MobAi.getStateByClass(Wandering.class));

		addImmunity( Paralysis.class );
		addImmunity( Stun.class );
		addImmunity( ToxicGas.class );
		addImmunity( Terror.class );
		addImmunity( Death.class );
		addImmunity( Amok.class );
		addImmunity( Blindness.class );
		addImmunity( Sleep.class );
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
				getSprite().zap(getPos());
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
	public boolean getCloser(int target) {
		return false;
	}

	@Override
    public boolean getFurther(int target) {
		return false;
	}

	@Override
	public boolean zap(@NotNull Char enemy){
		return false;
	}

	@Override
	public boolean canBePet(){
		return false;
	}
}
