package com.nyrds.pixeldungeon.mobs.necropolis;

import com.nyrds.pixeldungeon.effects.DeathStroke;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Stun;
import com.watabou.pixeldungeon.items.Gold;
import com.watabou.utils.Random;

import org.jetbrains.annotations.NotNull;

/**
 * Created by DeadDie on 12.02.2016
 */
// Rare of the Death Knight
public class DreadKnight extends UndeadMob {
    {
        hp(ht(40));
        baseDefenseSkill = 15;
        baseAttackSkill  = 17;

        dmgMin = 8;
        dmgMax = 16;
        dr = 12;

        expForKill = 8;
        maxLvl = 15;

        loot(Gold.class, 0.02f);
    }

    @Override
    public int attackProc(@NotNull Char enemy, int damage ) {
        //Double damage proc
        if (Random.Int(4) == 1){
            if (enemy !=null){
                DeathStroke.hit(enemy);
            }
            return damage * 2;
        }
        //Paralysis proc
        if (Random.Int(10) == 1){
            Buff.affect(enemy, Stun.class);
        }
        return damage;
    }
}
