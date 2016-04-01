package com.nyrds.pixeldungeon.mobs.guts;

import com.watabou.noosa.audio.Sample;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.effects.Pushing;
import com.watabou.pixeldungeon.items.Gold;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.utils.Random;

/**
 * Created by DeadDie on 12.02.2016
 */
public class ZombieGnoll extends Mob {
    {
        hp(ht(100));
        defenseSkill = 25;

        EXP = 15;
        maxLvl = 30;

        loot = Gold.class;
        lootChance = 0.5f;
    }

    @Override
    public int damageRoll() {
        return Random.NormalIntRange(20, 35);
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
    public void die(Object cause) {
        super.die(cause);

        if (Random.Int(5) == 1){
            int gnollPosition = this.getPos();

            if (Dungeon.level.cellValid(gnollPosition)) {
                ZombieGnoll newGnoll = new ZombieGnoll();
                newGnoll.setPos(gnollPosition);
                Dungeon.level.spawnMob(newGnoll, 0);
                //Actor.addDelayed(new Pushing(newGnoll, getPos(), newGnoll.getPos()), -1);

                Sample.INSTANCE.play(Assets.SND_DEATH);
            }
        }

        if (Dungeon.hero.lvl <= maxLvl + 2) {
            dropLoot();
        }
    }
}
