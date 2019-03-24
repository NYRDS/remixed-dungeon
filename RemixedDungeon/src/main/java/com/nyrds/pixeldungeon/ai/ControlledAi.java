package com.nyrds.pixeldungeon.ai;

import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.hero.CharAction;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.utils.Utils;

public class ControlledAi extends MobAi implements AiState {

    @Override
    public void act(Mob me) {

        if (me.curAction instanceof CharAction.Move) {
            if (me.getPos() != me.curAction.dst) {
                if(me.doStepTo(me.curAction.dst)) {
                    Dungeon.observe();
                } else {
                    me.curAction = null;
                }
                return;
            }

        } else if (me.curAction instanceof CharAction.Attack) {
            CharAction.Attack attack = (CharAction.Attack) me.curAction;

            if(!attack.target.isAlive()){
                me.curAction = null;
                return;
            }

            if (me.canAttack(attack.target)) {
                me.doAttack(attack.target);
                me.curAction = null;
                return;
            } else {
                me.target = me.getEnemy().getPos();
                if(me.doStepTo(me.target)) {

                } else {
                    me.curAction = null;
                }

                return;
            }

        } else if (me.curAction instanceof CharAction.Interact) {

            // return actAttack((CharAction.Attack) curAction);
        }

        me.spend(Actor.TICK);
        me.curAction = null;
    }

    @Override
    public void gotDamage(Mob me, Object src, int dmg) {
    }

    @Override
    public void onDie() {
        Dungeon.hero.setControlTarget(Dungeon.hero);
        Dungeon.observe();
    }

    @Override
    public String status(Mob me) {
        return Utils.format(Game.getVar(R.string.Mob_StaControlledStatus),me.getEntityKind());
    }
}
