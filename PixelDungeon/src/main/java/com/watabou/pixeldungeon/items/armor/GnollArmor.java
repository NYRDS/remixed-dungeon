package com.watabou.pixeldungeon.items.armor;

import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Sample;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.hero.HeroClass;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.effects.Flare;
import com.watabou.pixeldungeon.effects.SpellSprite;
import com.watabou.pixeldungeon.utils.GLog;

/**
 * Created by mike on 10.01.2018.
 * This file is part of Remixed Pixel Dungeon.
 */

public class GnollArmor extends ClassArmor {

    private static final String TXT_NOT_GNOLL = Game.getVar(R.string.GnollArmor_NotGnoll);

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
    public void doSpecial() {

        SpellSprite.show( getCurUser(), SpellSprite.DOMINATION );
        Sample.INSTANCE.play( Assets.SND_DOMINANCE );

        int mobsDominated = 0;
        for (Mob mob : Dungeon.level.getCopyOfMobsArray()) {
            if (Dungeon.level.fieldOfView[mob.getPos()]) {
                if(mob.canBePet()) {
                    Mob.makePet(mob, getCurUser());
                    new Flare(3, 32).show(mob.getSprite(), 2f);
                    mobsDominated++;
                }

                if(mobsDominated > getCurUser().lvl() / 7) {
                    break;
                }
            }
        }

        getCurUser().spend( Actor.TICK );
    }

    @Override
    public boolean doEquip( Hero hero ) {
        if (hero.heroClass == HeroClass.GNOLL) {
            return super.doEquip( hero );
        } else {
            GLog.w( TXT_NOT_GNOLL);
            return false;
        }
    }
}
