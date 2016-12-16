package com.nyrds.pixeldungeon.mobs.icecaves;

import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.blobs.Fire;
import com.watabou.pixeldungeon.actors.blobs.Freezing;
import com.watabou.pixeldungeon.actors.buffs.Blindness;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Charm;
import com.watabou.pixeldungeon.actors.buffs.FlavourBuff;
import com.watabou.pixeldungeon.actors.buffs.Roots;
import com.watabou.pixeldungeon.actors.buffs.Slow;
import com.watabou.pixeldungeon.actors.buffs.Vertigo;
import com.watabou.pixeldungeon.actors.buffs.Weakness;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.items.Gold;
import com.watabou.utils.Random;

/**
 * Created by DeadDie on 12.02.2016
 */
public class ColdSpirit extends Mob {

    public ColdSpirit(){
        hp(ht(25));

        baseSpeed = 1.1f;
        defenseSkill = 11;
        flying = true;

        EXP = 5;
        maxLvl = 15;

        loot = Gold.class;
        lootChance = 0.02f;
    }

    @SuppressWarnings("unchecked")
    @Override
    public int attackProc( Char enemy, int damage ) {
        //Buff proc
        if (Random.Int( 4 ) == 1) {
            Freezing.affect( enemy.getPos(), (Fire) Dungeon.level.blobs.get( Fire.class ) );
        }
        return damage;
    }

    @Override
    public int damageRoll() {
        return Random.NormalIntRange(5, 8);
    }

    @Override
    public int attackSkill( Char target ) {
        return 10;
    }

    @Override
    public int dr() {
        return 10;
    }
}
