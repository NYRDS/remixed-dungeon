package com.nyrds.pixeldungeon.mechanics.spells;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.mobs.common.Deathling;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.mobs.Fraction;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.actors.mobs.Rat;
import com.watabou.pixeldungeon.effects.Wound;
import com.watabou.pixeldungeon.plants.Sungrass;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;

import java.util.Collection;

/**
 * Created by DeadDie on 02.09.2017
 */
public class SummoningSpell extends Spell {

    private int summonLimit = 1;
    private static final String TXT_MAXIMUM_PETS  	   = Game.getVar(R.string.Spells_SummonLimitReached);

    @Override
    public boolean cast(Char chr){
        if(!super.cast(chr)) {
	        return false;
        }

        if(chr instanceof Hero) {
	        Hero hero = (Hero)chr;
	        if (isSummoningLimitReached(hero)) {
		        GLog.w(getLimitWarning(getSummonLimit()));
		        return false;
	        }
        }

        int spawnPos = Dungeon.level.getEmptyCellNextTo(chr.getPos());

        Wound.hit(chr);
        Buff.detach(chr, Sungrass.Health.class);

        if (Dungeon.level.cellValid(spawnPos)) {
	        Mob pet = getSummonMob();
	        if(chr instanceof Hero) {
		        Hero hero = (Hero)chr;
		        pet = Mob.makePet(pet, hero);
	        } else if(chr instanceof Mob) {
		        Mob mob = (Mob) chr;
		        pet.setFraction(mob.fraction());
	        } else {
		        pet.setFraction(Fraction.DUNGEON);
	        }
            pet.setPos(spawnPos);
            Dungeon.level.spawnMob(pet);
        }

        chr.spend(1/chr.speed());

	    return true;
    }


    public boolean isSummoningLimitReached(Hero hero){
        if (getSummonLimit() <= getNumberOfSummons(hero)){
            return true;
        }
        return false;
    }

    public int getNumberOfSummons(Hero hero){
        Collection<Mob> pets = hero.getPets();

        int n = 0;
        for (Mob mob : pets) {
            if (mob.isAlive() && mob instanceof Deathling) {
                n++;
            }
        }

        return n;
    }

    public int getSummonLimit(){
        return summonLimit;
    }

    public Mob getSummonMob(){
        return new Rat();
    }

    public String getLimitWarning(int limit){
        return Utils.format(TXT_MAXIMUM_PETS, this.name(), limit);
    }
}
