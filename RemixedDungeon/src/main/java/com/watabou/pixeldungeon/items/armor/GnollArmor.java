package com.watabou.pixeldungeon.items.armor;

import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Sample;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.hero.HeroClass;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.effects.Flare;
import com.watabou.pixeldungeon.effects.SpellSprite;
import com.watabou.pixeldungeon.levels.Level;
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
        Char owner = getOwner();
        
        SpellSprite.show( owner, SpellSprite.DOMINATION );
        Sample.INSTANCE.play( Assets.SND_DOMINANCE );

        int mobsDominated = owner.countPets();
        Level level = owner.level();

        for (Mob mob : level.getCopyOfMobsArray()) {

            if (level.fieldOfView[mob.getPos()]) {
                if(mobsDominated > owner.lvl() / 6) {
                    break;
                }

                if(mob.canBePet()) {
                    Mob.makePet(mob, owner.getId());
                    new Flare(3, 32).show(mob.getSprite(), 2f);
                    mobsDominated++;
                }
            }
        }

        owner.spend( Actor.TICK );
    }

    @Override
    public boolean doEquip( Hero hero ) {
        if (hero.getHeroClass() == HeroClass.GNOLL) {
            return super.doEquip( hero );
        } else {
            GLog.w( Game.getVar(R.string.GnollArmor_NotGnoll) );
            return false;
        }
    }
}
