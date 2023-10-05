
package com.watabou.pixeldungeon.actors.mobs;

import com.nyrds.pixeldungeon.mechanics.NamedEntityKind;
import com.nyrds.pixeldungeon.mobs.npc.ScarecrowNPC;
import com.watabou.pixeldungeon.actors.mobs.npcs.Ghost;
import com.watabou.pixeldungeon.items.Gold;

import org.jetbrains.annotations.NotNull;

public class Gnoll extends Mob {
	
	{
		hp(ht(12));
		baseDefenseSkill = 4;
		baseAttackSkill  = 11;
		dmgMin = 2;
		dmgMax = 5;
		dr = 2;

		exp = 2;
		maxLvl = 12;
		
		loot(Gold.class, 0.5f);
	}

	@Override
	public void die(@NotNull NamedEntityKind cause) {
		ScarecrowNPC.Quest.process( getPos() );
		Ghost.Quest.process( getPos() );
		super.die( cause );
	}

}
