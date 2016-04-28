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
import com.watabou.pixeldungeon.sprites.CharSprite;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;

/**
 * Created by DeadDie on 12.02.2016
 */
public class Wererat extends Mob {

    private static final float TIME_TO_HATCH	= 4f;

    {
        hp(ht(100));
        defenseSkill = 25;

        EXP = 1;
        maxLvl = 30;

        pacified = true;
    }

    private static final String RAT_TRANSFORMING_STATE = "rat_transforming_state";

    private boolean transforming = false;

    @Override
    public void storeInBundle( Bundle bundle ) {
        super.storeInBundle( bundle );

        bundle.put(RAT_TRANSFORMING_STATE, transforming);
    }

    @Override
    public void restoreFromBundle( Bundle bundle ) {

        super.restoreFromBundle( bundle );
        transforming = bundle.getBoolean(RAT_TRANSFORMING_STATE);
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
            if(!transforming){
                spend( TIME_TO_HATCH );
                transforming = true;
                if (Dungeon.visible[getPos()]) {
                    getSprite().showStatus( CharSprite.NEGATIVE, Game.getVar(R.string.Goo_StaInfo1));
                    GLog.n(Game.getVar(R.string.Wererat_Info1));
                }
                PlayZap();
                return true;
            }
            else {
                int wereratPos = this.getPos();
                if (Dungeon.level.cellValid(wereratPos)) {
                    WereratTransformed wererat = new WereratTransformed();
                    wererat.setPos(wereratPos);
                    Dungeon.level.spawnMob(wererat, 0);
                    Sample.INSTANCE.play(Assets.SND_CURSED);
                }
                die(this);
                return true;
            }
        }
        return super.act();
    }

    @Override
    public void onZapComplete() {
        PlayZap();
    }

    public void PlayZap(){
        this.getSprite().zap(getEnemy().getPos(), new Callback() {
            @Override
            public void call() {
            }
        });
    }
}
