package com.nyrds.pixeldungeon.ai;

import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.hero.HeroAction;
import com.watabou.pixeldungeon.actors.mobs.Mob;

public class ControlledAi extends MobAi implements AiState {

    @Override
    public void act(Mob me) {

        if(!me.isAlive()) {
            Dungeon.hero.setControlTarget(Dungeon.hero);
        }

        if (me.curAction instanceof HeroAction.Move) {
            if(me.getPos() != me.curAction.dst) {
                me.doStepTo(me.curAction.dst);
                return;
            }

        } else if (me.curAction instanceof HeroAction.Interact) {

           // return actInteract((HeroAction.Interact) curAction);

        } else if (me.curAction instanceof HeroAction.Attack) {

           // return actAttack((HeroAction.Attack) curAction);
        }

        me.spend(1);
        me.curAction = null;
    }

    @Override
    public void gotDamage(Mob me, Object src, int dmg) {
    }
}
