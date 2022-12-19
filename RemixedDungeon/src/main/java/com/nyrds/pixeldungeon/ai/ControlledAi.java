package com.nyrds.pixeldungeon.ai;

import com.nyrds.pixeldungeon.mechanics.NamedEntityKind;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.ml.actions.Attack;
import com.nyrds.pixeldungeon.ml.actions.CharAction;
import com.nyrds.pixeldungeon.ml.actions.Move;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.utils.Utils;

import org.jetbrains.annotations.NotNull;



public class ControlledAi extends MobAi implements AiState {

    @Override
    public void act(@NotNull Mob me) {

        final Char owner = me.getOwner();
        final CharAction curAction = me.curAction;

        if(curAction!=null) {

            final int dst = curAction.dst;

            if (dst == owner.getPos()) {
                owner.setControlTarget(owner);
                Dungeon.observe();
                me.setState(MobAi.getStateByClass(Sleeping.class));
                return;
            }

            if (curAction instanceof Move) {
                if (me.getPos() != dst) {
                    if (me.doStepTo(dst)) {
                        Dungeon.observe();
                    } else {
                        me.curAction = null;
                    }
                    return;
                }

            } else if (curAction instanceof Attack) {
                Attack attack = (Attack) curAction;

                final Char target = attack.target;

                if (!target.isAlive()) {
                    me.curAction = null;
                    return;
                }

                if (me.canAttack(target)) {
                    me.doAttack(target);
                    me.curAction = null;
                } else {
                    me.setTarget(me.getEnemy().getPos());
                    if (!me.doStepTo(me.getTarget())) {
                        me.curAction = null;
                    }

                }
                return;
            }
        }

        me.spend(Actor.TICK);
        me.curAction = null;
    }

    @Override
    public void gotDamage(Mob me, NamedEntityKind src, int dmg) {
    }

    @Override
    public void onDie(@NotNull Mob me) {
        var owner = me.getOwner();
        owner.setControlTarget(owner);
        Dungeon.observe();
    }

    @Override
    public String status(Char me) {
        return Utils.format(R.string.Mob_StaControlledStatus, me.name());
    }
}
