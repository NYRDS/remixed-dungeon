package com.nyrds.retrodungeon.mechanics.spells;

import android.support.annotation.NonNull;

import com.nyrds.retrodungeon.ml.R;
import com.nyrds.retrodungeon.mobs.common.MobFactory;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.mobs.Fraction;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.effects.Wound;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.plants.Sungrass;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;

import java.util.Collection;

/**
 * Created by DeadDie on 02.09.2017
 */
public class SummoningSpell extends Spell {

    protected int summonLimit = 1;


    private static final String TXT_MAXIMUM_PETS  	   = Game.getVar(R.string.Spells_SummonLimitReached);

	protected String mobKind = "Rat";

    @Override
    public boolean cast(@NonNull Char chr){
        if(chr instanceof Hero) {
            Hero hero = (Hero)chr;
            if (isSummoningLimitReached(hero)) {
                GLog.w(getLimitWarning(getSummonLimit()));
                return false;
            }

            hero.spend(castTime);
            hero.busy();
            hero.getSprite().zap(hero.getPos());
        }

        if(!super.cast(chr)) {
	        return false;
        }

	    Level level = Dungeon.level;
        int spawnPos = level.getEmptyCellNextTo(chr.getPos());

        Wound.hit(chr);
        Buff.detach(chr, Sungrass.Health.class);

        if (level.cellValid(spawnPos)) {
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
            level.spawnMob(pet);
        }
        
	    castCallback(chr);
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
            if (mob.isAlive() && mob.getMobClassName().equals(mobKind) ) {
                n++;
            }
        }

        return n;
    }

    public int getSummonLimit(){
        return summonLimit;
    }

    public Mob getSummonMob(){
        return MobFactory.mobByName(mobKind);
    }

    public String getLimitWarning(int limit){
        return Utils.format(TXT_MAXIMUM_PETS, this.name(), limit);
    }
}
