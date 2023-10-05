
package com.watabou.pixeldungeon.actors.mobs;

import com.nyrds.pixeldungeon.mechanics.NamedEntityKind;
import com.watabou.pixeldungeon.actors.buffs.Amok;
import com.watabou.pixeldungeon.actors.buffs.Bleeding;
import com.watabou.pixeldungeon.actors.buffs.Sleep;
import com.watabou.pixeldungeon.actors.buffs.Terror;
import com.watabou.pixeldungeon.actors.mobs.npcs.Imp;
import com.watabou.pixeldungeon.items.scrolls.ScrollOfPsionicBlast;

import org.jetbrains.annotations.NotNull;

public class Golem extends Mob {
	
	public Golem() {

		hp(ht(85));
		baseDefenseSkill = 18;
		baseAttackSkill  = 28;
		dmgMin = 20;
		dmgMax = 40;
		dr = 12;
		
		exp = 12;
		maxLvl = 22;
		
		addResistance( ScrollOfPsionicBlast.class );
		
		addImmunity( Amok.class );
		addImmunity( Terror.class );
		addImmunity( Sleep.class );
		addImmunity( Bleeding.class );
	}

	@Override
	protected float _attackDelay() {
		return 1.5f;
	}

	@Override
	public void die(@NotNull NamedEntityKind cause) {
		Imp.Quest.process( this );
		
		super.die( cause );
	}
}
