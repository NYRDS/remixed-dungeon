package com.nyrds.pixeldungeon.items.common.armor;

import com.nyrds.pixeldungeon.mechanics.Necromancy;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.mobs.common.Deathling;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.hero.HeroClass;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.effects.Wound;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.armor.ClassArmor;
import com.watabou.pixeldungeon.plants.Sungrass;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;

import java.util.Collection;
import java.util.HashSet;

public class NecromancerRobe extends UsableArmor {

	private static final String TXT_NOT_NECROMANCER = Game.getVar(R.string.NecromancerArmor_NotNecromancer);
	private static final String AC_SPECIAL = Game.getVar(R.string.NecromancerArmor_ACSpecial);

	public NecromancerRobe() {
		super( 1 );
		image = 23;
	}

	@Override
	public String special() {
		return AC_SPECIAL;
	}

	@Override
	public void doSpecial() {
		Collection<Mob> pets = Dungeon.hero.getPets();

		int n = 0;
		for (Mob mob : pets){
			if (mob.isAlive() && mob instanceof Deathling) {
				n++;
			}
		}
		if (n == Necromancy.getLimit(this)){
			GLog.w( Necromancy.getLimitWarning(this) );
			return;
		}
		Necromancy.summonDeathling(this);
	}

	@Override
	public boolean doEquip( Hero hero ) {
		if (hero.heroClass == HeroClass.NECROMANCER) {
			return super.doEquip( hero );
		} else {
			GLog.w( TXT_NOT_NECROMANCER );
			return false;
		}
	}

}