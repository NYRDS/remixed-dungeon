package com.nyrds.pixeldungeon.ai;

import com.nyrds.pixeldungeon.mechanics.NamedEntityKind;
import com.nyrds.pixeldungeon.ml.R;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.utils.Utils;

import org.jetbrains.annotations.NotNull;

public class Sleeping extends MobAi implements AiState {

    public Sleeping(){}

    @Override
    public void act(@NotNull Char me) {

        if(returnToOwnerIfTooFar(me, 3)) {
            return;
        }

        Char enemy = chooseEnemy(me, 0.5f);
        me.setEnemy(enemy);

        if (me.isEnemyInFov() ){
            huntEnemy(me);
            me.spend(Mob.TIME_TO_WAKE_UP);
        } else {
            me.enemySeen = false;
        }
    }

    @Override
    public void gotDamage(Char me, NamedEntityKind src, int dmg) {
        seekRevenge(me,src);
    }

    @Override
    public String status(Char me) {
        return Utils.format(R.string.Mob_StaSleepingStatus,
                me.getName());
    }
}
