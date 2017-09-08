package com.nyrds.pixeldungeon.mechanics.spells;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.mobs.common.Deathling;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.actors.mobs.Mob;

//This can be just config file
public class SummonDeathling extends SummoningSpell {

	public SummonDeathling() {
		targetingType = SpellHelper.TARGET_NONE;
		magicAffinity = SpellHelper.AFFINITY_NECROMANCY;

		name = Game.getVar(R.string.Necromancy_SummonDeathlingName);
		desc = Game.getVar(R.string.SummonDeathling_Info);
		imageIndex = 0;

		castTime = 3f;
	}

	@Override
	public Mob getSummonMob() {
		return new Deathling();
	}

	@Override
	public String texture(){
		return "spellsIcons/necromancy.png";
	}
}
