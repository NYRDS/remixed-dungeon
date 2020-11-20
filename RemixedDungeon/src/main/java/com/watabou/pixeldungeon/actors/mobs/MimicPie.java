package com.watabou.pixeldungeon.actors.mobs;

import com.nyrds.pixeldungeon.mobs.common.IDepthAdjustable;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.blobs.ToxicGas;
import com.watabou.pixeldungeon.actors.buffs.Paralysis;
import com.watabou.pixeldungeon.actors.buffs.Stun;
import com.watabou.pixeldungeon.items.food.RottenPasty;
import com.watabou.utils.Random;

public class MimicPie extends Mob implements IDepthAdjustable {
	
	private int level;
	
	public MimicPie() {
		baseSpeed = 1.25f;
		
		flying = true;

		level = Dungeon.depth;

		collect(new RottenPasty());

		addImmunity( ToxicGas.class );
		addImmunity( Paralysis.class );
		addImmunity( Stun.class );
		adjustStats(level);
	}

	@Override
	public int damageRoll() {
		return Random.NormalIntRange( ht() / 10, ht() / 4 );
	}
	
	@Override
	public int attackSkill( Char target ) {
		return 9 + level;
	}

	public void adjustStats( int level ) {
		this.level = level;

		hp(ht((3 + level) * 5));
		exp = 2 + 2 * (level - 1) / 5;
		baseDefenseSkill = 2 * attackSkill( null ) / 3;
		
		enemySeen = true;
	}

	@Override
	public boolean canBePet() {
		return false;
	}
}
