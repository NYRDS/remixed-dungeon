package com.nyrds.pixeldungeon.ml.actions;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.audio.Sample;
import com.nyrds.platform.game.Game;
import com.watabou.noosa.Camera;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.items.Heap;
import com.watabou.pixeldungeon.items.keys.GoldenKey;
import com.watabou.pixeldungeon.items.keys.Key;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.utils.GLog;

public class OpenChest extends CharAction {
    public OpenChest(int dst ) {
        this.dst = dst;
    }

    @Override
    public boolean act(Char hero) {
        Level level = hero.level();
        int heroPos = hero.getPos();

        if (level.adjacent(heroPos, dst) || heroPos == dst) {

            Heap heap = level.getHeap(dst);
            if (heap != null && (heap.type == Heap.Type.CHEST || heap.type == Heap.Type.TOMB || heap.type == Heap.Type.SKELETON
                    || heap.type == Heap.Type.LOCKED_CHEST || heap.type == Heap.Type.CRYSTAL_CHEST || heap.type == Heap.Type.MIMIC)) {

                final Key[] theKey = {null};

                if (heap.type == Heap.Type.LOCKED_CHEST || heap.type == Heap.Type.CRYSTAL_CHEST) {

                    theKey[0] = hero.getBelongings().getKey(GoldenKey.class, Dungeon.depth, level.levelId);

                    if (theKey[0] == null) {
                        GLog.w(Game.getVar(R.string.Hero_LockedChest));
                        hero.readyAndIdle();
                        return false;
                    }
                }

                switch (heap.type) {
                    case TOMB:
                        Sample.INSTANCE.play(Assets.SND_TOMB);
                        Camera.main.shake(1, 0.5f);
                        break;
                    case SKELETON:
                        break;
                    default:
                        Sample.INSTANCE.play(Assets.SND_UNLOCK);
                }

                hero.doOperate(Key.TIME_TO_UNLOCK);

                if (theKey[0] != null) {
                    theKey[0].removeItemFrom(hero);
                }

                Heap OpenedHeap = level.getHeap(dst);
                if (OpenedHeap != null) {
                    if (OpenedHeap.type == Heap.Type.SKELETON) {
                        Sample.INSTANCE.play(Assets.SND_BONES);
                    }
                    OpenedHeap.open(hero);
                }


            } else {
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
