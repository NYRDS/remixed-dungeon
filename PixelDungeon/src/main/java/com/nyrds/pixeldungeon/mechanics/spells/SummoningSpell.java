package com.nyrds.pixeldungeon.mechanics.spells;

import com.nyrds.pixeldungeon.mechanics.Necromancy;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.utils.GLog;

/**
 * Created by DeadDie on 02.09.2017
 */
public class SummoningSpell extends Spell {

    private int summonLimit = 1;
    private int nSummons = 0;

    @Override
    public void use(Hero hero){
        if(!hero.spendSoulPoints(spellCost())){
            GLog.w( notEnoughSouls(name) );
            return;
        }
        if(isSummoningLimitReached()){
            GLog.w( Necromancy.getLimitWarning(getSummonLimit()) );
            return;
        }
        cast(hero);
    }

    @Override
    public void cast(Hero hero){
        nSummons ++;
    }

    public boolean isSummoningLimitReached(){
        if (getSummonLimit() <= getNumberOfSummons()){
            return true;
        }
        return false;
    }

    public int getNumberOfSummons(){
        return nSummons;
    }

    public int getSummonLimit(){
        return summonLimit;
    }
}
