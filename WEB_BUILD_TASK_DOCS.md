# Web Build Task Documentation - Updated State

## Task
Fix TeaVM web build for Remixed Pixel Dungeon - resolve "Method not found" errors for WasmGC and JS targets.

## Current Branch
- **Branch**: teavm-classlib-stubs
- **HEAD**: c31e59e39 (Fix TeaVM web build dependency propagation)
- **Git bisect first bad commit**: 3308a4b4e (gitblobstore: checkandput manifest)

## Build Status Summary

| Target | TeaVM Version | Status | Notes |
|--------|---------------|--------|-------|
| Desktop | N/A | ✅ SUCCESS | `./gradlew --settings-file settings.desktop.gradle :RemixedDungeonDesktop:build` |
| Android | N/A | ✅ SUCCESS | `./gradlew --settings-file settings.android.gradle :RemixedDungeon:assembleAndroidFdroidDebug` |
| Web JS | 0.16.0-dev-1 | ❌ FAILED | NPE in ClassInfoGenerator.writeSimpleConstructors (LuaJ reflection) |
| Web WasmGC | 0.16.0-dev-1 | ❌ FAILED | Internal error: Unsupported generic type: null |

## All "Method not found" Errors RESOLVED ✅

| Missing Method | Called From | Fix Applied |
|----------------|-------------|-------------|
| `Field.getInt(Object)` | Utils.getClassParam/Params → Item/DummyItem/ItemsList → Dungeon.reset | **ResourceMap.java** generated at codegen time with name→value map |
| `Field.getInt(Object)` | XTilemapConfiguration.createTerrainMapping → Terrain fields | **TerrainMap.java** generated at codegen time with hardcoded terrain fields |
| `InetAddress.isLoopbackAddress()` | BaseWebServer → AboutScene | Added to **TInetAddress** in supplemental-classlib |
| `Proxy.newProxyInstance` | LuajavaLib → PlatformLuajavaLib → LuaEngine → Dungeon.reset | Added to **TProxy** + **InvocationHandler** in supplemental-classlib |
| `System.exit(int)` | OsLib.exit → LuaEngine → Dungeon.reset | Added to **TSystem** in supplemental-classlib |
| `Runtime.exec(String)` | JseProcess → JseOsLib → LuaEngine → Dungeon.reset | Added to **TRuntime** in supplemental-classlib |
| `System.arraycopy` | String/StringBuilder/ObjectMap → TeaApplication | Added to **TSystem** in supplemental-classlib |
| `System.setProperty` | TeaApplication.init | Added to **TSystem** (returns String) |
| `System.identityHashCode` | IdentityHashMap → JSONObject → HtmlPreferences | Added to **TSystem** |
| `System.getenv(String)` | JseOsLib.getenv → LuaEngine → Dungeon.reset | Added to **TSystem** |
| `System.getTempDir()` | File.createTempFile → JseOsLib.tmpname → LuaEngine | Added to **TSystem** |

## Codegen Infrastructure Created

### Modified Files
- `RemixedDungeonHtml/make_r.py` - Extended to generate:
  - `R.java` (existing)
  - `ResourceMap.java` - O(1) string/array resource lookup
  - `TerrainMap.java` - Pre-built terrain field name→value map

### Generated Classes (at build time)
- `com.nyrds.pixeldungeon.ml.ResourceMap` - Parallel arrays for ~3000 resources
- `com.nyrds.pixeldungeon.levels.TerrainMap` - HashMap for 58 terrain constants

### Application Code Changes
- `Utils.java` - Replaced reflection with `ResourceMap.get(name)`
- `XTilemapConfiguration.java` - Replaced reflection with `TerrainMap.entries()`

## Supplemental Classlib Additions

