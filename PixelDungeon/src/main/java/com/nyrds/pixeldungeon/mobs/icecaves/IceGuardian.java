package com.nyrds.pixeldungeon.mobs.icecaves;

import com.nyrds.pixeldungeon.mobs.common.MultiKindMob;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.blobs.ToxicGas;
import com.watabou.pixeldungeon.actors.buffs.Amok;
import com.watabou.pixeldungeon.actors.buffs.Blindness;
import com.watabou.pixeldungeon.actors.buffs.Paralysis;
import com.watabou.pixeldungeon.actors.buffs.Sleep;
import com.watabou.pixeldungeon.actors.buffs.Terror;
import com.watabou.pixeldungeon.actors.mobs.Boss;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.items.food.FrozenCarpaccio;
import com.watabou.pixeldungeon.items.weapon.enchantments.Death;
import com.watabou.utils.Random;

public class IceGuardian extends MultiKindMob {

	public IceGuardian() {
		hp(ht(70));
		EXP = 5;
		defenseSkill = 10;

		kind = 1;

		baseSpeed = 0.7f;
		
		loot = new FrozenCarpaccio();
		lootChance = 0.2f;

		IMMUNITIES.add( Paralysis.class );
		IMMUNITIES.add( ToxicGas.class );
		IMMUNITIES.add( Terror.class );
		IMMUNITIES.add( Death.class );
		IMMUNITIES.add( Amok.class );
		IMMUNITIES.add( Blindness.class );
		IMMUNITIES.add( Sleep.class );
	}
	
	@Override
	public int damageRoll() {
		return Random.NormalIntRange( 5, 10 );
	}
	
	@Override
	public int attackSkill( Char target ) {
		return 30;
	}
	
	@Override
	public int dr() {
		return 11;
	}


	@Override
	public void die(Object cause) {
		super.die(cause);

		for (Mob mob : (Iterable<Mob>) Dungeon.level.mobs.clone()) {
			if (mob instanceof Boss && (mob instanceof IceGuardianCore)) {
				ressurrect();
				ressurrect();
				mob.damage(150,cause);
			}
		}
	}

}
