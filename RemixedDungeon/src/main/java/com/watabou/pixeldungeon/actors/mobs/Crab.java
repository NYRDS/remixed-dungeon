
package com.watabou.pixeldungeon.actors.mobs;

import com.nyrds.pixeldungeon.mechanics.NamedEntityKind;
import com.watabou.pixeldungeon.actors.mobs.npcs.Ghost;
import com.watabou.pixeldungeon.items.food.MysteryMeat;

import org.jetbrains.annotations.NotNull;

public class Crab extends Mob {

	{
		hp(ht(15));
		baseDefenseSkill = 5;
		baseAttackSkill  = 12;
		baseSpeed = 2f;
		
		exp = 3;
		maxLvl = 9;
		dmgMin = 3;
		dmgMax = 6;
		dr = 4;

		loot(MysteryMeat.class, 0.314f);
	}

	@Override
	public void die(@NotNull NamedEntityKind cause) {
		Ghost.Quest.process( getPos() );
		super.die( cause );
	}

}
