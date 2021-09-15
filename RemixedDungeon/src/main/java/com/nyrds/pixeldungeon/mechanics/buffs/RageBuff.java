package com.nyrds.pixeldungeon.mechanics.buffs;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.audio.Sample;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.effects.particles.ShadowParticle;
import com.watabou.pixeldungeon.items.rings.ArtifactBuff;
import com.watabou.pixeldungeon.ui.BuffIndicator;

import org.jetbrains.annotations.NotNull;

/**
 * Created by mike on 25.03.2018.
 * This file is part of Remixed Pixel Dungeon.
 */
public class RageBuff extends ArtifactBuff {
    @Override
    public boolean act() {
        if (target.isAlive()) {
            if (target.hp() > target.ht() / 5 && Math.random() < 0.1f) {
                target.damage((int) (Math.random() * 5), this);
                target.getSprite().emitter().burst(ShadowParticle.CURSE, 6);
                Sample.INSTANCE.play(Assets.SND_CURSED);
            }
        } else {
            deactivateActor();
        }
        spend(1);
        return true;
    }

    @Override
    public int attackProc(Char attacker, Char defender, int damage) {
        return damage * 2;
    }

    @Override
    public int icon() {
        return BuffIndicator.BLOODLUST;
    }

    @Override
    public String name() {
        return StringsManager.getVar(R.string.CorpseDustBuff_Name);
    }

    @Override
    public String desc() {
        return StringsManager.getVar(R.string.CorpseDustBuff_Info);
    }

    @Override
    public boolean attachTo(@NotNull Char target ) {
        return target.hasBuff(RageBuff.class) || super.attachTo(target);
    }
}
