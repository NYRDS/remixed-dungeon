package com.watabou.pixeldungeon.actors;

import com.nyrds.pixeldungeon.ml.EventCollector;
import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.ResultDescriptions;
import com.watabou.pixeldungeon.effects.particles.SparkParticle;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.levels.traps.LightningTrap;
import com.watabou.pixeldungeon.mechanics.Ballistica;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CharUtils {
    static public boolean isVisible(@Nullable Char ch) {
        if(ch==null) {
            return false;
        }

        if(!ch.level().cellValid(ch.getPos())) {
            EventCollector.logException("Checking visibility on invalid cell");
            return false;
        }

        return Dungeon.visible[ch.getPos()];
    }

    public static void checkDeathReport(Char attacker, @NotNull Char victim, String desc) {
        if (!victim.isAlive() && victim == Dungeon.hero) {
            Dungeon.fail(Utils.format(ResultDescriptions.getDescription(ResultDescriptions.Reason.MOB),
                    Utils.indefinite(attacker.getName()), Dungeon.depth));
            GLog.n(desc, attacker.getName());
        }
    }

    public static void lightningProc(@NotNull Char enemy, int damage) {

        if (Dungeon.level.water[enemy.getPos()] && !enemy.isFlying()) {
            damage *= 2f;
        }

        enemy.damage( damage, LightningTrap.LIGHTNING );

        enemy.getSprite().centerEmitter().burst( SparkParticle.FACTORY, 3 );
        enemy.getSprite().flash();

        if (enemy == Dungeon.hero) {
            Camera.main.shake( 2, 0.3f );
        }
    }

    public static boolean canDoOnlyRangedAttack(@NotNull Char attacker, @NotNull Char enemy) {
        return !Dungeon.level.adjacent( attacker.getPos(), enemy.getPos() )
                && Ballistica.cast( attacker.getPos(), enemy.getPos(), false, true ) == enemy.getPos();
    }

    public static boolean steal(@NotNull Char thief, @NotNull Char victim) {

        if (victim.getBelongings().isBackpackEmpty()) {
            return false;
        }

        Item item = victim.getBelongings().randomUnequipped();

        GLog.w( Game.getVars(R.array.Char_Stole)[thief.getGender()], thief.getName(), item.name(), victim.getName_objective() );

        item.detachAll( victim.getBelongings().backpack );

        thief.collect(item);

        return true;
    }
}
