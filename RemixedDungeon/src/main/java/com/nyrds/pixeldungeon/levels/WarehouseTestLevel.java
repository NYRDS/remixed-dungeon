package com.nyrds.pixeldungeon.levels;

import com.watabou.pixeldungeon.levels.Room;
import com.watabou.pixeldungeon.levels.SewerLevel;
import com.watabou.pixeldungeon.levels.painters.WarehousePainter;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Test level that forces warehouse rooms to be generated for screenshot purposes.
 */
public class WarehouseTestLevel extends SewerLevel {

    @Override
    protected void assignRoomType() {
        // Force multiple warehouse rooms for screenshot testing
        int warehouseCount = 0;
        int maxWarehouses = 3; // Generate up to 3 warehouse rooms

        for (Room r : rooms) {
            if (r.type == Room.Type.NULL && r.connected.size() == 1) {
                if (r.width() > 4 && r.height() > 4 && warehouseCount < maxWarehouses) {
                    if (Random.Int(3) == 0) { // 1 in 3 chance for qualifying rooms to be warehouse
                        r.type = Room.Type.WAREHOUSE;
                        warehouseCount++;
                    }
                }
            }
        }

        // Call parent to assign remaining room types normally
        super.assignRoomType();
    }
}