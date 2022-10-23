package com.watabou.pixeldungeon.windows;

import com.nyrds.platform.compatibility.RectF;
import com.watabou.noosa.Image;
import com.watabou.pixeldungeon.items.bags.Bag;
import com.watabou.pixeldungeon.items.bags.Keyring;
import com.watabou.pixeldungeon.items.bags.PotionBelt;
import com.watabou.pixeldungeon.items.bags.Quiver;
import com.watabou.pixeldungeon.items.bags.ScrollHolder;
import com.watabou.pixeldungeon.items.bags.SeedPouch;
import com.watabou.pixeldungeon.items.bags.WandHolster;
import com.watabou.pixeldungeon.ui.Icons;
import com.watabou.pixeldungeon.windows.elements.Tab;

class BagTab extends Tab {

    private final Image icon;

    public Bag bag;

    public BagTab(WndBag parent, Bag bag ) {
        super(parent);

        this.bag = bag;

        icon = icon();
        add( icon );
    }

    @Override
    public void select( boolean value ) {
        super.select( value );
        icon.am = selected ? 1.0f : 0.6f;
    }

    @Override
    protected void layout() {
        super.layout();

        icon.copy( icon() );
        icon.setX(x + (width - icon.width) / 2);
        icon.setY(y + (height - icon.height) / 2 - 2 - (selected ? 0 : 1));
        if (!selected && icon.getY() < y + CUT) {
            RectF frame = icon.frame();
            frame.top += (y + CUT - icon.getY()) / icon.texture.height;
            icon.frame( frame );
            icon.setY(y + CUT);
        }
    }

    private Image icon() {
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
