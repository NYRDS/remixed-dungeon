package com.nyrds.pixeldungeon.mobs.necropolis;

import com.nyrds.pixeldungeon.effects.DeathStroke;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Paralysis;
import com.watabou.pixeldungeon.items.Gold;
import com.watabou.utils.Random;

/**
 * Created by DeadDie on 12.02.2016
 */
// Rare of the Death Knight
public class DreadKnight extends UndeadMob {
    {
        hp(ht(40));
        defenseSkill = 15;

        EXP = 8;
        maxLvl = 15;

        loot = Gold.class;
        lootChance = 0.02f;
    }

    @Override
    public int attackProc( Char enemy, int damage ) {
        //Double damage proc
        if (Random.Int(4) == 1){
            if (enemy !=null){
                DeathStroke.hit(enemy);
            }
            return damage * 2;
        }
        //Paralysis proc
        if (Random.Int(10) == 1){
            Buff.affect(enemy, Paralysis.class);
        }
        return damage;
    }

    @Override
    public int damageRoll() {
        return Random.NormalIntRange(8, 16);
    }

    @Override
    public int attackSkill( Char target ) {
        return 17;
    }

    @Override
    public int dr() {
        return 12;
    }


}
