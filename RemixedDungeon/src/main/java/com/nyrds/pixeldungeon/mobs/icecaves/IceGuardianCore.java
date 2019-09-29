package com.nyrds.pixeldungeon.mobs.icecaves;

import com.nyrds.pixeldungeon.items.icecaves.IceKey;
import com.nyrds.pixeldungeon.items.icecaves.WandOfIcebolt;
import com.nyrds.pixeldungeon.mechanics.NamedEntityKind;
import com.watabou.pixeldungeon.Badges;
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
import com.watabou.pixeldungeon.items.keys.SkeletonKey;
import com.watabou.pixeldungeon.items.weapon.enchantments.Death;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.utils.Random;

public class IceGuardianCore extends Boss {

	public IceGuardianCore() {
		hp(ht(1000));
		exp = 5;
		defenseSkill = 10;

		baseSpeed = 0.5f;
		loot = new WandOfIcebolt().upgrade(1);
		lootChance = 1.0f;

		addImmunity( Paralysis.class );
		addImmunity( ToxicGas.class );
		addImmunity( Terror.class );
		addImmunity( Death.class );
		addImmunity( Amok.class );
		addImmunity( Blindness.class );
		addImmunity( Sleep.class );
	}
	
	@Override
	public int damageRoll() {
		return Random.NormalIntRange( 13, 23 );
	}
	
	@Override
	public int attackSkill( Char target ) {
		return 26;
	}
	
	@Override
	public int dr() {
		return 11;
	}

	@Override
	public void die(NamedEntityKind cause) {
		super.die(cause);

		Level level = Dungeon.level;

		for (Mob mob : level.getCopyOfMobsArray()) {
			if (mob instanceof IceGuardian) {
				mob.die(cause);
			}
		}

		level.drop( new SkeletonKey(), getPos() ).sprite.drop();
		level.drop( new IceKey(), getPos() ).sprite.drop();

		Badges.validateBossSlain(Badges.Badge.ICE_GUARDIAN_SLAIN);
	}
}