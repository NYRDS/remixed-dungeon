package com.nyrds.pixeldungeon.mobs.guts;

import com.watabou.noosa.audio.Sample;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Bleeding;
import com.watabou.pixeldungeon.actors.buffs.Buff;
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

        loot = Gold.class;
        lootChance = 0.5f;
    }


    @Override
    public int attackProc( Char enemy, int damage ) {
        //Life drain proc
        if (Random.Int(3) == 1){
            CellEmitter.center(this.getPos()).start(Speck.factory(Speck.HEALING), 0.3f, 3);
            this.hp(this.hp() + damage / 2);
        }
        //Bleeding proc
        if (Random.Int(3) == 1){
            Buff.affect(enemy, Bleeding.class).set(damage);
        }
        //Double damage proc
        if (Random.Int(3) == 1){
            enemy.getSprite().bloodBurstA(getSprite().center(), 1000);
            Sample.INSTANCE.play(Assets.SND_FALLING);
            return damage*2;
        }
        return damage;
    }

    @Override
    public int damageRoll() {
        return Random.NormalIntRange(10, 30);
    }

    @Override
    public int attackSkill( Char target ) { return 36; }

    @Override
    public int dr() { return 2; }
}
