package com.nyrds.pixeldungeon.ai;

import com.watabou.pixeldungeon.actors.hero.HeroAction;
import com.watabou.pixeldungeon.actors.mobs.Mob;

public class ControlledAi extends MobAi implements AiState {

    private HeroAction curAction;

    @Override
    public boolean act(Mob me) {


        if (curAction instanceof HeroAction.Move) {
            me.doStepTo(curAction.dst);
        } else if (curAction instanceof HeroAction.Interact) {

           // return actInteract((HeroAction.Interact) curAction);

        } else if (curAction instanceof HeroAction.Attack) {

           // return actAttack((HeroAction.Attack) curAction);
        }


        return true;

    }

    @Override
    public void gotDamage(Mob me, Object src, int dmg) {
    }

    public void setAction(HeroAction action) {
        this.curAction = action;
    }

}
