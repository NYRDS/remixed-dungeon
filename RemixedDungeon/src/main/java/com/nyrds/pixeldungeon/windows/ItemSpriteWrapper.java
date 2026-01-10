package com.nyrds.pixeldungeon.windows;

import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.sprites.ItemSprite;

/**
 * An ItemSprite wrapper that provides setSize and positioning methods similar to ItemSlot
 */
public class ItemSpriteWrapper extends ItemSprite {

    private float width;
    private float height;

    public ItemSpriteWrapper() {
        super();
        this.width = SIZE;
        this.height = SIZE;
    }

    public ItemSpriteWrapper(Item item) {
        super(item);
        this.width = SIZE;
        this.height = SIZE;
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