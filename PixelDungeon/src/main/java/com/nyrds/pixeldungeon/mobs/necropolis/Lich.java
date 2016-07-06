package com.nyrds.pixeldungeon.mobs.necropolis;

import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.blobs.ToxicGas;
import com.watabou.pixeldungeon.actors.buffs.Amok;
import com.watabou.pixeldungeon.actors.buffs.Blindness;
import com.watabou.pixeldungeon.actors.buffs.Paralysis;
import com.watabou.pixeldungeon.actors.buffs.Poison;
import com.watabou.pixeldungeon.actors.buffs.Sleep;
import com.watabou.pixeldungeon.actors.buffs.Terror;
import com.watabou.pixeldungeon.actors.mobs.Boss;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.items.Gold;
import com.watabou.pixeldungeon.items.weapon.enchantments.Death;
import com.watabou.pixeldungeon.mechanics.Ballistica;
import com.watabou.utils.Random;

/**
 * Created by DeadDie on 12.02.2016
 */
public class Lich extends Boss {
    {
        hp(ht(120));
        EXP = 20;
        defenseSkill = 20;

        baseSpeed = 0f;

        IMMUNITIES.add( Paralysis.class );
        IMMUNITIES.add( ToxicGas.class );
        IMMUNITIES.add( Terror.class );
        IMMUNITIES.add( Death.class );
        IMMUNITIES.add( Amok.class );
        IMMUNITIES.add( Blindness.class );
        IMMUNITIES.add( Sleep.class );
    }


    @Override
    protected boolean canAttack(Char enemy) {
        return Dungeon.level.distance(getPos(), enemy.getPos()) < 4 && Ballistica.cast(getPos(), enemy.getPos(), false, true) == enemy.getPos();
    }

    @Override
    protected boolean doAttack(Char enemy) {

        if (Dungeon.level.distance(getPos(), enemy.getPos()) <= 1) {
            return super.doAttack(enemy);
        } else {

            getSprite().zap(enemy.getPos());

            spend(1);

            if (hit(this, enemy, true)) {
                enemy.damage(damageRoll(), this);
            }
            return true;
        }
    }

    @Override
    public int damageRoll() {
        return Random.NormalIntRange( 8, 15 );
    }

    @Override
    public int attackSkill( Char target ) {
        return 20;
    }

    @Override
    public int dr() {
        return 5;
    }


}
