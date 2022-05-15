package com.nyrds.pixeldungeon.ai;

import com.nyrds.pixeldungeon.mechanics.NamedEntityKind;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.utils.Utils;

import org.jetbrains.annotations.NotNull;

public class Passive extends MobAi implements AiState {

    public Passive(){}

    @Override
    public void act(@NotNull Mob me) {
        me.enemySeen = false;
        me.spend(Actor.TICK);
    }

    @Override
    public String status(Char me) {
        return Utils.format(R.string.Mob_StaPassiveStatus,
                me.getName());
    }

    @Override
    public void gotDamage(Mob me, NamedEntityKind src, int dmg) {
        seekRevenge(me,src);
    }

}
