package com.watabou.pixeldungeon.actors.hero;

import com.watabou.pixeldungeon.actors.Char;

public class Attack extends CharAction {
    public Char target;
    public Attack(Char target ) {
        this.target = target;
    }

    @Override
    public boolean act(Char hero) {

        hero.enemy = target;

        if (target.isAlive() && !hero.pacified) {

            if (hero.bowEquipped()) {
                if (hero.level().adjacent(hero.getPos(), target.getPos()) && hero.getBelongings().weapon.goodForMelee()) {
                    return hero.actMeleeAttack(target);
                }
                return hero.actBowAttack(target);
            }
            return hero.actMeleeAttack(target);
        }
        return hero.getCloserIfVisible(target.getPos());
    }
}
