package com.nyrds.pixeldungeon.ai;


import com.nyrds.pixeldungeon.ml.EventCollector;
import com.nyrds.pixeldungeon.utils.CharsList;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.utils.Utils;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public abstract class MobAi implements AiState {

    private static Map<String, AiState> aiStateInstances = new HashMap<>();

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
    }

    @Override
    public String getTag() {
        return getClass().getSimpleName().toUpperCase(Locale.ROOT);
    }

    protected void seekRevenge(Mob me, Object src) {
        if(src == me) { //no selfharm
            return;
        }

        if (src instanceof Char && !me.friendly((Char)src)) {
            me.setEnemy((Char) src);
        }else {
            me.setEnemy(chooseEnemy(me));
        }

        if (me.isEnemyInFov()) {
            me.setState(MobAi.getStateByClass(Hunting.class));
        } else {
            me.target = me.respawnCell(me.level());
            me.setState(MobAi.getStateByClass(Wandering.class));
        }
    }

    @Override
    public String status(Mob me) {
        return Utils.format("This %s is %s", me.getName(), getTag());
    }

    protected Char chooseNearestChar(Mob me) {

        Char bestEnemy = CharsList.DUMMY;
        int dist = me.level().getLength();

        for (Char chr : Actor.chars.values()) {

            if(chr==me) {
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


    protected Char chooseEnemy(Mob me) {

        Char bestEnemy = CharsList.DUMMY;
        int dist = me.level().getLength();

        for (Char chr : Actor.chars.values()) {
            if (me.level().fieldOfView[chr.getPos()]) {
                if (!me.friendly(chr)) {
                    int candidateDist = me.level().distance(me.getPos(), chr.getPos());
                    if (candidateDist < dist) {
                        bestEnemy = chr;
                        dist = candidateDist;
                    }
                }
            }
        }

        return bestEnemy;
    }

    protected void huntEnemy(Mob me) {

        if (me.getEnemy().valid()) {
            me.enemySeen = true;
            me.target = me.getEnemy().getPos();

            me.notice();
            me.setState(getStateByClass(Hunting.class));
        }
    }

    public boolean returnToOwnerIfTooFar(Mob me, int maxDist) {
        if(     me.level().distance(me.getPos(),me.getOwnerPos())>maxDist
            &&  me.level().distance(me.target,  me.getOwnerPos())>maxDist
        ) {
            me.target = me.getOwnerPos();
            me.setState(getStateByClass(Wandering.class));
            return true;
        }
        return false;
    }

    private static void registerAiState(Class<? extends AiState> stateClass) {
        try {
            aiStateInstances.put(stateClass.getSimpleName().toUpperCase(Locale.ROOT), stateClass.newInstance());
        } catch (InstantiationException e) {
            EventCollector.logException(e);
        } catch (IllegalAccessException e) {
            EventCollector.logException(e);
        }
    }

    public static AiState getStateByTag(String stateTag) {
        String tag = stateTag.toUpperCase(Locale.ROOT);
        AiState aiState = aiStateInstances.get(tag);

        if (aiState != null) {
            return aiState;
        }

        aiState = new CustomMobAi(stateTag);

        aiStateInstances.put(tag, aiState);

        return aiState;
    }


    public static AiState getStateByClass(Class<? extends AiState> stateClass) {
        return aiStateInstances.get(stateClass.getSimpleName().toUpperCase(Locale.ROOT));
    }

    @Override
    public void onDie() { // do nothing, we are dead already...
    }
}
