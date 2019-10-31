package com.watabou.pixeldungeon.actors;

import com.nyrds.pixeldungeon.ai.MobAi;
import com.nyrds.pixeldungeon.ai.Wandering;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.Statistics;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.levels.Level;

public class RespawnerActor extends Actor {
    private static final float TIME_TO_RESPAWN = 50;
    private Level level;

    public RespawnerActor(Level level) {
        this.level = level;
    }

    @Override
    protected boolean act() {

        int hostileMobsCount = 0;
        for (Mob mob : level.mobs) {
            if (!mob.isPet()) {
                hostileMobsCount++;
            }
        }

        if (hostileMobsCount < level.nMobs()) {

            Mob mob = level.createMob();
            mob.setState(MobAi.getStateByClass(Wandering.class));
            if (Dungeon.hero.isAlive()) {
                level.spawnMob(mob);
                if (Statistics.amuletObtained) {
                    mob.beckon(Dungeon.hero.getPos());
                }
            }
        }
        spend(Dungeon.nightMode || Statistics.amuletObtained ? TIME_TO_RESPAWN / 2
                : TIME_TO_RESPAWN);
        return true;
    }
}
