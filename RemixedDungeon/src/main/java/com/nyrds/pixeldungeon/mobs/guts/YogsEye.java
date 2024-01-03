package com.nyrds.pixeldungeon.mobs.guts;

import com.nyrds.pixeldungeon.ai.MobAi;
import com.nyrds.pixeldungeon.ai.Sleeping;
import com.nyrds.pixeldungeon.game.GameLoop;
import com.nyrds.pixeldungeon.mechanics.NamedEntityKind;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.mobs.common.MobFactory;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.Badges;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.CharUtils;
import com.watabou.pixeldungeon.actors.blobs.ToxicGas;
import com.watabou.pixeldungeon.actors.buffs.Amok;
import com.watabou.pixeldungeon.actors.buffs.Burning;
import com.watabou.pixeldungeon.actors.buffs.Charm;
import com.watabou.pixeldungeon.actors.buffs.Sleep;
import com.watabou.pixeldungeon.actors.buffs.Terror;
import com.watabou.pixeldungeon.actors.mobs.Boss;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.effects.CellEmitter;
import com.watabou.pixeldungeon.effects.particles.PurpleParticle;
import com.watabou.pixeldungeon.items.keys.SkeletonKey;
import com.watabou.pixeldungeon.items.scrolls.ScrollOfPsionicBlast;
import com.watabou.pixeldungeon.items.weapon.enchantments.Death;
import com.watabou.pixeldungeon.mechanics.Ballistica;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.sprites.CharSprite;
import com.watabou.pixeldungeon.sprites.YogSprite;
import com.watabou.utils.Random;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

/**
 * Created by DeadDie on 12.02.2016
 */
public class YogsEye extends Boss {

    int [] trace;
    int distance = 0;

    public YogsEye() {
        spriteClass = YogSprite.class;
        movable = false;
        hp(ht(1000));

        expForKill = 50;

        baseDefenseSkill = 30;
        baseAttackSkill = 30;

        dmgMin = 20;
        dmgMax = 30;

        setState(MobAi.getStateByClass(Sleeping.class));

        addImmunity(Death.class);
        addImmunity(Terror.class);
        addImmunity(Amok.class);
        addImmunity(Charm.class);
        addImmunity(Sleep.class);
        addImmunity(Burning.class);
        addImmunity(ToxicGas.class);
        addImmunity(ScrollOfPsionicBlast.class);

        collect(new SkeletonKey());
    }

    public void spawnOrgans() {
        String[] secondaryBossArray = {"RottingFist", "BurningFist", "YogsBrain", "YogsHeart", "YogsTeeth"};
        var names = new ArrayList<String>();

        int organsCount = GameLoop.getDifficulty() > 2 ? 3 : 2;

        do {
            var candidate = Random.oneOf(secondaryBossArray);
            if (!names.contains(candidate)) {
                names.add(candidate);
            }
        } while (names.size() < organsCount);

        for (var candidate : names) {
            var organ = MobFactory.mobByName(candidate);
            organ.setPos(level().getNearestTerrain(getPos(),
                    (level, cell) -> level.passable[cell] && Actor.findChar(cell) == null)
            );
            level().spawnMob(organ);
        }
    }

    @Override
    public void damage(int dmg, @NotNull NamedEntityKind src) {

        int damageShift = 0;
        for (Mob mob : level().mobs) {
            if (mob.isBoss() && !(mob instanceof YogsEye)) {
                mob.beckon(getPos());
                damageShift++;
            }
        }

        dmg >>= damageShift;

        super.damage(dmg, src);
    }

    @Override
    public int defenseProc(Char enemy, int damage) {
        CharUtils.spawnOnNextCell(this, "Larva", (int) (10 * GameLoop.getDifficultyFactor()));

        return super.defenseProc(enemy, damage);
    }


    @Override
    public boolean canAttack(@NotNull Char enemy) {

        Ballistica.cast(getPos() - level().getWidth(), enemy.getPos(), true, false);

        trace = Ballistica.trace.clone();
        distance = Ballistica.distance;

        for (int i = 1; i < distance; i++) {
            if (trace[i] == enemy.getPos()) {
                GameScene.zapEffect(getPos() - level().getWidth(), enemy.getPos(), "DeathRay");
                return true;
            }
        }
        return false;
    }



    @Override
    protected int zapProc(@NotNull Char enemy, int damage) {
        for (int i = 1; i < distance; i++) {
            int cell = trace[i];

            Char victim = Actor.findChar(cell);
            if (victim != null) {
                if (CharUtils.hit(this, victim, true)) {
                    victim.damage(Random.NormalIntRange(dmgMin, dmgMax), this);
                    int pos = victim.getPos();

                    if (Dungeon.isCellVisible(pos)) {
                        victim.getSprite().flash();
                        CellEmitter.center(pos).burst(PurpleParticle.BURST, Random.IntRange(2, 3));
                    }

                    CharUtils.checkDeathReport(this ,victim, StringsManager.getVar(R.string.Eye_Kill));
                } else {
                    victim.showStatus(CharSprite.NEUTRAL, victim.defenseVerb());
                }
            }
        }
        distance = 0;
        return damage;
    }

    @Override
    public void beckon(int cell) {
    }

    @Override
    public void die(@NotNull NamedEntityKind cause) {
        Mob mob = level().getRandomMob();
        while (mob != null) {
            mob.remove();
            mob = level().getRandomMob();
        }

        Badges.validateBossSlain(Badges.Badge.YOG_SLAIN);
        super.die(cause);

        yell(StringsManager.getVar(R.string.Yog_Info1));
    }

    @Override
    public void notice() {
        super.notice();
        yell(StringsManager.getVar(R.string.Yog_Info2));
    }

}