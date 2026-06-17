
    package com.nyrds.pixeldungeon.levels;

    import java.util.HashMap;
    import java.util.Map;

    public class TerrainMap {
        private static final Map<String, Integer> TERRAIN_MAP = new HashMap<>();

        static {
    
            TERRAIN_MAP.put("CHASM", 0);
            TERRAIN_MAP.put("EMPTY", 1);
            TERRAIN_MAP.put("GRASS", 2);
            TERRAIN_MAP.put("EMPTY_WELL", 3);
            TERRAIN_MAP.put("WALL", 4);
            TERRAIN_MAP.put("DOOR", 5);
            TERRAIN_MAP.put("OPEN_DOOR", 6);
            TERRAIN_MAP.put("ENTRANCE", 7);
            TERRAIN_MAP.put("EXIT", 8);
            TERRAIN_MAP.put("EMBERS", 9);
            TERRAIN_MAP.put("LOCKED_DOOR", 10);
            TERRAIN_MAP.put("PEDESTAL", 11);
            TERRAIN_MAP.put("WALL_DECO", 12);
            TERRAIN_MAP.put("BARRICADE", 13);
            TERRAIN_MAP.put("EMPTY_SP", 14);
            TERRAIN_MAP.put("HIGH_GRASS", 15);
            TERRAIN_MAP.put("EMPTY_DECO", 24);
            TERRAIN_MAP.put("LOCKED_EXIT", 25);
            TERRAIN_MAP.put("UNLOCKED_EXIT", 26);
            TERRAIN_MAP.put("SIGN", 29);
            TERRAIN_MAP.put("WELL", 34);
            TERRAIN_MAP.put("STATUE", 35);
            TERRAIN_MAP.put("STATUE_SP", 36);
            TERRAIN_MAP.put("BOOKSHELF", 41);
            TERRAIN_MAP.put("ALCHEMY", 42);
            TERRAIN_MAP.put("CHASM_FLOOR", 43);
            TERRAIN_MAP.put("CHASM_FLOOR_SP", 44);
            TERRAIN_MAP.put("CHASM_WALL", 45);
            TERRAIN_MAP.put("CHASM_WATER", 46);
            TERRAIN_MAP.put("SECRET_DOOR", 16);
            TERRAIN_MAP.put("TOXIC_TRAP", 17);
            TERRAIN_MAP.put("SECRET_TOXIC_TRAP", 18);
            TERRAIN_MAP.put("FIRE_TRAP", 19);
            TERRAIN_MAP.put("SECRET_FIRE_TRAP", 20);
            TERRAIN_MAP.put("PARALYTIC_TRAP", 21);
            TERRAIN_MAP.put("SECRET_PARALYTIC_TRAP", 22);
            TERRAIN_MAP.put("INACTIVE_TRAP", 23);
            TERRAIN_MAP.put("POISON_TRAP", 27);
            TERRAIN_MAP.put("SECRET_POISON_TRAP", 28);
            TERRAIN_MAP.put("ALARM_TRAP", 30);
            TERRAIN_MAP.put("SECRET_ALARM_TRAP", 31);
            TERRAIN_MAP.put("LIGHTNING_TRAP", 32);
            TERRAIN_MAP.put("SECRET_LIGHTNING_TRAP", 33);
            TERRAIN_MAP.put("GRIPPING_TRAP", 37);
            TERRAIN_MAP.put("SECRET_GRIPPING_TRAP", 38);
            TERRAIN_MAP.put("SUMMONING_TRAP", 39);
            TERRAIN_MAP.put("SECRET_SUMMONING_TRAP", 40);
            TERRAIN_MAP.put("WATER_TILES", 48);
            TERRAIN_MAP.put("WATER", 63);
        }

        public static int get(String name) {
            Integer val = TERRAIN_MAP.get(name);
            return val != null ? val : -1;
        }

        public static boolean contains(String name) {
            return TERRAIN_MAP.containsKey(name);
        }

        public static Iterable<Map.Entry<String, Integer>> entries() {
            return TERRAIN_MAP.entrySet();
        }
    }