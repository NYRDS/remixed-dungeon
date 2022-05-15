package com.nyrds.pixeldungeon.ai;

import com.nyrds.pixeldungeon.mechanics.NamedEntityKind;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.sprites.CharSprite;
import com.watabou.pixeldungeon.utils.Utils;

import org.jetbrains.annotations.NotNull;

public class ThiefFleeing extends MobAi implements AiState {

    public ThiefFleeing() {
    }

    @Override
    public void act(@NotNull Mob me) {
        me.enemySeen = me.isEnemyInFov();
        if (me.enemySeen) {
            me.setTarget(me.getEnemy().getPos());
        }

        if(!me.doStepFrom(me.getTarget())) {
            me.spend(Actor.TICK);
            me.showStatus( CharSprite.NEGATIVE, Mob.TXT_RAGE );
            me.setState(MobAi.getStateByClass(Hunting.class));
        }
    }

    @Override
    public String status(Char me) {
        Char enemy = me.getEnemy();
        if(enemy.valid()) {
            return Utils.format(R.string.Mob_StaFleeingStatus2,
                    me.getName(), enemy.getName_objective());
        }
        return Utils.format(R.string.Mob_StaFleeingStatus,
                me.getName());
    }

    @Override
    public void gotDamage(Mob me, NamedEntityKind src, int dmg) {
        seekRevenge(me,src);
    }
}
