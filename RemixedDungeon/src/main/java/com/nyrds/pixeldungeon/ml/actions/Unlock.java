package com.nyrds.pixeldungeon.ml.actions;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.events.EventCollector;
import com.nyrds.platform.audio.Sample;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.items.keys.IronKey;
import com.watabou.pixeldungeon.items.keys.Key;
import com.watabou.pixeldungeon.items.keys.SkeletonKey;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.levels.Terrain;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.utils.GLog;

import org.jetbrains.annotations.NotNull;

public class Unlock extends CharAction {
    public Unlock(int door ) {
        this.dst = door;
    }

    public boolean act(@NotNull Char hero) {
        Level level = hero.level();
        if (level.adjacent(hero.getPos(), dst)) {
            Key theKey = null;
            int door = level.map[dst];

            if (door == Terrain.LOCKED_DOOR) {
                theKey = hero.getBelongings().getKey(IronKey.class, Dungeon.depth, level.levelId);
            } else if (door == Terrain.LOCKED_EXIT) {
                theKey = hero.getBelongings().getKey(SkeletonKey.class, Dungeon.depth, level.levelId);
            }

            if (theKey != null) {
                theKey.removeItemFrom(hero);

                switch (door) {
                    case Terrain.LOCKED_DOOR:
                        level.set(dst, Terrain.DOOR);
                        break;
                    case Terrain.LOCKED_EXIT:
                        level.set(dst, Terrain.UNLOCKED_EXIT);
                        break;
                    default:
                        EventCollector.logException("trying to unlock tile:" + door);
                }

                hero.doOperate(Key.TIME_TO_UNLOCK, dst, () -> {
                    GameScene.updateMapPair(dst);
                    hero.next();
                });

                Sample.INSTANCE.play(Assets.SND_UNLOCK);
                hero.setCurAction(null);
            } else {
                GLog.w(StringsManager.getVar(R.string.Hero_LockedDoor));
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
