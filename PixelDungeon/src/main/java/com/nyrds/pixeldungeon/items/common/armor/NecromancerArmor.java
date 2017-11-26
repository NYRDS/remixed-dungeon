package com.nyrds.pixeldungeon.items.common.armor;

import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.hero.HeroClass;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.items.armor.ClassArmor;
import com.watabou.pixeldungeon.utils.GLog;

import java.util.Collection;
import java.util.HashSet;

public class NecromancerArmor extends ClassArmor {

	private static final String TXT_NOT_NECROMANCER = Game.getVar(R.string.NecromancerArmor_NotNecromancer);
	private static final String AC_SPECIAL = Game.getVar(R.string.Necromancer_ACSpecial);

	public HashSet<Mob> pets   = new HashSet<>();

	public NecromancerArmor() {
		image = 22;
	}

	public String desc() {
		return info2;
	}

	@Override
	public String special() {
		return AC_SPECIAL;
	}

	@Override
	public void doSpecial() {
		//TODO: Let's create a "Necrotism" ability, shall we? So it works fairly simple
		//TODO: The caster get a "Necrotism" buff, which inflicts 1/20th of it's maximum health as a damage, each turn on a course of 3 turns
		//TODO: Every turn it has 50% chance of infecting any character, in a 3 cell radius, including caster
		//TODO: It cannot infect target that has an active "Necrotism" buff

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