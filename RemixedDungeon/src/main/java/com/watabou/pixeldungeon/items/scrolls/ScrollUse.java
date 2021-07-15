package com.watabou.pixeldungeon.items.scrolls;

import com.nyrds.platform.audio.Sample;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.actors.buffs.Invisibility;
import com.watabou.pixeldungeon.windows.WndBag;

class ScrollUse {
    private final InventoryScroll inventoryScroll;

    public ScrollUse(InventoryScroll inventoryScroll) {
        this.inventoryScroll = inventoryScroll;
    }

    public WndBag.Listener invoke() {
        return (item, selector) -> {
            if (item != null) {
                inventoryScroll.onItemSelected( item, selector );
                selector.spend( Scroll.TIME_TO_READ );

                Sample.INSTANCE.play( Assets.SND_READ );
                Invisibility.dispel(selector);
            } else if (InventoryScroll.identifiedByUse) {
                inventoryScroll.confirmCancellation();
            } else {
                inventoryScroll.collect( selector.getBelongings().backpack );
            }
        };
    }
}
