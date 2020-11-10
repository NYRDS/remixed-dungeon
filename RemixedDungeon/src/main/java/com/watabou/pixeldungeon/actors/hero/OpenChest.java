package com.watabou.pixeldungeon.actors.hero;

import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Sample;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.items.Heap;
import com.watabou.pixeldungeon.items.keys.GoldenKey;
import com.watabou.pixeldungeon.items.keys.Key;
import com.watabou.pixeldungeon.utils.GLog;

public class OpenChest extends CharAction {
    public OpenChest(int dst ) {
        this.dst = dst;
    }

    @Override
    public boolean act(Hero hero) {
        if (Dungeon.level.adjacent(hero.getPos(), dst) || hero.getPos() == dst) {

            Heap heap = Dungeon.level.getHeap(dst);
            if (heap != null && (heap.type == Heap.Type.CHEST || heap.type == Heap.Type.TOMB || heap.type == Heap.Type.SKELETON
                    || heap.type == Heap.Type.LOCKED_CHEST || heap.type == Heap.Type.CRYSTAL_CHEST || heap.type == Heap.Type.MIMIC)) {

                hero.theKey = null;

                if (heap.type == Heap.Type.LOCKED_CHEST || heap.type == Heap.Type.CRYSTAL_CHEST) {

                    hero.theKey = hero.getBelongings().getKey(GoldenKey.class, Dungeon.depth, Dungeon.level.levelId);

                    if (hero.theKey == null) {
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

                hero.spend(Key.TIME_TO_UNLOCK);
                hero.getSprite().operate(dst);

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
