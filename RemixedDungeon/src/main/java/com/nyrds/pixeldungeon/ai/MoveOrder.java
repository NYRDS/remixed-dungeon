package com.nyrds.pixeldungeon.ai;

import com.nyrds.pixeldungeon.mechanics.NamedEntityKind;
import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.utils.Utils;

public class MoveOrder extends MobAi implements AiState {

    public MoveOrder(){ }

    @Override
    public void act(Mob me) {
        if(!me.doStepTo(me.getTarget())) {
            me.setState(getStateByClass(Hunting.class));
        }
    }

    @Override
    public String status(Char me) {
        return Utils.format(Game.getVar(R.string.Mob_StaWanderingStatus),
                me.getName());
    }

    @Override
    public void gotDamage(Mob me, NamedEntityKind src, int dmg) {
        seekRevenge(me,src);
    }
}
