package com.nyrds.pixeldungeon.mobs.icecaves;

import com.nyrds.pixeldungeon.items.icecaves.IceKey;
import com.nyrds.pixeldungeon.items.icecaves.WandOfIcebolt;
import com.nyrds.pixeldungeon.mechanics.NamedEntityKind;
import com.watabou.pixeldungeon.Badges;
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

import org.jetbrains.annotations.NotNull;

public class IceGuardianCore extends Boss {

	public IceGuardianCore() {
		hp(ht(1000));
		expForKill = 5;
		baseDefenseSkill = 10;
		baseAttackSkill  = 26;
		dmgMin = 13;
		dmgMax = 23;
		dr = 11;

		baseSpeed = 0.5f;
		collect(new WandOfIcebolt().upgrade(1));

		addImmunity( Paralysis.class );
		addImmunity( ToxicGas.class );
		addImmunity( Terror.class );
		addImmunity( Death.class );
		addImmunity( Amok.class );
		addImmunity( Blindness.class );
		addImmunity( Sleep.class );

		collect(new SkeletonKey());
		collect(new IceKey());

	}

	@Override
	public void die(@NotNull NamedEntityKind cause) {
		super.die(cause);

		for (Mob mob : level().getCopyOfMobsArray()) {
			if (mob instanceof IceGuardian) {
				mob.die(cause);
			}
		}

		Badges.validateBossSlain(Badges.Badge.ICE_GUARDIAN_SLAIN);
	}
}