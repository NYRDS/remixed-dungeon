package com.watabou.pixeldungeon.actors;

import com.nyrds.pixeldungeon.ai.MobAi;
import com.nyrds.pixeldungeon.ai.Wandering;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.Statistics;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.levels.Level;

public class RespawnerActor extends Actor {
    private static final float TIME_TO_RESPAWN = 50;
    private final Level level;

    public RespawnerActor(Level level) {
        this.level = level;
    }

    @Override
    protected boolean act() {

        final Hero hero = Dungeon.hero;

        if(hero.isAlive()) {

            int hostileMobsCount = 0;
            for (Mob mob : level.mobs) {
                if (!mob.isPet()) {
                    hostileMobsCount++;
                }
            }

            if (hostileMobsCount < level.nMobs()) {
                Mob mob = level.createMob();
                if (level.cellValid(mob.getPos())) {
                    mob.setState(MobAi.getStateByClass(Wandering.class));
                    level.spawnMob(mob);
                    if (Statistics.amuletObtained) {
                        mob.beckon(hero.getPos());
                    }
                }
            }
        }

        float time = Dungeon.nightMode || Statistics.amuletObtained ? TIME_TO_RESPAWN / 2
                : TIME_TO_RESPAWN;

        spend(time);
        return true;
    }
}
