package com.nyrds.retrodungeon.mobs.guts;

import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.blobs.ToxicGas;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Levitation;
import com.watabou.pixeldungeon.actors.buffs.Paralysis;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.items.keys.SkeletonKey;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

public class MimicAmulet extends Mob {

	private int level;

	public MimicAmulet() {
		
		baseSpeed = 1.25f;
		
		flying = true;

		level = Dungeon.depth;
		
		IMMUNITIES.add( ToxicGas.class );
		IMMUNITIES.add( Paralysis.class );
	}
	
	private static final String LEVEL	= "level";
	
	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle(bundle);
		bundle.put(LEVEL, level);
	}
	
	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle(bundle);
		adjustStats(bundle.getInt(LEVEL));
	}
	
	@Override
	public int damageRoll() {
		return Random.NormalIntRange( ht() / 10, ht() / 4 );
	}
	
	@Override
	public int attackSkill( Char target ) {
		return 9 + level;
	}
	
	@Override
	public void die(Object cause) {
		super.die(cause);
		Dungeon.level.drop(new SkeletonKey(), getPos());
	}

	@Override
	protected boolean act() {
		if(buff(Levitation.class)==null) {
			Buff.affect(this, Levitation.class, 1000000);
		}
		return super.act();
	}

	public void adjustStats( int level ) {
		this.level = level;

		hp(ht((3 + level) * 5));
		exp = 2 + 2 * (level - 1) / 5;
		defenseSkill = 2 * attackSkill( null ) / 3;
		
		enemySeen = true;
	}

	@Override
	public boolean canBePet() {
		return false;
	}
}
