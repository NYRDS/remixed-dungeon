package com.nyrds.retrodungeon.mobs.guts;

import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.blobs.ToxicGas;
import com.watabou.pixeldungeon.actors.buffs.Amok;
import com.watabou.pixeldungeon.actors.buffs.Paralysis;
import com.watabou.pixeldungeon.actors.buffs.Sleep;
import com.watabou.pixeldungeon.actors.buffs.Terror;
import com.watabou.pixeldungeon.actors.mobs.Boss;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.actors.mobs.Yog;
import com.watabou.pixeldungeon.effects.Pushing;
import com.watabou.pixeldungeon.items.potions.PotionOfHealing;
import com.watabou.utils.Random;

/**
 * Created by DeadDie on 12.02.2016
 */
public class YogsHeart extends Boss {
    {
        hp(ht(250));
        defenseSkill = 30;

        exp = 12;

        IMMUNITIES.add(ToxicGas.class);
        IMMUNITIES.add(Paralysis.class);
        IMMUNITIES.add(Amok.class);
        IMMUNITIES.add(Sleep.class);
        IMMUNITIES.add(Terror.class);
    }

    @Override
    public int damageRoll() {
        return Random.NormalIntRange(25, 35);
    }

    @Override
    public int attackSkill( Char target ) {
        return 21;
    }

    @Override
    public int dr() {
        return 12;
    }

    @Override
    public int defenseProc(Char enemy, int damage) {

        int larvaPos = Dungeon.level.getEmptyCellNextTo(getPos());

        if (Dungeon.level.cellValid(larvaPos)) {
            Yog.Larva larva = new Yog.Larva();
            larva.setPos(larvaPos);
            Dungeon.level.spawnMob(larva, 0);
            Actor.addDelayed(new Pushing(larva, getPos(), larva.getPos()), -1);
        }

        return super.defenseProc(enemy, damage);
    }

	@Override
	public boolean act() {

		Mob mob = Dungeon.level.getRandomMob();

		if(mob!=null && mob.isAlive() && !mob.isPet()) {
			PotionOfHealing.heal(mob,0.1f);
		}

		return super.act();
	}
}
