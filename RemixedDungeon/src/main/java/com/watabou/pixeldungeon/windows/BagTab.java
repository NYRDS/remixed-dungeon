package com.watabou.pixeldungeon.windows;

import com.nyrds.pixeldungeon.windows.ImageTab;
import com.watabou.noosa.Image;
import com.watabou.pixeldungeon.items.bags.Bag;
import com.watabou.pixeldungeon.ui.Icons;

class BagTab extends ImageTab {
    public final Bag bag;

    public BagTab(WndBag parent, Bag bag ) {
        super(parent, icon(bag));
        this.bag = bag;

    }

    private static Image icon(Bag bag) {
        switch (bag.getEntityKind()) {
            case "SeedPouch":
                return Icons.get( Icons.SEED_POUCH );
            case "ScrollHolder":
                return Icons.get( Icons.SCROLL_HOLDER );
            case "WandHolster":
                return Icons.get( Icons.WAND_HOLSTER );
            case "PotionBelt":
                return Icons.get( Icons.POTIONS_BELT );
            case "Keyring":
                return Icons.get( Icons.KEYRING );
            case "Quiver":
                return Icons.get( Icons.QUIVER);
            default:
                return Icons.get( Icons.BACKPACK );
        }
    }
}
