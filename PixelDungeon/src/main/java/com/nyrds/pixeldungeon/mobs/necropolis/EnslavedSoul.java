package com.nyrds.pixeldungeon.mobs.necropolis;

import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Blindness;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Charm;
import com.watabou.pixeldungeon.actors.buffs.FlavourBuff;
import com.watabou.pixeldungeon.actors.buffs.Roots;
import com.watabou.pixeldungeon.actors.buffs.Slow;
import com.watabou.pixeldungeon.actors.buffs.Vertigo;
import com.watabou.pixeldungeon.actors.buffs.Weakness;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.items.Gold;
import com.watabou.utils.Random;

/**
 * Created by DeadDie on 12.02.2016
 */
public class EnslavedSoul extends UndeadMob {

    static Class<?> BuffsForEnemy[] = {
            Blindness.class,
            Charm.class,
            Roots.class,
            Slow.class,
            Vertigo.class,
            Weakness.class
    };

    public EnslavedSoul(){
        hp(ht(45));
        defenseSkill = 27;

        baseSpeed = 1.1f;

        EXP = 10;
        maxLvl = 15;

        loot = Gold.class;
        lootChance = 0.02f;
    }

    @SuppressWarnings("unchecked")
    @Override
    public int attackProc( Char enemy, int damage ) {
        //Buff proc
        if (Random.Int(5) == 1){
            if(enemy instanceof Hero) {
                Class <? extends FlavourBuff> buffClass = (Class<? extends FlavourBuff>) Random.oneOf(BuffsForEnemy);
                Buff.prolong( enemy, buffClass, 3 );
            }
        }

        return damage;
    }

    @Override
    public int damageRoll() {
        return Random.NormalIntRange(5, 15);
    }

    @Override
    public int attackSkill( Char target ) {
        return 10;
    }

    @Override
    public int dr() {
        return 20;
    }


}
