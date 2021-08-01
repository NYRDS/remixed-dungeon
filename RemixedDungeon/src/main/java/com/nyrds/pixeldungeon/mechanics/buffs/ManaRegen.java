package com.nyrds.pixeldungeon.mechanics.buffs;

import com.nyrds.Packable;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.effects.CellEmitter;
import com.watabou.pixeldungeon.effects.particles.ShaftParticle;
import com.watabou.pixeldungeon.ui.BuffIndicator;

import org.jetbrains.annotations.NotNull;

public class ManaRegen extends Buff {

    private static final float STEP = 5f;

    @Packable
    private int pos;

    @Override
    public boolean attachTo(@NotNull Char target) {
        pos = target.getPos();
        return super.attachTo(target);
    }

    @Override
    public boolean act() {
        if (target.getPos() != pos || (target.getSkillPoints() >= target.getSkillPointsMax())) {
            detach();
        } else {
            target.accumulateSkillPoints( Math.max( target.getSkillPointsMax() / 10, 1) );

            if (Dungeon.visible[pos]) {
                CellEmitter.get(pos).start(ShaftParticle.FACTORY, 0.2f, 3);
            }
        }
        spend(STEP);
        return true;
    }

    @Override
    public int icon() {
        return BuffIndicator.HEALING;
    }

    @Override
    public String name() {
return StringsManager.getVar(R.string.MoongraceBuff_Name);
}

    @Override
    public String desc() {
return StringsManager.getVar(R.string.MoongraceBuff_Info);
}
}
