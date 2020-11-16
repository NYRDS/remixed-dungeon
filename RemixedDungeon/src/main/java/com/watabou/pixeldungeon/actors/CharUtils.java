package com.watabou.pixeldungeon.actors;

import com.nyrds.pixeldungeon.ml.EventCollector;
import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Sample;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.ResultDescriptions;
import com.watabou.pixeldungeon.actors.buffs.Invisibility;
import com.watabou.pixeldungeon.actors.mobs.Mimic;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.effects.Speck;
import com.watabou.pixeldungeon.effects.particles.SparkParticle;
import com.watabou.pixeldungeon.items.Heap;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.wands.WandOfBlink;
import com.watabou.pixeldungeon.levels.traps.LightningTrap;
import com.watabou.pixeldungeon.mechanics.Ballistica;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.utils.Random;

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
        return !attacker.adjacent(enemy)
                && Ballistica.cast( attacker.getPos(), enemy.getPos(), false, true ) == enemy.getPos();
    }

    public static boolean steal(@NotNull Char thief, @NotNull Char victim) {

        if(!thief.adjacent(victim)) {
            return false;
        }

        if (victim.getBelongings().isBackpackEmpty()) {
            return false;
        }

        Item item = victim.getBelongings().randomUnequipped();

        GLog.w( Game.getVars(R.array.Char_Stole)[thief.getGender()], thief.getName(), item.name(), victim.getName_objective() );

        item.detachAll( victim.getBelongings().backpack );

        thief.collect(item);

        return true;
    }

    public static void teleportRandom(Char ch ) {
        if(Dungeon.level.isBossLevel() || !ch.isMovable()) {
            GLog.w( Utils.format(R.string.ScrollOfTeleportation_NoTeleport2, ch.getName_objective()) );
            return;
        }

        int pos = Dungeon.level.randomRespawnCell();

        if (!Dungeon.level.cellValid(pos)) {
            GLog.w( Utils.format(R.string.ScrollOfTeleportation_NoTeleport2, ch.getName_objective()) );
        } else {
            WandOfBlink.appear( ch, pos );
            Dungeon.level.press( pos, ch );
            Dungeon.observe();
            GLog.i( Utils.format(R.string.ScrollOfTeleportation_Teleport2, ch.getName_objective()) );
        }
    }

    public static boolean hit(@NotNull Char attacker, Char defender, boolean magic) {
        if(attacker.invisible>0) {
            return true;
        }

        float acuRoll = Random.Float(attacker.attackSkill(defender));
        float defRoll = Random.Float(defender.defenseSkill(attacker));
        return (magic ? acuRoll * 2 : acuRoll) >= defRoll;
    }

    public static void challengeAllMobs(Char ch, String sound) {

        if (!GameScene.isSceneReady()) {
            return;
        }

        for (Mob mob : ch.level().mobs) {
            mob.beckon(ch.getPos());
        }

        for (Heap heap : ch.level().allHeaps()) {
            if (heap.type == Heap.Type.MIMIC) {
                Mimic m = Mimic.spawnAt(heap.pos, heap.items);
                if (m != null) {
                    m.beckon(ch.getPos());
                    heap.destroy();
                }
            }
        }

        ch.getSprite().centerEmitter().start(Speck.factory(Speck.SCREAM), 0.3f, 3);

        Sample.INSTANCE.play(sound);
        Invisibility.dispel(ch);
    }
}
