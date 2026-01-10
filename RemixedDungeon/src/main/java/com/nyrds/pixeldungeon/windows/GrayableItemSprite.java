package com.nyrds.pixeldungeon.windows;

import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.sprites.ItemSprite;
import com.watabou.utils.Rect;

import lombok.Getter;

/**
 * An ItemSprite that can be grayed out to indicate missing items
 */
@Getter
public class GrayableItemSprite extends ItemSprite {

    private boolean grayedOut = false;
    private float width;
    private float height;

    public GrayableItemSprite() {
        super();
        this.width = SIZE;
        this.height = SIZE;
    }

    public GrayableItemSprite(Item item) {
        super(item);
        this.width = SIZE;
        this.height = SIZE;
    }

    public void setGrayedOut(boolean grayedOut) {
        this.grayedOut = grayedOut;
        if (grayedOut) {
            brightness(0.3f); // Gray out the icon
        } else {
            brightness(1.0f); // Normal brightness
        }
    }

    public void setSize(float width, float height) {
        this.width = width;
        this.height = height;
        float scaleX = width / SIZE;
        float scaleY = height / SIZE;
        setScaleXY(scaleX, scaleY);
    }

    public float width() {
        return width;
    }

    public float height() {
        return height;
    }

    public void setRect(float x, float y, float width, float height) {
        this.width = width;
        this.height = height;
        float scaleX = width / SIZE;
        float scaleY = height / SIZE;
        setScaleXY(scaleX, scaleY);
        setPos(x, y);
    }

}