package com.nyrds.pixeldungeon.mobs.necropolis;

import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Sample;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.blobs.ToxicGas;
import com.watabou.pixeldungeon.actors.buffs.Paralysis;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.effects.CellEmitter;
import com.watabou.pixeldungeon.effects.Speck;
import com.watabou.pixeldungeon.items.Gold;
import com.watabou.pixeldungeon.sprites.CharSprite;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.utils.Random;

/**
 * Created by DeadDie on 12.02.2016
 */
public class Zombie extends Mob {
    {
        hp(ht(210));
        defenseSkill = 27;

        EXP = 7;
        maxLvl = 35;

        loot = Gold.class;
        lootChance = 0.02f;

        IMMUNITIES.add(Paralysis.class);
        IMMUNITIES.add(ToxicGas.class);
    }

    @Override
    public int damageRoll() {
        return Random.NormalIntRange(15, 35);
    }

    @Override
    public int attackSkill( Char target ) {
        return 25;
    }

    @Override
    public int dr() {
        return 20;
    }


}
