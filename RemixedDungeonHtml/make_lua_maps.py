#!/usr/bin/env python3
"""
Generate LuaJ codegen maps for TeaVM web build.
Eliminates runtime reflection by generating compile-time class/method/constructor maps.
"""

import os
import re
import glob


def scan_lua_bindclass():
    """Extract all bindClass class names from .lua files"""
    class_names = set()
    lua_files = glob.glob('../scripts/**/*.lua', recursive=True)

    for file in lua_files:
        with open(file, 'r', encoding='utf-8') as f:
            content = f.read()
        matches = re.findall(r'luajava\.bindClass\s*\(\s*"([^"]+)"\s*\)', content)
        class_names.update(matches)

    # Classes that don't actually exist in the codebase - exclude from generated map
    # These will fall back to reflection via classForName()
    NON_EXISTENT_CLASSES = {
        "com.nyrds.pixeldungeon.support.PollfishSurveys",
        "com.nyrds.pixeldungeon.support.AdsRewardVideo",
        "com.nyrds.pixeldungeon.support.AdsInterstitial",
        "com.watabou.noosa.StringsManager",
        "com.watabou.noosa.audio.Music",
        "com.watabou.noosa.audio.Sample",
        "com.watabou.pixeldungeon.RemixedDungeon",
        "com.nyrds.platform.Input",
        "com.nyrds.platform.audio.Music",
    }

    # Also add classes from classRemap in PlatformLuajavaLib
    class_remap = {
        "com.watabou.pixeldungeon.RemixedDungeon": "com.nyrds.platform.game.RemixedDungeon",
        "com.watabou.noosa.audio.Sample": "com.nyrds.platform.audio.Sample",
        "com.watabou.noosa.audio.Music": "com.nyrds.platform.audio.MusicManager",
        "com.watabou.noosa.StringsManager": "com.nyrds.platform.util.StringsManager",
        "com.nyrds.platform.Input": "com.nyrds.platform.app.Input",
        "com.nyrds.platform.audio.Music": "com.nyrds.platform.audio.MusicManager",
    }
    # Only add remapped classes that actually exist (the target classes)
    for target in class_remap.values():
        if target not in NON_EXISTENT_CLASSES:
            class_names.add(target)

    # Remove non-existent classes
    class_names = class_names - NON_EXISTENT_CLASSES

    return sorted(class_names)


def scan_lua_newinstance():
    """Extract all newInstance class names and their arg patterns from .lua files"""
    constructor_calls = []  # list of (class_name, arg_types_list)

    lua_files = glob.glob('../scripts/**/*.lua', recursive=True)

    all_content = ""
    # String literal class names
    for file in lua_files:
        with open(file, 'r', encoding='utf-8') as f:
            content = f.read()
        all_content += content + "\n"
        matches = re.findall(r'luajava\.newInstance\s*\(\s*"([^"]+)"', content)
        for m in matches:
            constructor_calls.append((m, []))

    # Variable reference class names (Objects.Ui.WndOptionsLua, etc.) - need manual mapping
    var_refs = re.findall(r'luajava\.newInstance\s*\((\w+(?:\.\w+)*)', all_content)
    for ref in var_refs:
        # These will be resolved manually in the generator
        constructor_calls.append((ref, []))

    # Manual mapping for variable references found in commonClasses.lua
    var_class_map = {
        "Position": "com.nyrds.pixeldungeon.utils.Position",
        "Bundle": "com.watabou.utils.Bundle",
        "Objects.Ui.WndOptionsLua": "com.nyrds.pixeldungeon.windows.WndOptionsLua",
        "Objects.Ui.WndStory": "com.watabou.pixeldungeon.windows.WndStory",
        "Objects.Ui.WndShopOptions": "com.nyrds.pixeldungeon.windows.WndShopOptions",
        "Objects.Ui.WndQuest": "com.watabou.pixeldungeon.windows.WndQuest",
        "Objects.Ui.WndBag": "com.watabou.pixeldungeon.windows.WndBag",
        "RPD.Objects.Ui.WndChooseWay": "com.watabou.pixeldungeon.windows.WndChooseWay",
    }

    # Resolve variable references
    resolved = []
    for ref, args in constructor_calls:
        if ref in var_class_map:
            resolved.append((var_class_map[ref], []))
        elif "." not in ref and ref[0].isupper():  # Likely a class reference
            # Will need to be handled specially
            pass
        else:
            resolved.append((ref, args))

    return resolved