New classes in `supplemental-classlib/src/main/java/org/teavm/classlib/java/`:
- `lang/TSystem.java` - System methods (exit, arraycopy, setProperty, identityHashCode, getenv, getTempDir, etc.)
- `lang/TRuntime.java` - Runtime methods (exec, exit, memory, processors)
- `lang/reflect/TProxy.java` - Proxy.newProxyInstance, InvocationHandler support
- `lang/reflect/InvocationHandler.java` - Required interface
- `net/TInetAddress.java` - Added isLoopbackAddress()

## Remaining Blockers (TeaVM 0.16.0-dev-1 Bugs)

### JS Target
```
NullPointerException: Cannot invoke "org.teavm.model.ClassReader.getMethod(...)"
because "cls" is null
    at org.teavm.backend.javascript.intrinsics.reflection.ClassInfoGenerator.writeSimpleConstructors()
```
**Trigger**: LuaJ reflection usage when `org.teavm.reflect.enableRef=false`
**TeaVM Bug**: ClassInfoGenerator doesn't handle missing class for reflection metadata

### WasmGC Target
```
java.lang.IllegalArgumentException: Unsupported generic type: null
    at org.teavm.backend.wasm.intrinsics.reflection.ReflectionMetadataGenerator.generateGenericType()
```
**Trigger**: Reflection metadata generation for WasmGC
**TeaVM Bug**: Generic type resolution returns null for some classes

## Historical TeaVM Version Comparison

| TeaVM Version | JS Target | WasmGC Target | Java Version |
|---------------|-----------|---------------|--------------|
| 0.14.1 (stable) | NPE in ClassInfoGenerator | Builds but invalid WASM (stack underflow at offset 669770) | Java 11 |
| 0.13.1 | Missing JDK classes | Not tested | Java 11 |
| 0.15.0-dev-6 | Requires Java 17 | Requires Java 17 | Java 17+ |
| 0.16.0-dev-1 | NPE in ClassInfoGenerator | Unsupported generic type: null | Java 17+ |

## Files Modified in This Session

### Core Infrastructure
- `RemixedDungeonHtml/make_r.py` - Codegen script
- `RemixedDungeonHtml/build.gradle` - Already configured for codegen

### Application Code (reflection elimination)
- `RemixedDungeon/src/main/java/com/watabou/pixeldungeon/utils/Utils.java`
- `RemixedDungeon/src/main/java/com/nyrds/pixeldungeon/levels/XTilemapConfiguration.java`

### Supplemental Classlib (missing JDK/TeaVM API implementations)
- `supplemental-classlib/src/main/java/org/teavm/classlib/java/lang/TSystem.java` (NEW)
- `supplemental-classlib/src/main/java/org/teavm/classlib/java/lang/TRuntime.java` (NEW)
- `supplemental-classlib/src/main/java/org/teavm/classlib/java/lang/reflect/TProxy.java` (UPDATED)
- `supplemental-classlib/src/main/java/org/teavm/classlib/java/lang/reflect/InvocationHandler.java` (NEW)
- `supplemental-classlib/src/main/java/org/teavm/classlib/java/net/TInetAddress.java` (UPDATED)
- `supplemental-classlib/build.gradle` - Added teavm-classlib dependency

## Next Steps to Investigate

1. **Report TeaVM bugs** for both JS and WasmGC targets in 0.16.0-dev-1
2. **Test TeaVM 0.14.1 WasmGC** with our supplemental-classlib fixes (may resolve runtime stack underflow)
3. **Apply @NoReflection** to LuaJ/org.json classes as workaround for JS target
4. **Try TeaVM 0.13.1** as baseline (may have different missing-class issues)

## Build Commands

```bash
# Desktop (works)
./gradlew --settings-file settings.desktop.gradle :RemixedDungeonDesktop:build

# Android (works)
./gradlew --settings-file settings.android.gradle :RemixedDungeon:assembleAndroidFdroidDebug

# Web JS (fails - TeaVM bug)
./gradlew --settings-file settings.html.gradle :RemixedDungeonHtml:compileTeavmApp

# Web WasmGC (fails - TeaVM bug)
./gradlew --settings-file settings.html.gradle :RemixedDungeonHtml:generateWasmGC
```