package com.nyrds.pixeldungeon.mechanics.spells;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.mobs.common.MobFactory;
import com.nyrds.pixeldungeon.utils.CharsList;
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

import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * Created by DeadDie on 02.09.2017
 */
public class SummoningSpell extends Spell {

    private int summonLimit = 1;

    protected String mobKind = "Rat";

    @Override
    public boolean canCast(@NotNull Char chr, boolean reallyCast) {
        if (!super.canCast(chr, reallyCast)) {
            return false;
        }

        if(chr instanceof Hero) {
            Hero hero = (Hero)chr;
            if (isSummoningLimitReached(hero)) {
                if(reallyCast) {
                    GLog.w(getLimitWarning(getSummonLimit()));
                }
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean cast(@NotNull Char chr){
        if(!super.cast(chr)) {
	        return false;
        }

	    Level level = Dungeon.level;
        int spawnPos = level.getEmptyCellNextTo(chr.getPos());

        Wound.hit(chr);
        Buff.detach(chr, Sungrass.Health.class);

        if (level.cellValid(spawnPos)) {
            Mob pet = MobFactory.mobByName(mobKind);

            if(chr instanceof Hero) {
		        pet = Mob.makePet(pet, chr.getId());
	        } else if(chr instanceof Mob) {
		        Mob mob = (Mob) chr;
		        pet.setFraction(mob.fraction());
	        } else {
		        pet.setFraction(Fraction.DUNGEON);
	        }

	        pet.setPos(spawnPos);
            level.spawnMob(pet,0,chr.getPos());
        }
        
	    castCallback(chr);
	    return true;
    }


    private boolean isSummoningLimitReached(Hero hero){
        return getSummonLimit() <= getNumberOfSummons(hero);
    }

    private int getNumberOfSummons(Hero hero){
        Collection<Integer> pets = hero.getPets();

        int n = 0;
        for (Integer mobId : pets) {
            Char aChar = CharsList.getById(mobId);
            if(aChar.valid()) {
                Mob mob = (Mob) aChar;
                if (mob.isAlive() && mob.getEntityKind().equals(mobKind)) {
                    n++;
                }
            }
        }

        return n;
    }

    private String getLimitWarning(int limit){
        return Utils.format(R.string.Spells_SummonLimitReached, this.name(), limit);
    }

    public int getSummonLimit() {
        return summonLimit;
    }
}
