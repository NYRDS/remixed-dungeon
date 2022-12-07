package com.nyrds.retrodungeon.mobs.guts;

import android.support.annotation.NonNull;

import com.nyrds.retrodungeon.items.guts.HeartOfDarkness;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.effects.Pushing;
import com.watabou.utils.Random;

/**
 * Created by DeadDie on 12.02.2016
 */
public class TreacherousSpirit extends Mob {
    {
        hp(ht(200));
        defenseSkill = 35;

        exp = 45;
        maxLvl = 30;

        setState(WANDERING);
        lootChance = 1f;
        loot = HeartOfDarkness.class;
    }

    @Override
    public int attackProc(@NonNull Char enemy, int damage ) {
        //Summon proc
        if (Random.Int(4) == 1){
            int spiritPos = Dungeon.level.getEmptyCellNextTo(getPos());

            if (Dungeon.level.cellValid(spiritPos)) {
                SpiritOfPain spirit = new SpiritOfPain();
                spirit.setPos(spiritPos);
                Dungeon.level.spawnMob(spirit, 0);
                Actor.addDelayed(new Pushing(spirit, getPos(), spirit.getPos()), -1);
            }
        }
        return damage;
    }

    @Override
    public int damageRoll() {
        return Random.NormalIntRange(30, 45);
    }

    @Override
    public int attackSkill( Char target ) {
        return 35;
    }

    @Override
    public int dr() {
        return 25;
    }

    @Override
    public boolean canBePet(){
        return false;
    }

}
