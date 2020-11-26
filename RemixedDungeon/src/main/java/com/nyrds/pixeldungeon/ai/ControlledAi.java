package com.nyrds.pixeldungeon.ai;

import com.nyrds.pixeldungeon.mechanics.NamedEntityKind;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.ml.actions.Attack;
import com.nyrds.pixeldungeon.ml.actions.Interact;
import com.nyrds.pixeldungeon.ml.actions.Move;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.utils.Utils;

public class ControlledAi extends MobAi implements AiState {

    @Override
    public void act(Mob me) {

        if (me.curAction instanceof Move) {
            if (me.getPos() != me.curAction.dst) {
                if(me.doStepTo(me.curAction.dst)) {
                    Dungeon.observe();
                } else {
                    me.curAction = null;
                }
                return;
            }

        } else if (me.curAction instanceof Attack) {
            Attack attack = (Attack) me.curAction;

            if(!attack.target.isAlive()){
                me.curAction = null;
                return;
            }

            if (me.canAttack(attack.target)) {
                me.doAttack(attack.target);
                me.curAction = null;
                return;
            } else {
                me.setTarget(me.getEnemy().getPos());
                if(me.doStepTo(me.getTarget())) {

                } else {
                    me.curAction = null;
                }

                return;
            }

        } else if (me.curAction instanceof Interact) {

            // return actAttack((CharAction.Attack) curAction);
        }

        me.spend(Actor.TICK);
        me.curAction = null;
    }

    @Override
    public void gotDamage(Mob me, NamedEntityKind src, int dmg) {
    }

    @Override
    public void onDie() {
        Dungeon.hero.setControlTarget(Dungeon.hero);
        Dungeon.observe();
    }

    @Override
    public String status(Char me) {
        return Utils.format(Game.getVar(R.string.Mob_StaControlledStatus),me.name());
    }
}
