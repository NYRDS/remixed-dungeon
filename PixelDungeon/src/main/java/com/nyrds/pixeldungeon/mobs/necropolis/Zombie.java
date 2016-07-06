package com.nyrds.pixeldungeon.mobs.necropolis;

import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Sample;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.blobs.ToxicGas;
import com.watabou.pixeldungeon.actors.buffs.Amok;
import com.watabou.pixeldungeon.actors.buffs.Blindness;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Paralysis;
import com.watabou.pixeldungeon.actors.buffs.Roots;
import com.watabou.pixeldungeon.actors.buffs.Sleep;
import com.watabou.pixeldungeon.actors.buffs.Terror;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.effects.CellEmitter;
import com.watabou.pixeldungeon.effects.Speck;
import com.watabou.pixeldungeon.items.Gold;
import com.watabou.pixeldungeon.items.weapon.enchantments.Death;
import com.watabou.pixeldungeon.sprites.CharSprite;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.utils.Random;

/**
 * Created by DeadDie on 12.02.2016
 */
public class Zombie extends Mob {
    {
        hp(ht(45));
        defenseSkill = 20;

        EXP = 10;
        maxLvl = 15;

        loot = Gold.class;
        lootChance = 0.02f;

        IMMUNITIES.add( Paralysis.class );
        IMMUNITIES.add( ToxicGas.class );
        IMMUNITIES.add( Terror.class );
        IMMUNITIES.add( Death.class );
        IMMUNITIES.add( Amok.class );
        IMMUNITIES.add( Blindness.class );
        IMMUNITIES.add( Sleep.class );
    }

    @Override
    public int attackProc( Char enemy, int damage ) {
        //Roots proc
        if (Random.Int(10) == 1){
            Buff.affect(enemy, Roots.class);
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
