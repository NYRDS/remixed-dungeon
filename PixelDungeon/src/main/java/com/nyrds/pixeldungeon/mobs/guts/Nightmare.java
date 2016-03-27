package com.nyrds.pixeldungeon.mobs.guts;

import com.watabou.noosa.audio.Sample;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Bleeding;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Paralysis;
import com.watabou.pixeldungeon.actors.buffs.Roots;
import com.watabou.pixeldungeon.actors.mobs.Boss;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.effects.CellEmitter;
import com.watabou.pixeldungeon.effects.Speck;
import com.watabou.pixeldungeon.items.Gold;
import com.watabou.utils.Random;

/**
 * Created by DeadDie on 12.02.2016
 */
public class Nightmare extends Mob {
    {
        hp(ht(80));
        defenseSkill = 24;

        EXP = 0;
    }


    @Override
    public int attackProc( Char enemy, int damage ) {
        //Roots drain proc
        if (Random.Int(10) == 1){
            Buff.affect(enemy, Roots.class);
        }
        //Paralysis proc
        if (Random.Int(10) == 1){
            Buff.affect(enemy, Paralysis.class);
        }
        return damage;
    }

    @Override
    public int damageRoll() {
        return Random.NormalIntRange(20, 25);
    }

    @Override
    public int attackSkill( Char target ) { return 26; }

    @Override
    public int dr() { return 10; }

    @Override
    protected boolean act(){
        super.act();

        state = HUNTING;

        return true;
    }
}
