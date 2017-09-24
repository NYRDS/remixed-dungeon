package com.nyrds.pixeldungeon.mechanics.spells;

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
	public String texture(){
		return "spellsIcons/necromancy.png";
	}
}
