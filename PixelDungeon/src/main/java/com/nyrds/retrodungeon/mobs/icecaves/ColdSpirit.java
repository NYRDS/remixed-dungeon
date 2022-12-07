package com.nyrds.retrodungeon.mobs.icecaves;

import android.support.annotation.NonNull;

import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.blobs.Fire;
import com.watabou.pixeldungeon.actors.blobs.Freezing;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.items.Gold;
import com.watabou.utils.Random;

/**
 * Created by DeadDie on 12.02.2016
 */
public class ColdSpirit extends Mob {

    public ColdSpirit(){
        hp(ht(50));

        baseSpeed = 1.3f;
        defenseSkill = 16;
        flying = true;

        exp = 8;
        maxLvl = 20;

        loot = Gold.class;
        lootChance = 0.02f;
    }

    @SuppressWarnings("unchecked")
    @Override
    public int attackProc(@NonNull Char enemy, int damage ) {
        //Buff proc
        if (Random.Int( 4 ) == 1) {
            Freezing.affect( enemy.getPos(), (Fire) Dungeon.level.blobs.get( Fire.class ) );
        }
        return damage;
    }

    @Override
    public int damageRoll() {
        return Random.NormalIntRange(12, 15);
    }

    @Override
    public int attackSkill( Char target ) {
        return 22;
    }

    @Override
    public int dr() {
        return 22;
    }
}
