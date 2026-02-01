package com.nyrds.pixeldungeon.windows;

import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.ui.ItemSlot;

import lombok.Getter;

/**
 * An ItemSlot that can be grayed out to indicate missing items
 */
@Getter
public class GrayableItemSlot extends ItemSlot {

    private boolean grayedOut = false;

    public GrayableItemSlot() {
        super();
    }

    public GrayableItemSlot(Item item) {
        super(item);
    }

    public void setGrayedOut(boolean grayedOut) {
        this.grayedOut = grayedOut;
        if (icon != null) {
            if (grayedOut) {
                icon.brightness(0.3f); // Gray out the icon
            } else {
                icon.brightness(1.0f); // Normal brightness
            }
        }
    }

}