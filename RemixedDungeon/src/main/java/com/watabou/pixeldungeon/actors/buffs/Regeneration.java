
package com.watabou.pixeldungeon.actors.buffs;

import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.Facilitations;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.hero.Hero;

import org.jetbrains.annotations.NotNull;

public class Regeneration extends Buff {

    private static final float REGENERATION_DELAY = 10;

    @Override
    public boolean act() {
        if (target.isAlive()) {
            final int[] bonus = {0};

            if(Dungeon.isFacilitated(Facilitations.FAST_REGENERATION) && target instanceof Hero) {
                bonus[0] += 10;
            }

            target.forEachBuff(b-> bonus[0] +=b.regenerationBonus());

            int healPoints = 1;
            float healRate = (float) Math.pow(1.2, bonus[0]);

            if(healRate > REGENERATION_DELAY * 5) {
                healPoints += (int) (healRate / 5);
                healRate = REGENERATION_DELAY * 5;
            }

            if (!target.isStarving() && !target.level().isSafe()) {
                target.heal(healPoints,this);
            }


            spend(REGENERATION_DELAY / healRate);
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
