package com.nyrds.pixeldungeon.ml.actions;

import com.nyrds.pixeldungeon.game.GameLoop;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.items.Heap;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.windows.WndItem;

public class MapItemAction extends CharAction {
    public MapItemAction(int dst) {
        this.dst = dst;
    }

    @Override
    public void act(Char hero) {
        Level level = hero.level();
        int pos = hero.getPos();
        if (level.adjacent(pos, dst)) {

            Heap heap = level.getHeap(dst);
            if (heap != null) {
                Item item = heap.peek();
                GameLoop.addToScene(new WndItem(item, hero));
            }

            hero.readyAndIdle();

            return;
        }

        if (hero.getCloser(dst)) {
            return;
        }

        hero.readyAndIdle();
    }
}
