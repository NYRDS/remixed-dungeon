package com.nyrds.pixeldungeon.mechanics.spells;

import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.utils.Utils;

//This can be just config file
public class SummonDeathling extends SummoningSpell {

	public SummonDeathling() {
		targetingType = SpellHelper.TARGET_NONE;
		magicAffinity = SpellHelper.AFFINITY_NECROMANCY;
		mobKind = "Deathling";

		imageIndex = 0;
		castTime = 3f;
	}

	@Override
	public int getSummonLimit(){
		return Dungeon.hero.magicLvl();
	}

	@Override
	public String desc(){
		return Utils.format(desc, getSummonLimit());
	}

	@Override
	public String texture(){
		return "spellsIcons/necromancy.png";
	}
}
