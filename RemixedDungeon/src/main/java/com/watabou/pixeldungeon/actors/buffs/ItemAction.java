package com.watabou.pixeldungeon.actors.buffs;

import com.watabou.pixeldungeon.items.Item;

public interface ItemAction {
    Item act(Item srcItem);

    String actionText(Item srcItem);

    void carrierFx();
}
