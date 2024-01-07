package com.nyrds.pixeldungeon.mobs.guts;

import com.nyrds.pixeldungeon.effects.Devour;
import com.nyrds.pixeldungeon.mechanics.NamedEntityKind;
import com.nyrds.platform.audio.Sample;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.blobs.ToxicGas;
import com.watabou.pixeldungeon.actors.buffs.Amok;
import com.watabou.pixeldungeon.actors.buffs.Bleeding;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Burning;
import com.watabou.pixeldungeon.actors.buffs.Paralysis;
import com.watabou.pixeldungeon.actors.buffs.Sleep;
import com.watabou.pixeldungeon.actors.buffs.Terror;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.utils.Random;

import org.jetbrains.annotations.NotNull;

/**
 * Created by DeadDie on 12.02.2016
 */
public class YogsTeeth extends Mob {
    {
        hp(ht(350));
        baseDefenseSkill = 44;
        baseAttackSkill  = 46;
        dmgMin = 50;
        dmgMax = 80;
        dr = 21;

        expForKill = 26;

        addResistance(ToxicGas.class);

        addImmunity(Paralysis.class);
        addImmunity(Amok.class);
        addImmunity(Sleep.class);
        addImmunity(Terror.class);
        addImmunity(Burning.class);
    }

    @Override
    public void damage(int dmg, @NotNull NamedEntityKind src) {
        for (Mob mob : level().mobs) {
            mob.beckon(getPos());
        }

        super.damage(dmg, src);
    }

    @Override
    public int attackProc(@NotNull Char enemy, int damage ) {
        //Life drain proc
        if (Random.Int(3) == 1){
            heal(damage, this);
        }

        //Bleeding proc
        if (Random.Int(3) == 1){
            Buff.affect(enemy, Bleeding.class).level(damage);
        }

        //Double damage proc
        if (Random.Int(3) == 1){
            Devour.hit(enemy);
            Sample.INSTANCE.play(Assets.SND_BITE);
            return damage*2;
        }
        return damage;
    }

    @Override
    public boolean canBePet() {
        return false;
    }
}
