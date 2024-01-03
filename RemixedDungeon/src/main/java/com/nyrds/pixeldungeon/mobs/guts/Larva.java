package com.nyrds.pixeldungeon.mobs.guts;

import com.nyrds.pixeldungeon.ai.Hunting;
import com.nyrds.pixeldungeon.ai.MobAi;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.mobs.common.MobFactory;
import com.nyrds.platform.audio.Sample;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.effects.particles.ShadowParticle;
import com.watabou.pixeldungeon.sprites.LarvaSprite;
import com.watabou.utils.Random;

public class Larva extends Mob {

    {
        spriteClass = LarvaSprite.class;

        hp(ht(120));
        baseDefenseSkill = 20;
        baseAttackSkill = 30;

        dmgMin = 25;
        dmgMax = 30;
        dr = 8;
        expForKill = 0;

        setState(MobAi.getStateByClass(Hunting.class));

        lvl(1);
    }

    String [] imago = {
        "Scorpio",
        "Worm",
        "Eye"
    };

    @Override
    public boolean act() {
        if (lvl() >= 2) {
            getSprite().emitter().burst( ShadowParticle.CURSE, 4 );
            Sample.INSTANCE.play( Assets.SND_CURSED );

            Mob mob = MobFactory.mobByName(Random.element(imago));
            mob.setPos(getPos());
            level().spawnMob(mob, 0, getPos());
            die(this);
        }
        return super.act();
    }

    @Override
    public String getDescription() {
        return StringsManager.getVar(R.string.Yog_Desc);

    }
}