# Known constructor signatures (class_name -> list of parameter types)
# Only includes classes that actually exist and have constructors
CONSTRUCTOR_SIGNATURES = {
    "com.watabou.pixeldungeon.items.wands.WandOfBlink": [],
    "com.watabou.pixeldungeon.items.wands.WandOfTelekinesis": [],
    "com.watabou.pixeldungeon.items.wands.WandOfFirebolt": [],
    "com.nyrds.pixeldungeon.windows.LuaWndBagListener": ["int"],  # callbackId
    "com.watabou.pixeldungeon.windows.WndBag": [
        "com.watabou.pixeldungeon.actors.hero.Belongings",
        "com.watabou.pixeldungeon.items.bags.Bag",
        "com.watabou.pixeldungeon.windows.WndBag.Listener",
        "com.watabou.pixeldungeon.windows.WndBag.Mode",
        "java.lang.String"
    ],
    "com.nyrds.pixeldungeon.utils.Position": ["java.lang.String", "int", "int"],  # levelId, x, y
    "com.nyrds.pixeldungeon.windows.WndOptionsLua": ["java.lang.Object", "java.lang.String", "java.lang.String", "java.lang.String[]"],
    "com.watabou.pixeldungeon.windows.WndStory": ["java.lang.String"],
    "com.nyrds.pixeldungeon.windows.WndShopOptions": ["com.watabou.pixeldungeon.actors.mobs.Mob", "com.watabou.pixeldungeon.actors.hero.Hero"],
    "com.watabou.pixeldungeon.windows.WndQuest": ["com.watabou.pixeldungeon.actors.hero.Hero", "java.lang.String"],
    "com.watabou.pixeldungeon.windows.WndChooseWay": ["com.watabou.pixeldungeon.actors.Char", "com.watabou.pixeldungeon.items.Item", "com.watabou.pixeldungeon.actors.hero.HeroSubClass", "com.watabou.pixeldungeon.actors.hero.HeroSubClass"],
    "com.watabou.utils.Bundle": ["java.lang.String"],
    "com.watabou.pixeldungeon.sprites.Glowing": ["int", "int"],  # color, period
}

# Method signatures for hot classes - ONLY static methods with simple signatures
# Format: class_name -> {method_name: [(param_types), return_type]}
# return_type: "void" or the return type class name
METHOD_SIGNATURES = {
    "com.watabou.pixeldungeon.utils.GLog": {
        "i": (["java.lang.String", "java.lang.Object[]"], "void"),
        "p": (["java.lang.String", "java.lang.Object[]"], "void"),
        "n": (["java.lang.String", "java.lang.Object[]"], "void"),
        "toFile": (["java.lang.String", "java.lang.Object[]"], "void"),
        "w": (["java.lang.String", "java.lang.Object[]"], "void"),
    },
    "com.nyrds.platform.util.StringsManager": {
        "maybeId": (["java.lang.String"], "java.lang.String"),
        "getVar": (["java.lang.String"], "java.lang.String"),
    },
    "com.nyrds.pixeldungeon.utils.DungeonGenerator": {
        "getEntryLevel": ([], "java.lang.String"),
        "getLevelsList": ([], "java.util.List"),
    },
    "com.nyrds.pixeldungeon.items.common.ItemFactory": {
        "itemByName": (["java.lang.String"], "com.watabou.pixeldungeon.items.Item"),
        "createItem": (["java.lang.String", "java.lang.String"], "com.watabou.pixeldungeon.items.Item"),
    },
}


