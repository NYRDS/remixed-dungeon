package com.nyrds.pixeldungeon.mobs.guts;

import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Sample;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.effects.CellEmitter;
import com.watabou.pixeldungeon.effects.Pushing;
import com.watabou.pixeldungeon.effects.Speck;
import com.watabou.pixeldungeon.items.Gold;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.utils.Random;

/**
 * Created by DeadDie on 12.02.2016
 */
public class Wererat extends Mob {

    private static final float TIME_TO_HATCH	= 2f;

    {
        hp(ht(100));
        defenseSkill = 25;

        EXP = 15;
        maxLvl = 30;

        loot = Gold.class;
        lootChance = 0.2f;
        pacified = true;
    }

    @Override
    public int damageRoll() {
        return Random.NormalIntRange(10, 15);
    }

    @Override
    public int attackSkill( Char target ) {
        return 25;
    }

    @Override
    public int dr() {
        return 2;
    }

    @Override
    public boolean act() {
        if (enemySeen){
            GLog.n(Game.getVar(R.string.Goo_Info1));
            spend( TIME_TO_HATCH );

            int wereratPos = this.getPos();

            if (Dungeon.level.cellValid(wereratPos)) {

                WereratTransformed wererat = new WereratTransformed();
                wererat.setPos(wereratPos);
                Dungeon.level.spawnMob(wererat, 0);
                Sample.INSTANCE.play(Assets.SND_CURSED);
                die(this);
            }
        }
        return super.act();
    }
}
