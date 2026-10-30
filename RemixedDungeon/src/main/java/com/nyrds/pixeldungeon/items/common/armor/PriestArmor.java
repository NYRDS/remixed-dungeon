package com.nyrds.pixeldungeon.items.common.armor;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.audio.Sample;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.nyrds.pixeldungeon.mechanics.buffs.BuffFactory;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.hero.HeroClass;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.effects.particles.ShaftParticle;
import com.watabou.pixeldungeon.items.armor.ClassArmor;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.utils.GLog;
import org.jetbrains.annotations.NotNull;

public class PriestArmor extends ClassArmor {

    private static final int SANCTIFY_RADIUS = 2;
    private static final int BLESSED_DURATION = 20;

    {
        image = 34;
    }

    @Override
    public String special() {
        return "PriestArmor_ACSpecial";
    }

    @Override
    public String desc() {
        return StringsManager.getVar(R.string.PriestArmor_Desc);
    }

    @Override
    public void doSpecial(@NotNull Char user) {

        Level level = user.level();

        int damage = 5 + user.lvl();

        for (Mob mob : level.getCopyOfMobsArray()) {
            if (!user.friendly(mob) && level.distance(mob.getPos(), user.getPos()) <= SANCTIFY_RADIUS) {
                int dmg = mob.undead ? damage : (mob.demon ? Math.max(1, damage / 2) : 0);
                if (dmg > 0) {
                    mob.getSprite().emitter().start(ShaftParticle.FACTORY, 0.1f, 5);
                    mob.damage(dmg, user);
                }
            }
        }

        // Blessed is a lua buff on master (batch 19-2b) - string-kind affect
        Buff.affect(user, BuffFactory.BLESSED, BLESSED_DURATION).level(1 + user.lvl() / 5);
        user.getSprite().emitter().start(ShaftParticle.FACTORY, 0.1f, 8);

        user.doOperate(Actor.TICK);
        Sample.INSTANCE.play(Assets.SND_LEVELUP);
    }

    @Override
    public boolean doEquip(@NotNull Char hero) {
        if (hero.getHeroClass() == HeroClass.PRIEST) {
            return super.doEquip(hero);
        } else {
            GLog.w(StringsManager.getVar(R.string.PriestArmor_NotPriest));
            return false;
        }
    }
}
