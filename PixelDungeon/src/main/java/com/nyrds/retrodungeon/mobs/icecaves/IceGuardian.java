package com.nyrds.retrodungeon.mobs.icecaves;

import com.nyrds.retrodungeon.mobs.common.MultiKindMob;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.blobs.ToxicGas;
import com.watabou.pixeldungeon.actors.buffs.Amok;
import com.watabou.pixeldungeon.actors.buffs.Blindness;
import com.watabou.pixeldungeon.actors.buffs.Paralysis;
import com.watabou.pixeldungeon.actors.buffs.Sleep;
import com.watabou.pixeldungeon.actors.buffs.Terror;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.items.food.FrozenCarpaccio;
import com.watabou.pixeldungeon.items.weapon.enchantments.Death;
import com.watabou.utils.Random;

public class IceGuardian extends MultiKindMob {

	public IceGuardian() {
		hp(ht(70));
		exp = 5;
		defenseSkill = 30;

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
		return Random.NormalIntRange( 10, 15 );
	}
	
	@Override
	public int attackSkill( Char target ) {
		return 31;
	}
	
	@Override
	public int dr() {
		return 14;
	}


	@Override
	public void die(Object cause) {
		super.die(cause);
		for (Mob mob : (Iterable<Mob>) Dungeon.level.mobs.clone()) {
			if (mob instanceof IceGuardianCore) {
				mob.damage(150,cause);
				if (mob.isAlive()){
					ressurrect();
					ressurrect();
				}
			}
		}
	}

}
