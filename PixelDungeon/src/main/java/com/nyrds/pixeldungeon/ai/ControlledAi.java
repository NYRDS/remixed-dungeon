package com.nyrds.pixeldungeon.ai;

import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.hero.HeroAction;
import com.watabou.pixeldungeon.actors.mobs.Mob;

public class ControlledAi extends MobAi implements AiState {

    @Override
    public void act(Mob me) {

        if (!me.isAlive()) {
            Dungeon.hero.setControlTarget(Dungeon.hero);
            Dungeon.observe();
        }

        if (me.curAction instanceof HeroAction.Move) {
            if (me.getPos() != me.curAction.dst) {
                if(me.doStepTo(me.curAction.dst)) {
                    Dungeon.observe();
                } else {
                    me.curAction = null;
                }
                return;
            }

        } else if (me.curAction instanceof HeroAction.Attack) {
            HeroAction.Attack attack = (HeroAction.Attack) me.curAction;

            if(!attack.target.isAlive()){
                me.curAction = null;
                return;
            }

            if (me.canAttack(attack.target)) {
                me.doAttack(attack.target);
                return;
            } else {
                me.target = me.getEnemy().getPos();
                if(me.doStepTo(me.target)) {
                    Dungeon.observe();
                } else {
                    me.curAction = null;
                }

                return;
            }

        } else if (me.curAction instanceof HeroAction.Interact) {

            // return actAttack((HeroAction.Attack) curAction);
        }

        me.spend(1);
        me.curAction = null;
    }

    @Override
    public void gotDamage(Mob me, Object src, int dmg) {
    }
}
