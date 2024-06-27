package com.nyrds.pixeldungeon.mobs.guts;


import com.nyrds.pixeldungeon.ai.Hunting;
import com.nyrds.pixeldungeon.ai.MobAi;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Roots;
import com.watabou.pixeldungeon.actors.buffs.Stun;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.utils.Random;

import org.jetbrains.annotations.NotNull;

/**
 * Created by DeadDie on 12.02.2016
 */
public class Nightmare extends Mob {

    {
        hp(ht(80));
        baseDefenseSkill = 24;
        baseAttackSkill  = 26;
        dmgMin = 20;
        dmgMax = 25;
        dr = 10;

        flying = true;
        expForKill = 0;
    }

    @Override
    public int attackProc(@NotNull Char enemy, int damage ) {
        //Roots proc
        if (Random.Int(10) == 1){
            Buff.affect(enemy, Roots.class);
        }
        //Paralysis proc
        if (Random.Int(10) == 1){
            Buff.affect(enemy, Stun.class);
        }
        return damage;
    }

    @Override
    public boolean act(){
        super.act();

        setState(MobAi.getStateByClass(Hunting.class));

        return true;
    }
}
