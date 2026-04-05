package com.nyrds.pixeldungeon.levels;

import com.watabou.pixeldungeon.levels.RegularLevel;
import com.watabou.pixeldungeon.levels.Room;
import com.watabou.pixeldungeon.levels.painters.WarehousePainter;
import com.watabou.utils.Random;

import java.util.ArrayList;

/**
 * Demo level that always generates warehouse rooms for screenshot purposes.
 */
public class WarehouseDemoLevel extends RegularLevel {

    @Override
    protected void assignRoomType() {
        // Force multiple warehouse rooms for demo purposes
        int warehouseCount = 0;
        int maxWarehouses = 5;

        for (Room r : rooms) {
            if (r.type == Room.Type.NULL && r.connected.size() == 1) {
                // Make qualifying rooms warehouses
                if (r.width() > 4 && r.height() > 4) {
                    if (warehouseCount < maxWarehouses) {
                        r.type = Room.Type.WAREHOUSE;
                        warehouseCount++;
                    }
                }
            }
        }

        // Assign remaining rooms normally
        super.assignRoomType();
    }

    @Override
    protected boolean[] water() {
        // Return empty array - no water for demo level
        return new boolean[getWidth() * getHeight()];
    }

    @Override
    protected boolean[] grass() {
        // Return empty array - no grass for demo level
        return new boolean[getWidth() * getHeight()];
    }

    @Override
    protected void decorate() {
        // No decoration for demo level - keep it clean for screenshots
    }

    @Override
    public String tilesTex() {
        return "tiles_sewers.png";
    }

    @Override
    public String waterTex() {
        return "water_sewers.png";
    }
}