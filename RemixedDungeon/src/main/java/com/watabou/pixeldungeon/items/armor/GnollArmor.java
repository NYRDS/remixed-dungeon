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

        SpellSprite.show( getUser(), SpellSprite.DOMINATION );
        Sample.INSTANCE.play( Assets.SND_DOMINANCE );

        int mobsDominated = getUser().getPets().size();
        for (Mob mob : Dungeon.level.getCopyOfMobsArray()) {

            if (Dungeon.level.fieldOfView[mob.getPos()]) {
                if(mobsDominated > getUser().lvl() / 5) {
                    break;
                }

                if(mob.canBePet()) {
                    Mob.makePet(mob, getUser());
                    new Flare(3, 32).show(mob.getSprite(), 2f);
                    mobsDominated++;
                }
            }
        }

        getUser().spend( Actor.TICK );
    }

    @Override
    public boolean doEquip( Hero hero ) {
        if (hero.heroClass == HeroClass.GNOLL) {
            return super.doEquip( hero );
        } else {
            GLog.w( Game.getVar(R.string.GnollArmor_NotGnoll) );
            return false;
        }
    }
}
