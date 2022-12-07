package com.nyrds.retrodungeon.mobs.guts;

import com.nyrds.retrodungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Sample;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.blobs.ToxicGas;
import com.watabou.pixeldungeon.actors.buffs.Burning;
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

//TODO: Monster must not rise if he had been burned at least once. Maybe change sprite to "kind 2", which will suggest that he burned
public class ZombieGnoll extends Mob {
    {
        hp(ht(210));
        defenseSkill = 27;

        exp = 7;
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

    @Override
    public void die(Object cause) {
        super.die(cause);

        if (Random.Int(100) > 65 && cause != Burning.class){
            ressurrect();

            CellEmitter.center(this.getPos()).start(Speck.factory(Speck.BONE), 0.3f, 3);
            Sample.INSTANCE.play(Assets.SND_DEATH);
            if (Dungeon.visible[getPos()]) {
                getSprite().showStatus( CharSprite.NEGATIVE, Game.getVar(R.string.Goo_StaInfo1));
                GLog.n(Game.getVar(R.string.ZombieGnoll_Info));
            }

        }

        if (Dungeon.hero.lvl() <= maxLvl + 2) {
            dropLoot();
        }
    }
}