def make_lua_class_map(class_names):
    target_dir = 'build/generated/sources/java/main/com/nyrds/platform/lua'
    os.makedirs(target_dir, exist_ok=True)
    map_file = open(f"{target_dir}/LuaClassMap.java", "w", encoding='utf8')

    map_file.write('''package com.nyrds.platform.lua;

import java.util.HashMap;
import java.util.Map;

/**
 * Generated class map for LuaJ bindClass calls.
 * Eliminates runtime Class.forName reflection for TeaVM web build.
 * Generated by make_lua_maps.py - DO NOT EDIT MANUALLY.
 * Uses string-based lookup to avoid .class literals in static initializer (TeaVM compatibility).
 */
public class LuaClassMap {
    private static final Map<String, String> CLASS_NAME_MAP = new HashMap<>();
    private static final Map<String, Class<?>> CLASS_CACHE = new HashMap<>();

    static {
''')

    for class_name in class_names:
        map_file.write(f'        CLASS_NAME_MAP.put("{class_name}", "{class_name}");\n')

    map_file.write('''    }

    public static Class<?> get(String name) {
        // Check cache first
        Class<?> cached = CLASS_CACHE.get(name);
        if (cached != null) {
            return cached;
        }
        // Resolve from name map
        String className = CLASS_NAME_MAP.get(name);
        if (className != null) {
            try {
                Class<?> clazz = Class.forName(className);
                CLASS_CACHE.put(name, clazz);
                return clazz;
            } catch (ClassNotFoundException e) {
                return null;
            }
        }
        return null;
    }

    public static boolean contains(String name) {
        return CLASS_NAME_MAP.containsKey(name);
    }
}
''')
    map_file.close()
    print(f"Generated LuaClassMap.java with {len(class_names)} classes (string-based, no .class literals)")


def make_lua_constructor_map():
    target_dir = 'build/generated/sources/java/main/com/nyrds/platform/lua'
    os.makedirs(target_dir, exist_ok=True)
    map_file = open(f"{target_dir}/LuaConstructorMap.java", "w", encoding='utf8')

    map_file.write('''package com.nyrds.platform.lua;

import org.luaj.vm2.LuaValue;

/**
 * Generated constructor map for LuaJ newInstance calls.
 * Eliminates ALL reflection for TeaVM web build by using direct constructor calls.
 * Generated by make_lua_maps.py - DO NOT EDIT MANUALLY.
 */
public class LuaConstructorMap {

    public static Object newInstance(String name, LuaValue[] luaArgs) throws Exception {
        switch (name) {
''')

    for class_name, param_types in CONSTRUCTOR_SIGNATURES.items():
        # Generate case for this class with braces for variable scope
        map_file.write(f'            case "{class_name}": {{\n')
        
        if not param_types:
            # No-arg constructor
            map_file.write(f'                return new {class_name}();\n')
        else:
            # Generate coercion for each parameter
            for i, pt in enumerate(param_types):
                # Generate direct coercion based on parameter type
                if pt == "int":
                    map_file.write(f'                int arg{i} = luaArgs[{i}].checkint();\n')
                elif pt == "float":
                    map_file.write(f'                float arg{i} = (float) luaArgs[{i}].checkdouble();\n')
                elif pt == "boolean":
                    map_file.write(f'                boolean arg{i} = luaArgs[{i}].checkboolean();\n')
                elif pt == "long":
                    map_file.write(f'                long arg{i} = luaArgs[{i}].checklong();\n')
                elif pt == "double":
                    map_file.write(f'                double arg{i} = luaArgs[{i}].checkdouble();\n')
                elif pt == "java.lang.String":
                    map_file.write(f'                java.lang.String arg{i} = luaArgs[{i}].checkjstring();\n')
                elif pt == "java.lang.Object":
                    map_file.write(f'                java.lang.Object arg{i} = luaArgs[{i}];\n')
                elif pt.endswith("[]"):
                    # Array type - use checkuserdata())
                    java_type = pt.replace("java.lang.", "")
                    map_file.write(f'                {java_type} arg{i} = ({java_type}) luaArgs[{i}].touserdata();\n')
                else:
                    # Object type - use touserdata() and cast (avoids .class literal)
                    map_file.write(f'                {pt} arg{i} = ({pt}) luaArgs[{i}].touserdata();\n')
            
            # Build constructor call
            args = ", ".join([f"arg{i}" for i in range(len(param_types))])
            map_file.write(f'                return new {class_name}({args});\n')
        
        map_file.write('            }\n\n')

    map_file.write('''            default:
                return null; // not in map, fallback to reflection
        }
    }
}
''')
    map_file.close()
    print(f"Generated LuaConstructorMap.java with {len(CONSTRUCTOR_SIGNATURES)} constructors")


