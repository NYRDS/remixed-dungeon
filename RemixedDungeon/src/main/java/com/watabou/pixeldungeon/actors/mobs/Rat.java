
package com.watabou.pixeldungeon.actors.mobs;

import com.nyrds.pixeldungeon.ai.Fleeing;
import com.nyrds.pixeldungeon.ai.MobAi;
import com.nyrds.pixeldungeon.mechanics.NamedEntityKind;
import com.nyrds.pixeldungeon.mechanics.buffs.BuffFactory;
import com.nyrds.pixeldungeon.mobs.npc.PlagueDoctorNPC;
import com.nyrds.pixeldungeon.mobs.npc.ScarecrowNPC;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Terror;
import com.watabou.pixeldungeon.actors.mobs.npcs.Ghost;
import com.watabou.pixeldungeon.effects.Flare;

import org.jetbrains.annotations.NotNull;

public class Rat extends Mob {

	public Rat() {
		hp(ht(8));
		baseDefenseSkill = 3;
		baseAttackSkill  = 8;
		dmgMin = 1;
		dmgMax = 5;
		dr = 1;

		maxLvl = 7;
	}

	@Override
    public boolean canAttack(@NotNull Char enemy) {
		if(enemy.hasBuff(BuffFactory.RAT_SKULL_RATTER_AURA)) {
			setState(MobAi.getStateByClass(Fleeing.class));
			if(!hasBuff(BuffFactory.TERROR)) {
				new Flare(5, 32).color(0xFF0000, true).show(getSprite(), 2f);
				Buff.affect(this, Terror.class, Terror.DURATION);
				return false;
			}
		}
		return super.canAttack(enemy);
}

	@Override
	public void die(@NotNull NamedEntityKind cause) {
		ScarecrowNPC.Quest.process( getPos() );
		Ghost.Quest.process( getPos() );
		PlagueDoctorNPC.Quest.process( getPos() );
		
		super.die( cause );
	}

}
