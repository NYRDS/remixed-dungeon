package com.nyrds.pixeldungeon.mobs.guts;

import com.watabou.noosa.audio.Sample;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.blobs.ToxicGas;
import com.watabou.pixeldungeon.actors.buffs.Amok;
import com.watabou.pixeldungeon.actors.buffs.Bleeding;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Burning;
import com.watabou.pixeldungeon.actors.buffs.Paralysis;
import com.watabou.pixeldungeon.actors.buffs.Sleep;
import com.watabou.pixeldungeon.actors.buffs.Terror;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.mobs.Boss;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.effects.CellEmitter;
import com.watabou.pixeldungeon.effects.Speck;
import com.watabou.pixeldungeon.effects.Splash;
import com.watabou.pixeldungeon.items.Gold;
import com.watabou.utils.PointF;
import com.watabou.utils.Random;

/**
 * Created by DeadDie on 12.02.2016
 */
public class YogsTeeth extends Boss {
    {
        hp(ht(150));
        defenseSkill = 44;

        EXP = 26;

        RESISTANCES.add(ToxicGas.class);

        IMMUNITIES.add(Paralysis.class);
        IMMUNITIES.add(Amok.class);
        IMMUNITIES.add(Sleep.class);
        IMMUNITIES.add(Terror.class);
    }


    @Override
    public int attackProc( Char enemy, int damage ) {
        //Life drain proc
        if (Random.Int(3) == 1){
            CellEmitter.center(this.getPos()).start(Speck.factory(Speck.HEALING), 0.3f, 3);
            this.hp(this.hp() + damage );
        }
        //Bleeding proc
        if (Random.Int(3) == 1){
            Buff.affect(enemy, Bleeding.class).set(damage);
        }
        //Double damage proc
        if (Random.Int(3) == 1){
            enemy.getSprite().bloodBurstA(getSprite().center(), 1000);
            Sample.INSTANCE.play(Assets.SND_BITE);
            return damage*2;
        }
        return damage;
    }

    @Override
    public int damageRoll() {
        return Random.NormalIntRange(40, 40);
    }

    @Override
    public int attackSkill( Char target ) { return 36; }

    @Override
    public int dr() { return 21; }
}
