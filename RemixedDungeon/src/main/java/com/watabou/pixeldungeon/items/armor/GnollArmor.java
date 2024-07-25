package com.watabou.pixeldungeon.items.armor;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.audio.Sample;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.hero.HeroClass;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.effects.Flare;
import com.watabou.pixeldungeon.effects.SpellSprite;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.mechanics.ShadowCaster;
import com.watabou.pixeldungeon.utils.GLog;

import org.jetbrains.annotations.NotNull;

/**
 * Created by mike on 10.01.2018.
 * This file is part of Remixed Pixel Dungeon.
 */

public class GnollArmor extends ClassArmor {

    {
        image = 25;
        coverHair = false;
        hasCollar = false;
        hasHelmet = false;
    }

    @Override
    public String special() {
        return "GnollArmor_ACSpecial";
    }

    @Override
    public void doSpecial(@NotNull Char user) {

        SpellSprite.show( user, SpellSprite.DOMINATION );
        Sample.INSTANCE.play( Assets.SND_DOMINANCE );

        int mobsDominated = user.countPets();
        Level level = user.level();

        for (Mob mob : level.getCopyOfMobsArray()) {

            if (level.fieldOfView[mob.getPos()] && level.distanceL2(user, mob) <= ShadowCaster.MAX_DISTANCE) {

                if(mobsDominated > user.lvl() / 6) {
                    break;
                }

                if(mob.canBePet()) {
                    Mob.makePet(mob, user.getId());
                    new Flare(3, 32).show(mob.getSprite(), 2f);
                    mobsDominated++;
                }
            }
        }

        user.spend( Actor.TICK );
    }

    @Override
    public boolean doEquip(@NotNull Char hero ) {
        if (hero.getHeroClass() == HeroClass.GNOLL) {
            return super.doEquip( hero );
        } else {
            GLog.w(StringsManager.getVar(R.string.GnollArmor_NotGnoll));
            return false;
        }
    }

    @Override
    public String desc() {
        return StringsManager.getVar(R.string.GnollArmor_Info);
    }
}
