package com.nyrds.pixeldungeon.ml.actions;

import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.items.Heap;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.utils.GLog;

public class PickUp extends CharAction {
    public PickUp(int dst ) {
        this.dst = dst;
    }

    @Override
    public boolean act(Char hero) {
        if (hero.getPos() == dst) {

            Heap heap = Dungeon.level.getHeap(hero.getPos());
            if (heap != null) {
                Item item = heap.pickUp();
                item = item.pick(hero, hero.getPos());
                if (item != null) {
                    if (item.doPickUp(hero)) {

                        hero.itemPickedUp(item);

                        if (!heap.isEmpty()) {
                            GLog.i(Game.getVar(R.string.Hero_SomethingElse));
                        }
                        hero.curAction = null;
                    } else {
                        Heap newHeap = hero.level().drop(item, hero.getPos());

                        newHeap.sprite.drop();
                        newHeap.pickUpFailed();

                        hero.readyAndIdle();
                    }
                } else {
                    hero.readyAndIdle();
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
