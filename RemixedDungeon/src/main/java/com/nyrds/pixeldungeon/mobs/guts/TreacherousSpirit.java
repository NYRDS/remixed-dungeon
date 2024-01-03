package com.nyrds.pixeldungeon.mobs.guts;


import com.nyrds.pixeldungeon.ai.MobAi;
import com.nyrds.pixeldungeon.ai.Wandering;
import com.nyrds.pixeldungeon.items.guts.HeartOfDarkness;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.utils.Random;

import org.jetbrains.annotations.NotNull;

/**
 * Created by DeadDie on 12.02.2016
 */
public class TreacherousSpirit extends Mob {
    {
        hp(ht(200));
        baseDefenseSkill = 35;
        baseAttackSkill  = 35;
        dmgMin = 30;
        dmgMax = 45;
        dr = 25;

        expForKill = 45;
        maxLvl = 30;

        setState(MobAi.getStateByClass(Wandering.class));
        collect( new HeartOfDarkness());
    }

    @Override
    public int attackProc(@NotNull Char enemy, int damage ) {
        //Summon proc
        if (Random.Int(4) == 1){
            int spiritPos = Dungeon.level.getEmptyCellNextTo(getPos());

            if (Dungeon.level.cellValid(spiritPos)) {
                SpiritOfPain spirit = new SpiritOfPain();
                spirit.setPos(spiritPos);
                Dungeon.level.spawnMob(spirit, 0,getPos());
            }
        }
        return damage;
    }

    @Override
    public boolean canBePet(){
        return false;
    }

}
