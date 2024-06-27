package com.nyrds.pixeldungeon.mobs.guts;

import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.blobs.ToxicGas;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Paralysis;
import com.watabou.pixeldungeon.actors.buffs.Poison;
import com.watabou.pixeldungeon.actors.buffs.Roots;
import com.watabou.pixeldungeon.actors.buffs.Stun;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.items.Gold;
import com.watabou.utils.Random;

import org.jetbrains.annotations.NotNull;

/**
 * Created by DeadDie on 12.02.2016
 */
public class Worm extends Mob {
    {
        hp(ht(195));
        baseDefenseSkill = 15;
        baseAttackSkill  = 20;
        dmgMin = 22;
        dmgMax = 45;
        dr = 50;

        expForKill = 18;
        maxLvl = 35;

        loot(Gold.class, 0.4f);

        addImmunity(Paralysis.class);
		addImmunity(Stun.class);
        addImmunity(ToxicGas.class);
    }

    @Override
    public int attackProc(@NotNull Char enemy, int damage ) {
        //Roots proc
        if (Random.Int(7) == 1){
            Buff.affect(enemy, Roots.class);
        }
        //Poison proc
        if (Random.Int(5) == 1){
            Buff.affect( enemy, Poison.class, Random.Int( 7, 9 ) * Poison.durationFactor( enemy ) );
        }
        return damage;
    }
}
