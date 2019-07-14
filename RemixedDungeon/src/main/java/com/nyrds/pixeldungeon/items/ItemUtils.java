package com.nyrds.pixeldungeon.items;

import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.effects.Speck;
import com.watabou.pixeldungeon.items.Heap;
import com.watabou.pixeldungeon.items.Item;

public class ItemUtils {
    public static void throwItemAway(int pos) {
		Heap heap = Dungeon.level.getHeap( pos );
		Item item = heap.pickUp();
		int cell = Dungeon.level.getEmptyCellNextTo(pos);
		if(Dungeon.level.cellValid(cell)) {
			Dungeon.level.drop( item, cell ).sprite.drop( cell );
		}
	}

    public static void evoke(Hero hero) {
        hero.getSprite().emitter().burst(Speck.factory(Speck.EVOKE), 5);
    }
}
