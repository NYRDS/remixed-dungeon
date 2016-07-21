package com.nyrds.pixeldungeon.mobs.necropolis;

import com.nyrds.pixeldungeon.effects.DeathStroke;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.items.Gold;
import com.watabou.utils.Random;

/**
 * Created by DeadDie on 12.02.2016
 */
public class DeathKnight extends UndeadMob {
    {
        hp(ht(45));
        defenseSkill = 17;

        EXP = 10;
        maxLvl = 15;

        loot = Gold.class;
        lootChance = 0.02f;
    }

    @Override
    public int attackProc( Char enemy, int damage ) {
        //Double damage proc
        if (Random.Int(7) == 1){
            if (enemy !=null){
                DeathStroke.hit(enemy);
            }
            return damage * 2;
        }
        return damage;
    }

    @Override
    public int damageRoll() {
        return Random.NormalIntRange(7, 14);
    }

    @Override
    public int attackSkill( Char target ) {
        return 25;
    }

    @Override
    public int dr() {
        return 7;
    }


}
