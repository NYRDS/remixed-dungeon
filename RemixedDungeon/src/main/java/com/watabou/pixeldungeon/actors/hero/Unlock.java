package com.watabou.pixeldungeon.actors.hero;

import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Sample;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.items.keys.IronKey;
import com.watabou.pixeldungeon.items.keys.Key;
import com.watabou.pixeldungeon.items.keys.SkeletonKey;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.levels.Terrain;
import com.watabou.pixeldungeon.utils.GLog;

public class Unlock extends CharAction {
    public Unlock(int door ) {
        this.dst = door;
    }

    public boolean act(Hero hero) {
        Level level = hero.level();
        if (level.adjacent(hero.getPos(), dst)) {
            hero.theKey = null;
            int door = level.map[dst];

            if (door == Terrain.LOCKED_DOOR) {
                hero.theKey = hero.getBelongings().getKey(IronKey.class, Dungeon.depth, level.levelId);
            } else if (door == Terrain.LOCKED_EXIT) {
                hero.theKey = hero.getBelongings().getKey(SkeletonKey.class, Dungeon.depth, level.levelId);
            }

            if (hero.theKey != null) {
                hero.spend(Key.TIME_TO_UNLOCK);
                hero.getSprite().operate(dst);
                Sample.INSTANCE.play(Assets.SND_UNLOCK);
            } else {
                GLog.w(Game.getVar(R.string.Hero_LockedDoor));
                hero.readyAndIdle();
            }
            return false;
        }

        if (hero.getCloser(dst)) {
            return true;
        }

        hero.readyAndIdle();
        return false;
    }
}
