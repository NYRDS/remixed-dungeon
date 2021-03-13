package com.nyrds.pixeldungeon.ai;

import com.nyrds.pixeldungeon.mechanics.NamedEntityKind;
import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Terror;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.sprites.CharSprite;
import com.watabou.pixeldungeon.utils.Utils;

public class Horrified extends MobAi implements AiState{

    @Override
    public void act(Mob me) {

        if(!me.hasBuff(Terror.class)) {
            me.getSprite().showStatus(CharSprite.NEGATIVE, Mob.TXT_RAGE);
            me.setState(MobAi.getStateByClass(Hunting.class));
            return;
        }

        Terror terror = me.buff(Terror.class);
        NamedEntityKind src = terror.getSource();

        if(src instanceof Char) {
            me.doStepFrom(((Char) src).getPos());
            me.spend(Actor.TICK);
        }
    }

    @Override
    public void gotDamage(Mob me, NamedEntityKind src, int dmg) {
        Terror.recover(me);
    }

    @Override
    public String status(Char me) {

        if(me.hasBuff(Terror.class)) {
            Terror terror = me.buff(Terror.class);

                NamedEntityKind src = terror.getSource();

                if (src instanceof Char) {
                    return Utils.format(Game.getVar(R.string.Mob_StaTerrorStatus2),
                            me.getName(), ((Char) src).getName_objective());
                }
        }

        return Utils.format(Game.getVar(R.string.Mob_StaTerrorStatus),
                me.getName());
    }
}
