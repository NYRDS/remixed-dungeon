package com.nyrds.pixeldungeon.mobs.guts;

import com.nyrds.Packable;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.blobs.ToxicGas;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Levitation;
import com.watabou.pixeldungeon.actors.buffs.Paralysis;
import com.watabou.pixeldungeon.actors.buffs.Stun;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.items.keys.SkeletonKey;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

public class MimicAmulet extends Mob {

	@Packable
	private int level;

	public MimicAmulet() {
		
		baseSpeed = 1.25f;
		
		flying = true;

		level = Dungeon.depth;
		
		addImmunity( ToxicGas.class );
		addImmunity( Paralysis.class );
		addImmunity( Stun.class );

		collect(new SkeletonKey());
	}

	
	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle(bundle);

		if(getBelongings().getItem(SkeletonKey.class)==null) {
			collect(new SkeletonKey());
		}

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

	@Override
    public boolean act() {
		if(!hasBuff(Levitation.class)) {
			Buff.affect(this, Levitation.class, 1000000);
		}
		return super.act();
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
