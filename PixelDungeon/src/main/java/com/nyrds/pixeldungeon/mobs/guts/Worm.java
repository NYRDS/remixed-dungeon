package com.nyrds.pixeldungeon.mobs.guts;

import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.blobs.ToxicGas;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Paralysis;
import com.watabou.pixeldungeon.actors.buffs.Poison;
import com.watabou.pixeldungeon.actors.buffs.Roots;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.items.Gold;
import com.watabou.utils.Random;

/**
 * Created by DeadDie on 12.02.2016
 */
public class Worm extends Mob {
    {
        hp(ht(195));
        defenseSkill = 15;

        EXP = 18;

        loot = Gold.class;
        lootChance = 0.4f;

        IMMUNITIES.add(Paralysis.class);
        IMMUNITIES.add(ToxicGas.class);
    }

    @Override
    public int attackProc( Char enemy, int damage ) {
        //Roots proc
        if (Random.Int(7) == 1){
            Buff.affect(enemy, Roots.class);
        }
        //Poison proc
        if (Random.Int(5) == 1){
            Buff.affect(enemy, Poison.class);
        }
        return damage;
    }

    @Override
    public int damageRoll() {
        return Random.NormalIntRange(22, 45);
    }

    @Override
    public int attackSkill( Char target ) {
        return 20;
    }

    @Override
    public int dr() {
        return 50;
    }
}
