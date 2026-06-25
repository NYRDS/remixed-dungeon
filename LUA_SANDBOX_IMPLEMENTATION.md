# Lua Sandbox Implementation

## Overview
Implemented a runtime sandbox system that uses the compile-time `@LuaInterface` annotation map to warn when mods attempt to access Java classes/members not explicitly exposed via `@LuaInterface`.

**No external JSON library dependency** — uses custom JSON writer/parser in standard Java.

## Files Created/Modified

### New Files
- `RemixedDungeon/src/main/java/com/nyrds/lua/LuaSandbox.java` - Core sandbox logic

### Modified Files
- `RemixedDungeon/src/android/java/com/nyrds/platform/lua/PlatformLuajavaLib.java`
- `RemixedDungeonDesktop/src/libgdx/java/com/nyrds/platform/lua/PlatformLuajavaLib.java`
- `RemixedDungeonHtml/src/html/java/com/nyrds/platform/lua/PlatformLuajavaLib.java`
- `processor/src/main/java/com/nyrds/LuaInterfaceProcessor.java` - Custom JSON writer (no GSON)
- `RemixedDungeon/src/main/java/com/nyrds/lua/LuaSandbox.java` - Custom JSON parser (no GSON)
- ~20 core classes annotated with `@LuaInterface` at class level

## Architecture

### LuaSandbox
```java
public class LuaSandbox {
    static {
        initialize(); // Loads lua-interface-map.json from classpath
    }

    public static boolean canAccessClass(String className)
    public static boolean canAccessMethod(String className, String methodName)
    public static boolean canAccessField(String className, String fieldName)
    public static boolean canAccessConstructor(String className)
    public static void warnIfNotAllowed(String className, String member, String memberType)
}
```

### PlatformLuajavaLib Integration
Each platform's `PlatformLuajavaLib` overrides `classForName()`:
```java
@Override
protected Class<?> classForName(String name) {
    String actualClassName = classRemap.getOrDefault(name, name);
    LuaSandbox.warnIfNotAllowed(actualClassName, "<class>", "class");
    // ... load class
}
```

## Class-Level @LuaInterface Annotations Added

| Class | Package |
|-------|---------|
| Actor | com.watabou.pixeldungeon.actors |
| Char | com.watabou.pixeldungeon.actors |
| Hero | com.watabou.pixeldungeon.actors.hero |
| Mob | com.watabou.pixeldungeon.actors.mobs |
| Belongings | com.watabou.pixeldungeon.actors.hero |
| Buff | com.watabou.pixeldungeon.actors.buffs |
| Item | com.watabou.pixeldungeon.items |
| Level | com.watabou.pixeldungeon.levels |
| Dungeon | com.watabou.pixeldungeon |
| GameScene | com.watabou.pixeldungeon.scenes |
| GameLoop | com.nyrds.pixeldungeon.game |
| DungeonTilemap | com.watabou.pixeldungeon |
| Ballistica | com.watabou.pixeldungeon.mechanics |
| AlchemyRecipes | com.nyrds.pixeldungeon.alchemy |
| Badges | com.watabou.pixeldungeon |
| ItemUtils | com.nyrds.pixeldungeon.items |
| Treasury | com.nyrds.pixeldungeon.items |
| CharUtils | com.watabou.pixeldungeon.actors |
| Blob | com.watabou.pixeldungeon.actors.blobs |
| Alchemy | com.watabou.pixeldungeon.actors.blobs |
| SpellFactory | com.nyrds.pixeldungeon.mechanics.spells |
| Spell | com.nyrds.pixeldungeon.mechanics.spells |
| PetInventoryManager | com.nyrds.pixeldungeon.mechanics |
| LevelObject | com.nyrds.pixeldungeon.levels.objects |
| Heap | com.watabou.pixeldungeon.items |
| BuffIndicator | com.watabou.pixeldungeon.ui |
| Input | com.nyrds.platform.app (Android, Desktop) |
| BitmapData | com.nyrds.platform.gfx (Android, Desktop) |
| RemixedDungeon | com.nyrds.platform.game (Android, Desktop, HTML) |
| ModdingBase | com.nyrds.util |
| ModdingMode | com.nyrds.util (Android) |

## Build Verification
All three builds pass:
- Core: `./gradlew compileJava` - 62 classes in map
- Desktop: `./gradlew --settings-file settings.desktop.gradle :RemixedDungeonDesktop:build` ✅
- Android: `./gradlew --settings-file settings.android.gradle :RemixedDungeon:assembleAndroidFdroidDebug` ✅ - 65 classes

## Warning Output Example
```
INFO: LuaSandbox initialized with 59 classes
WARNING: LuaSandbox: Mod attempted to access unregistered class 'com.watabou.pixeldungeon.utils.GLog' (not annotated with @LuaInterface)
WARNING: LuaSandbox: Mod attempted to access class '<class>' on class 'com.nyrds.platform.game.RemixedDungeon' which is not exposed via @LuaInterface
```

## Design Decisions
1. **Warning-only, not blocking** - Maintains backwards compatibility; existing mods continue to work
2. **Class-level annotation required** - Only classes with `@LuaInterface` on the class itself are in the map
3. **Transitive closure** - Annotation processor includes classes referenced by annotated methods/fields
4. **Member-level checks** - Future: can extend to warn on specific method/field access
5. **No GSON dependency** - Custom JSON writer in annotation processor and custom JSON parser in runtime sandbox, both using only standard Java library