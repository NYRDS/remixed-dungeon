package com.nyrds.pixeldungeon.levels.painters;

import com.nyrds.lua.LuaEngine;
import com.nyrds.pixeldungeon.levels.CustomLevel;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.levels.Room;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;

import java.util.HashMap;
import java.util.Map;

/**
 * A painter that delegates painting to Lua scripts
 * Supports different painters for different room types
 */
public class LuaPainter {

    private static final Map<String, LuaTable> luaPaintersCache = new HashMap<>();

    /**
     * Static method to be called by the room type enum
     */
    public static void paint(Level level, Room room) {
        // Determine the appropriate Lua script based on room type and level configuration
        String luaScript = getLuaScriptForRoomType(level, room.type);

        if (luaScript != null) {
            LuaTable luaPainter = getOrCreateLuaPainter(luaScript);
            paintRoomWithLuaPainter(luaPainter, level, room);
        } else {
            // Fallback to standard painting
            com.watabou.pixeldungeon.levels.painters.StandardPainter.paint(level, room);
        }
    }

    /**
     * Gets the Lua script name for a specific room type and level
     * This could be configured in the level's JSON description
     */
    private static String getLuaScriptForRoomType(Level level, Room.Type roomType) {
        if (level instanceof CustomLevel) {
            CustomLevel customLevel = (CustomLevel) level;

            // First, try to get a specific painter for this room type
            String roomTypeName = roomType.name().toLowerCase();
            String specificPainter = customLevel.getProperty("luaPainter." + roomTypeName, null);

            if (specificPainter != null && !specificPainter.isEmpty()) {
                return specificPainter;
            }

            // If no specific painter, try to get a general painter for the level
            String generalPainter = customLevel.getProperty("luaPainter", null);

            if (generalPainter != null && !generalPainter.isEmpty()) {
                return generalPainter;
            }
        }

        return null;
    }

    /**
     * Gets or creates a Lua painter from cache
     */
    private static LuaTable getOrCreateLuaPainter(String luaScriptName) {
        LuaTable cachedPainter = luaPaintersCache.get(luaScriptName);

        if (cachedPainter == null) {
            cachedPainter = LuaEngine.require(luaScriptName);
            luaPaintersCache.put(luaScriptName, cachedPainter);
        }

        return cachedPainter;
    }

    /**
     * Actual painting method that calls the Lua script
     */
    private static void paintRoomWithLuaPainter(LuaTable luaPainter, Level level, Room room) {
        if (luaPainter != null) {
            LuaValue paintMethod = luaPainter.get("paint");

            if (!paintMethod.isnil()) {
                // Call the Lua paint function with level and room data
                // We need to convert the Room object to a format Lua can understand
                LuaValue luaLevel = convertLevelToLua(level);
                LuaValue luaRoom = convertRoomToLua(room);
                LuaValue luaRoomType = LuaValue.valueOf(room.type.name());

                paintMethod.call(luaLevel, luaRoom, luaRoomType);
            } else {
                // If no paint method exists, fall back to standard painting
                com.watabou.pixeldungeon.levels.painters.StandardPainter.paint(level, room);
            }
        } else {
            com.watabou.pixeldungeon.levels.painters.StandardPainter.paint(level, room);
        }
    }

    /**
     * Converts a Level object to a Lua table representation
     */
    private static LuaValue convertLevelToLua(Level level) {
        LuaTable luaLevel = new LuaTable();

        // Add basic level properties
        luaLevel.set("width", level.getWidth());
        luaLevel.set("height", level.getHeight());
        luaLevel.set("map", convertMapToLua(level));

        // Add helper methods for Lua scripts to interact with the level
        // These will be implemented in the Lua script itself using the RPD library

        return luaLevel;
    }

    /**
     * Converts a Room object to a Lua table representation
     */
    private static LuaValue convertRoomToLua(Room room) {
        LuaTable luaRoom = new LuaTable();

        luaRoom.set("left", room.left);
        luaRoom.set("right", room.right);
        luaRoom.set("top", room.top);
        luaRoom.set("bottom", room.bottom);
        luaRoom.set("width", room.width());
        luaRoom.set("height", room.height());
        luaRoom.set("type", room.type.name());

        return luaRoom;
    }

    /**
     * Converts the level's map to a Lua table
     */
    private static LuaValue convertMapToLua(Level level) {
        LuaTable luaMap = new LuaTable();

        for (int i = 0; i < level.map.length; i++) {
            luaMap.set(i + 1, LuaValue.valueOf(level.map[i])); // Lua arrays are 1-indexed
        }

        return luaMap;
    }
}