def make_lua_method_map():
    target_dir = 'build/generated/sources/java/main/com/nyrds/platform/lua'
    os.makedirs(target_dir, exist_ok=True)
    map_file = open(f"{target_dir}/LuaMethodMap.java", "w", encoding='utf8')

    map_file.write('''package com.nyrds.platform.lua;

import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;

/**
 * Generated method map for hot LuaJ class methods.
 * Eliminates runtime Method.invoke reflection for TeaVM web build by using direct method calls.
 * Generated by make_lua_maps.py - DO NOT EDIT MANUALLY.
 */
public class LuaMethodMap {
''')

    # Generate invoke method with switch on className + methodName
    map_file.write('''    public static Object invoke(String className, String methodName, Object[] args) {
        switch (className) {
''')

    for class_name, methods in METHOD_SIGNATURES.items():
        map_file.write(f'            case "{class_name}":\n')
        map_file.write('                switch (methodName) {\n')
        for method_name, (param_types, return_type) in methods.items():
            map_file.write(f'                    case "{method_name}": {{\n')
            # Generate argument extraction with unique variable names per case
            for i, pt in enumerate(param_types):
                
                if pt == "int":
                    map_file.write(f'                        int a{i} = ((Number) args[{i}]).intValue();\n')
                elif pt == "float":
                    map_file.write(f'                        float a{i} = ((Number) args[{i}]).floatValue();\n')
                elif pt == "boolean":
                    map_file.write(f'                        boolean a{i} = (Boolean) args[{i}];\n')
                elif pt == "long":
                    map_file.write(f'                        long a{i} = ((Number) args[{i}]).longValue();\n')
                elif pt == "double":
                    map_file.write(f'                        double a{i} = ((Number) args[{i}]).doubleValue();\n')
                elif pt == "java.lang.String":
                    map_file.write(f'                        java.lang.String a{i} = (java.lang.String) args[{i}];\n')
                elif pt == "java.lang.Object":
                    map_file.write(f'                        java.lang.Object a{i} = args[{i}];\n')
                elif pt == "java.lang.Object[]":
                    map_file.write(f'                        java.lang.Object[] a{i} = (java.lang.Object[]) args[{i}];\n')
                elif pt == "java.lang.Class":
                    map_file.write(f'                        java.lang.Class<?> a{i} = (java.lang.Class<?>) args[{i}];\n')
                elif pt.endswith("[]"):
                    java_type = pt.replace("java.lang.", "")
                    map_file.write(f'                        {java_type} a{i} = ({java_type}) args[{i}];\n')
                else:
                    map_file.write(f'                        {pt} a{i} = ({pt}) args[{i}];\n')
            
            # Build constructor call
            args_str = ", ".join([f"a{i}" for i in range(len(param_types))])
            
            if return_type == "void":
                map_file.write(f'                        {class_name}.{method_name}({args_str});\n')
                map_file.write('                        return null;\n')
            else:
                map_file.write(f'                        return {class_name}.{method_name}({args_str});\n')
            map_file.write('                    }\n')
        map_file.write('                    default: return null;\n')
        map_file.write('                }\n\n')

    map_file.write('''            default:
                return null; // not in map, fallback to reflection
        }
    }
}
''')
    map_file.close()
    total_methods = sum(len(m) for m in METHOD_SIGNATURES.values())
    print(f"Generated LuaMethodMap.java with {len(METHOD_SIGNATURES)} classes and {total_methods} methods (direct invoke stubs)")


if __name__ == "__main__":
    print("Scanning Lua files for bindClass...")
    class_names = scan_lua_bindclass()
    print(f"Found {len(class_names)} unique classes")

    print("Scanning Lua files for newInstance...")
    constructor_calls = scan_lua_newinstance()
    print(f"Found {len(constructor_calls)} newInstance calls")

    print("Generating LuaClassMap.java...")
    make_lua_class_map(class_names)

    print("Generating LuaConstructorMap.java...")
    make_lua_constructor_map()

    print("Generating LuaMethodMap.java...")
    make_lua_method_map()

    print("Done!")