package com.nyrds.pixeldungeon.ml.actions;

import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.CharUtils;
import com.watabou.pixeldungeon.items.Heap;
import com.watabou.pixeldungeon.items.Item;

public class PickUp extends CharAction {
    public PickUp(int dst ) {
        this.dst = dst;
    }

    @Override
    public void act(Char hero) {
        if (hero.getPos() == dst) {

            Heap heap = hero.level().getHeap(hero.getPos());
            if (heap != null) {
                Item item = heap.peek();
                if (item.valid()) {
                    CharUtils.tryPickUp(hero, item);
                } else {
                    hero.readyAndIdle();
                }
            } else {
                hero.readyAndIdle();
            }
            return;
        }

        if (hero.getCloser(dst)) {
            return;
        }

        hero.readyAndIdle();
    }
}
