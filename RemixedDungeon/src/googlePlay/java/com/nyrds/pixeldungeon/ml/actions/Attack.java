package com.nyrds.pixeldungeon.ml.actions;

import com.watabou.pixeldungeon.actors.Char;

public class Attack extends CharAction {
    public Char target;
    public Attack(Char target ) {
        this.target = target;
        dst = target.getPos();
    }

    @Override
    public boolean act(Char hero) {

        hero.setEnemy(target);

        if (target.isAlive() && !hero.pacified) {

            if (hero.bowEquipped()) {
                if (hero.adjacent(target) && hero.getBelongings().weapon.goodForMelee()) {
                    return hero.actMeleeAttack(target);
                }
                return hero.actBowAttack(target);
            }
            return hero.actMeleeAttack(target);
        }
        return hero.getCloserIfVisible(dst);
    }
}
