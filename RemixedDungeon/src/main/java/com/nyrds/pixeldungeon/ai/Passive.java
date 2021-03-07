package com.nyrds.pixeldungeon.ai;

import com.nyrds.pixeldungeon.mechanics.NamedEntityKind;
import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.utils.Utils;

public class Passive extends MobAi implements AiState {

    public Passive(){}

    @Override
    public void act(Mob me) {
        me.enemySeen = false;
        me.spend(Actor.TICK);

        if(Math.random()<0.33) {
            me.getSprite().idle();
        }
    }

    @Override
    public String status(Char me) {
        return Utils.format(Game.getVar(R.string.Mob_StaPassiveStatus),
                me.getName());
    }

    @Override
    public void gotDamage(Mob me, NamedEntityKind src, int dmg) {
        seekRevenge(me,src);
    }

}
