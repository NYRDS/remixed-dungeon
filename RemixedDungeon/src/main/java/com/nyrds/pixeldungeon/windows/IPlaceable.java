package com.nyrds.pixeldungeon.windows;

/**
 * Created by mike on 25.06.2018.
 * This file is part of Remixed Pixel Dungeon.
 */
public interface IPlaceable {
    float width();
    float height();
    void setPos(float x, float y);
    float getX();
    float getY();

    IPlaceable shadowOf();
}
