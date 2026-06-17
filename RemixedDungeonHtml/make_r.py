#!/usr/bin/env python3
import os
from lxml import etree as ElementTree


def makeRJava(strings, arrays):
    target_dir = 'src/html/java/com/nyrds/pixeldungeon/ml'
    os.makedirs(target_dir, exist_ok=True)
    rJava = open(f"{target_dir}/R.java", "w", encoding='utf8')
    rJava.write('''\n    package com.nyrds.pixeldungeon.ml;\n\n    public class R {\n        public static class string { \n                ''')

    counter = 0

    for str in strings:
        rJava.write(f'''\n                public static final int {str} = {counter};''')
        counter += 1

    rJava.write('''}\n    \n    public static class array {\n    ''')

    for str in arrays:
        rJava.write(f'''\n                public static final int {str} = {counter};''')
        counter += 1


    rJava.write('''\n        }\n    }''')
    rJava.close()


def makeResourceMap(strings, arrays):
    target_dir = 'src/html/java/com/nyrds/pixeldungeon/ml'
    os.makedirs(target_dir, exist_ok=True)
    mapJava = open(f"{target_dir}/ResourceMap.java", "w", encoding='utf8')
    mapJava.write('''\n    package com.nyrds.pixeldungeon.ml;\n\n    public class ResourceMap {\n        private static final String[] NAMES = {\n    ''')

    all_names = list(strings) + list(arrays)
    for i, name in enumerate(all_names):
        mapJava.write(f'''\n            "{name}",''')

    mapJava.write('''\n        };\n\n        private static final int[] VALUES = {\n    ''')

    for i in range(len(all_names)):
        mapJava.write(f'''\n            {i},''')

    mapJava.write('''\n        };\n\n        public static int get(String name) {\n            for (int i = 0; i < NAMES.length; i++) {\n                if (NAMES[i].equals(name)) {\n                    return VALUES[i];\n                }\n            }\n            return -1;\n        }\n    }''')
    mapJava.close()


def makeTerrainMap():
    target_dir = 'src/html/java/com/nyrds/pixeldungeon/levels'
    os.makedirs(target_dir, exist_ok=True)
    mapJava = open(f"{target_dir}/TerrainMap.java", "w", encoding='utf8')
    mapJava.write('''\n    package com.nyrds.pixeldungeon.levels;\n\n    import java.util.HashMap;\n    import java.util.Map;\n\n    public class TerrainMap {\n        private static final Map<String, Integer> TERRAIN_MAP = new HashMap<>();\n\n        static {\n    ''')

    # Hardcoded terrain fields from Terrain.java
    terrain_fields = {
        "CHASM": 0,
        "EMPTY": 1,
        "GRASS": 2,
        "EMPTY_WELL": 3,
        "WALL": 4,
        "DOOR": 5,
        "OPEN_DOOR": 6,
        "ENTRANCE": 7,
        "EXIT": 8,
        "EMBERS": 9,
        "LOCKED_DOOR": 10,
        "PEDESTAL": 11,
        "WALL_DECO": 12,
        "BARRICADE": 13,
        "EMPTY_SP": 14,
        "HIGH_GRASS": 15,
        "EMPTY_DECO": 24,
        "LOCKED_EXIT": 25,
        "UNLOCKED_EXIT": 26,
        "SIGN": 29,
        "WELL": 34,
        "STATUE": 35,
        "STATUE_SP": 36,
        "BOOKSHELF": 41,
        "ALCHEMY": 42,
        "CHASM_FLOOR": 43,
        "CHASM_FLOOR_SP": 44,
        "CHASM_WALL": 45,
        "CHASM_WATER": 46,
        "SECRET_DOOR": 16,
        "TOXIC_TRAP": 17,
        "SECRET_TOXIC_TRAP": 18,
        "FIRE_TRAP": 19,
        "SECRET_FIRE_TRAP": 20,
        "PARALYTIC_TRAP": 21,
        "SECRET_PARALYTIC_TRAP": 22,
        "INACTIVE_TRAP": 23,
        "POISON_TRAP": 27,
        "SECRET_POISON_TRAP": 28,
        "ALARM_TRAP": 30,
        "SECRET_ALARM_TRAP": 31,
        "LIGHTNING_TRAP": 32,
        "SECRET_LIGHTNING_TRAP": 33,
        "GRIPPING_TRAP": 37,
        "SECRET_GRIPPING_TRAP": 38,
        "SUMMONING_TRAP": 39,
        "SECRET_SUMMONING_TRAP": 40,
        "WATER_TILES": 48,
        "WATER": 63,
    }

    for name, value in terrain_fields.items():
        mapJava.write(f'''\n            TERRAIN_MAP.put("{name}", {value});''')

    mapJava.write('''\n        }\n\n        public static int get(String name) {\n            Integer val = TERRAIN_MAP.get(name);\n            return val != null ? val : -1;\n        }\n\n        public static boolean contains(String name) {\n            return TERRAIN_MAP.containsKey(name);\n        }\n\n        public static Iterable<Map.Entry<String, Integer>> entries() {\n            return TERRAIN_MAP.entrySet();\n        }\n    }''')
    mapJava.close()


r_strings = set()
r_arrays = set()

d_strings = {}
d_arrays = {}

locales = []

strings_files = ['../RemixedDungeon/src/main/res/values/strings_not_translate.xml',
                 '../RemixedDungeon/src/main/res/values/strings_api_signature.xml',
                 '../RemixedDungeon/src/main/res/values/string_arrays.xml',
                 '../RemixedDungeon/src/main/res/values/strings_all.xml']

for file in strings_files:
    pfile = ElementTree.parse(file).getroot()

    for entry in pfile:
        if entry.tag not in ["string", "string-array"]:
            continue

        entry_name = entry.get("name")
        if entry.tag == 'string':
            r_strings.add(entry_name)

        if entry.tag == 'string-array':
            d_arrays[entry_name] = []
            for e in entry:
                d_arrays[entry_name].append(e.text.replace("@string/", ""))

            r_arrays.add(entry_name)
print("Making R.java")
makeRJava(r_strings, r_arrays)
print("Making ResourceMap.java")
makeResourceMap(r_strings, r_arrays)
print("Making TerrainMap.java")
makeTerrainMap()
print("Done")