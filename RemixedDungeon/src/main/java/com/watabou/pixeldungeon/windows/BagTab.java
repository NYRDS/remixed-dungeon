package com.watabou.pixeldungeon.windows;

import com.nyrds.pixeldungeon.windows.ImageTab;
import com.watabou.noosa.Image;
import com.watabou.pixeldungeon.items.bags.Bag;
import com.watabou.pixeldungeon.items.bags.Keyring;
import com.watabou.pixeldungeon.items.bags.PotionBelt;
import com.watabou.pixeldungeon.items.bags.Quiver;
import com.watabou.pixeldungeon.items.bags.ScrollHolder;
import com.watabou.pixeldungeon.items.bags.SeedPouch;
import com.watabou.pixeldungeon.items.bags.WandHolster;
import com.watabou.pixeldungeon.ui.Icons;

class BagTab extends ImageTab {
    public final Bag bag;

    public BagTab(WndBag parent, Bag bag ) {
        super(parent, icon(bag));
        this.bag = bag;

    }

    private static Image icon(Bag bag) {
        if (bag instanceof SeedPouch) {
            return Icons.get( Icons.SEED_POUCH );
        } else if (bag instanceof ScrollHolder) {
            return Icons.get( Icons.SCROLL_HOLDER );
        } else if (bag instanceof WandHolster) {
            return Icons.get( Icons.WAND_HOLSTER );
        } else if (bag instanceof PotionBelt) {
            return Icons.get( Icons.POTIONS_BELT );
        } else if (bag instanceof Keyring) {
            return Icons.get( Icons.KEYRING );
        } else if (bag instanceof Quiver) {
            return Icons.get( Icons.QUIVER);
        } else {
            return Icons.get( Icons.BACKPACK );
        }
    }
}
