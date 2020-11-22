package com.nyrds.pixeldungeon.mobs.guts;

import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.blobs.ToxicGas;
import com.watabou.pixeldungeon.actors.buffs.Amok;
import com.watabou.pixeldungeon.actors.buffs.Burning;
import com.watabou.pixeldungeon.actors.buffs.Paralysis;
import com.watabou.pixeldungeon.actors.buffs.Sleep;
import com.watabou.pixeldungeon.actors.buffs.Terror;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.actors.mobs.Yog;
import com.watabou.pixeldungeon.items.potions.PotionOfHealing;
import com.watabou.utils.Random;

/**
 * Created by DeadDie on 12.02.2016
 */
public class YogsHeart extends Mob {
    {
        hp(ht(450));
        baseDefenseSkill = 40;
        baseAttackSkill  = 26;

        exp = 12;

        addImmunity(ToxicGas.class);
        addImmunity(Paralysis.class);
        addImmunity(Amok.class);
        addImmunity(Sleep.class);
        addImmunity(Terror.class);
        addImmunity(Burning.class);
    }

    @Override
    public int damageRoll() {
        return Random.NormalIntRange(35, 45);
    }

    @Override
    public int dr() {
        return 22;
    }

    @Override
    public int defenseProc(Char enemy, int damage) {

        int larvaPos = Dungeon.level.getEmptyCellNextTo(getPos());

        if (Dungeon.level.cellValid(larvaPos)) {
            Yog.Larva larva = new Yog.Larva();
            larva.setPos(larvaPos);
            Dungeon.level.spawnMob(larva, 0, getPos());
        }

        return super.defenseProc(enemy, damage);
    }

	@Override
	public boolean act() {

		Mob mob = Dungeon.level.getRandomMob();

		if(mob!=null && mob.isAlive() && !mob.isPet()) {
			PotionOfHealing.heal(mob,0.2f);
		}

		return super.act();
	}

    @Override
    public boolean canBePet() {
        return false;
    }
}
