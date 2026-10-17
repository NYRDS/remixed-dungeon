package com.nyrds.pixeldungeon.items.common.armor;

import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.items.armor.ClassArmor;
import org.jetbrains.annotations.NotNull;

public class PriestArmor extends ClassArmor {
    @Override
    public String special() {
        return "";
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

        Buff.affect(user, Blessed.class, BLESSED_DURATION).level(1 + user.lvl() / 5);
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
