package com.nyrds.pixeldungeon.mobs.spiders;

import com.nyrds.pixeldungeon.levels.objects.LevelObjectsFactory;
import com.nyrds.pixeldungeon.mobs.common.MultiKindMob;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.items.food.MysteryMeat;
import com.watabou.pixeldungeon.plants.Plant;
import com.watabou.utils.Random;

import org.jetbrains.annotations.NotNull;

public class SpiderExploding extends MultiKindMob {

    private static final String[] PlantClasses = {
            "Firebloom",
            "Icecap",
            "Sorrowmoss",
            "Dreamweed",
            "Sungrass",
            "Earthroot",
            "Fadeleaf",
            "Moongrace"
    };

    public SpiderExploding() {
        hp(ht(5));
        baseDefenseSkill = 1;
        baseAttackSkill  = 125;
        baseSpeed = 2f;
        dmgMin = 3;
        dmgMax = 6;
        dr = 0;

        expForKill = 3;
        maxLvl = 9;

        kind = Random.IntRange(0, 7);

        loot(new MysteryMeat(), 0.067f);
    }

    @Override
    public boolean attack(@NotNull Char enemy) {
        if (super.attack(enemy)) {

            Plant plant = (Plant) LevelObjectsFactory.objectByName(PlantClasses[getKind()]);
            plant.effect(enemy.getPos(), enemy);

            die(this);
            return true;
        }
        return false;
    }
}
