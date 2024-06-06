package com.nyrds.pixeldungeon.ai;


import com.nyrds.pixeldungeon.game.GameLoop;
import com.nyrds.pixeldungeon.mechanics.NamedEntityKind;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.utils.CharsList;
import com.nyrds.platform.EventCollector;
import com.watabou.pixeldungeon.Challenges;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.utils.Random;

import org.jetbrains.annotations.NotNull;

import org.apache.commons.collections4.map.HashedMap;
import java.util.Locale;
import java.util.Map;

public abstract class MobAi implements AiState {

    private static final Map<String, AiState> aiStateInstances = new HashedMap<>();

    static {
        registerAiState(Passive.class);
        registerAiState(Sleeping.class);
        registerAiState(Wandering.class);
        registerAiState(Hunting.class);
        registerAiState(Fleeing.class);
        registerAiState(ThiefFleeing.class);
        registerAiState(Horrified.class);
        registerAiState(RunningAmok.class);
        registerAiState(ControlledAi.class);
        registerAiState(MoveOrder.class);
        registerAiState(KillOrder.class);
    }

    @Override
    public String getTag() {
        return getClass().getSimpleName().toUpperCase(Locale.ROOT);
    }

    protected void seekRevenge(Char me, NamedEntityKind src) {
        if(src == me) { //no selfharm
            return;
        }

        if (src instanceof Char && !me.friendly((Char)src)) {
            me.setEnemy((Char) src);
        } else {
            if(!me.getEnemy().valid()) {
                me.setEnemy(chooseEnemy(me, 1.25f));
            }
        }

        if (me.isEnemyInFov()) {
            me.setState(MobAi.getStateByClass(Hunting.class));
        } else {
            me.setTarget(me.respawnCell(me.level()));
            me.setState(MobAi.getStateByClass(Wandering.class));
        }
    }

    @Override
    public String status(Char me) {
        return Utils.format(R.string.MobAi_status, me.getName(), getTag());
    }

    protected Char chooseNearestChar(@NotNull Char me) {

        Char bestEnemy = CharsList.DUMMY;
        int dist = me.level().getLength();

        for (Char chr : Actor.chars.values()) {

            if(chr==me) {
                continue;
            }

            if(chr.invisible>0) {
                continue;
            }

            if (me.level().fieldOfView[chr.getPos()]) {
                int candidateDist = me.level().distance(me.getPos(), chr.getPos());
                if (candidateDist < dist) {
                    bestEnemy = chr;
                    dist = candidateDist;
                }
            }
        }

        return bestEnemy;
    }


    protected Char chooseEnemy(@NotNull Char me, float attentionFactor) {

        attentionFactor *= me.getAttentionFactor();
        attentionFactor *= GameLoop.getDifficultyFactor();

        Char bestEnemy = CharsList.DUMMY;
        int dist = me.level().getLength();

        for (Char chr : Actor.chars.values()) {

            if(chr.invisible>0) {
                continue;
            }

            if (me.level().fieldOfView[chr.getPos()]) {
                if (!me.friendly(chr)) {
                    int candidateDist = me.level().distance(me.getPos(), chr.getPos());
                    if (candidateDist < dist) {
                        if(Random.Int((int) ((candidateDist + chr.stealth())/attentionFactor
                                                        + (chr.isFlying() ? 2 : 0))) == 0) {
                            bestEnemy = chr;
                            dist = candidateDist;
                        }
                    }
                }
            }
        }

        return bestEnemy;
    }

    protected void huntEnemy(@NotNull Char me) {

        final Char enemy = me.getEnemy();

        if (enemy.valid()) {
            me.enemySeen = true;
            final int enemyPos = enemy.getPos();

            me.setTarget(enemyPos);
            me.notice();
            me.setState(getStateByClass(Hunting.class));

            if (me.getOwnerId()==me.getId() && Dungeon.isChallenged(Challenges.SWARM_INTELLIGENCE)) {
                for (Mob mob : me.level().getCopyOfMobsArray()) {
                    if (me != mob && !mob.friendly(enemy)) {
                        mob.setEnemy(enemy);
                        mob.setTarget(enemyPos);
                        mob.notice();
                        mob.setState(getStateByClass(Hunting.class));
                    }
                }
            }
        }
    }

    public boolean returnToOwnerIfTooFar(@NotNull Char me, int maxDist) {
        final Level level = me.level();
        final int ownerPos = me.getOwnerPos();

        if(     level.distance(me.getPos(), ownerPos)>maxDist
            &&  level.distance(me.getTarget(), ownerPos)>maxDist
        ) {
            me.setTarget(ownerPos);
            me.setState(getStateByClass(Wandering.class));
            return true;
        }
        return false;
    }

    private static void registerAiState(@NotNull Class<? extends AiState> stateClass) {
        try {
            aiStateInstances.put(stateClass.getSimpleName().toUpperCase(Locale.ROOT), stateClass.newInstance());
        } catch (InstantiationException | IllegalAccessException e) {
            EventCollector.logException(e);
        }
    }

    public static @NotNull AiState getStateByTag(@NotNull String stateTag) {
        String tag = stateTag.toUpperCase(Locale.ROOT);
        AiState aiState = aiStateInstances.get(tag);

        if (aiState != null) {
            return aiState;
        }

        aiState = new CustomMobAi(stateTag);

        aiStateInstances.put(tag, aiState);

        return aiState;
    }


    public static AiState getStateByClass(@NotNull Class<? extends AiState> stateClass) {
        return aiStateInstances.get(stateClass.getSimpleName().toUpperCase(Locale.ROOT));
    }

    @Override
    public void onDie(@NotNull Char me) { // do nothing, we are dead already...
    }
}
