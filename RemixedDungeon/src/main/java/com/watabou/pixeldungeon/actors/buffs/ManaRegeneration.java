package com.watabou.pixeldungeon.actors.buffs;

import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.Facilitations;
import com.watabou.pixeldungeon.actors.Char;

import org.jetbrains.annotations.NotNull;

public class ManaRegeneration extends Buff {

    private static final float REGENERATION_DELAY = 20;

    @Override
    public boolean act() {
        if (target.isAlive()) {
            if (!target.level().isSafe()) {
                target.accumulateSkillPoints(1);
            }

            final int[] bonus = {0};

            if(Dungeon.isFacilitated(Facilitations.FAST_MANA_REGENERATION)) {
                bonus[0] += 10;
            }

            target.forEachBuff(b-> bonus[0] +=b.manaRegenerationBonus());

            spend((float) (REGENERATION_DELAY / Math.pow(1.2, bonus[0])));
        } else {
            deactivateActor();
        }
        return true;
    }

	@Override
	public boolean attachTo(@NotNull Char target ) {
        return target.buffLevel(getEntityKind()) > 0 || super.attachTo(target);
    }
}
