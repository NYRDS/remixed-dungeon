# TeaVM Local Switch Documentation

## Summary

Switched the web build (RemixedDungeonHtml) to use **locally built TeaVM** from the `teavm` submodule (on `remixed-patches` branch) and **locally built Luaj** from the `luaj` submodule, both published to `mavenLocal`.

This resolved the TeaVM 0.14.1 / 0.16.0-dev-1 `ClassInfoGenerator` NPE bug that was blocking the web build.

---

## Changes Made

### 1. Teavm Submodule
- **Before**: `master` branch at commit `4a132d8e6` (0.14.0-SNAPSHOT)
- **After**: `remixed-patches` branch at commit `52f17f621` (0.13.0-SNAPSHOT)
- The `remixed-patches` branch contains classlib stubs for missing JDK classes:
  - `AtomicIntegerArray`, `AtomicLongArray`
  - `CountDownLatch`, `Future`
  - `Proxy`, `InvocationHandler`
  - `InetAddress`, `Process`, `SocketException`
  - `System.exit`, `Runtime.exec`, `System.arraycopy`, `System.lineSeparator`

### 2. Luaj Submodule
- Built `luaj-jse-3.0.2.jar` from source using `ant jar-jse`
- Installed to mavenLocal: `org.luaj:luaj-jse:3.0.2`

### 3. Published Artifacts to mavenLocal
```bash
# Teavm modules (from remixed-patches branch)
./gradlew :classlib:publishToMavenLocal
./gradlew :core:publishToMavenLocal
./gradlew :interop:core:publishToMavenLocal
./gradlew :jso:core:publishToMavenLocal
./gradlew :jso:apis:publishToMavenLocal
./gradlew :jso:impl:publishToMavenLocal
./gradlew :platform:publishToMavenLocal
./gradlew :tools:gradle:publishToMavenLocal
./gradlew :metaprogramming:api:publishToMavenLocal
./gradlew :metaprogramming:impl:publishToMavenLocal

# Luaj
ant -f build.xml jar-jse
mvn install:install-file -Dfile=luaj-jse-3.0.2.jar -DgroupId=org.luaj -DartifactId=luaj-jse -Dversion=3.0.2 -Dpackaging=jar
```

### 4. Build.gradle Updates

#### RemixedDungeonHtml/build.gradle
- Changed `teaVmVersion` from `0.14.0-SNAPSHOT` → `0.13.0-SNAPSHOT`
- Changed `teavm-gradle-plugin` from `0.14.0-SNAPSHOT` → `0.13.0-SNAPSHOT`
- Removed `flatDir` teavm-libs repository
- Removed `force` resolution strategy for all teavm modules
- Changed luaj dependency from `files('build/patched-libs/luaj-jse-3.0.2.jar')` → `org.luaj:luaj-jse:3.0.2`
- Removed `patchLuajJar` task (no longer needed - using unpatched luaj from mavenLocal)

#### supplemental-classlib/build.gradle
- Changed teavm dependencies from `0.14.1` → `0.13.0-SNAPSHOT`
- Removed `@Intrinsified` annotations (not available in TeaVM 0.13.0)
- Added `System.lineSeparator()` to `TSystem.java`

### 5. Generated Sources Copied
Copied codegen-generated files to desktop/Android source trees:
- `RemixedDungeonHtml/src/html/java/com/nyrds/pixeldungeon/ml/ResourceMap.java` → `RemixedDungeon/src/main/java/com/nyrds/pixeldungeon/ml/` and `RemixedDungeonDesktop/src/generated/java/com/nyrds/pixeldungeon/ml/`
- `RemixedDungeonHtml/src/html/java/com/nyrds/pixeldungeon/levels/TerrainMap.java` → `RemixedDungeon/src/main/java/com/nyrds/pixeldungeon/levels/` and `RemixedDungeonDesktop/src/generated/java/com/nyrds/pixeldungeon/levels/`

---

## Why This Works

### The TeaVM Bug (0.14.1 / 0.16.0-dev-1)
```
NullPointerException: Cannot invoke "org.teavm.model.ClassReader.getMethod(...)" because "cls" is null
    at org.teavm.backend.javascript.intrinsics.reflection.ClassInfoGenerator.writeSimpleConstructors()
```

This is a **TeaVM internal bug** triggered when TeaVM processes ANY class with reflection metadata. Our generated LuaJ maps were reflection-free, but luaj-jse internals (and other deps) still had reflection, causing TeaVM to crash.

### Why 0.13.0-SNAPSHOT (remixed-patches) Works
- The `remixed-patches` branch is based on TeaVM 0.13.x which **does not have this bug**
- It includes all the missing JDK classlib stubs we need
- Combined with our reflection-free LuaJ codegen maps, both JS and WasmGC targets compile successfully

---

## Build Verification

All 4 build targets pass:

| Target | Command | Output |
|--------|---------|--------|
| **Web JS** | `./gradlew --settings-file settings.html.gradle :RemixedDungeonHtml:generateJavaScript` | ✅ `teavm-app.js` (7.3 MB) |
| **Web WasmGC** | `./gradlew --settings-file settings.html.gradle :RemixedDungeonHtml:generateWasmGC` | ✅ `teavm-app.wasm` (14.3 MB) |
| **Desktop** | `./gradlew --settings-file settings.desktop.gradle :RemixedDungeonDesktop:build` | ✅ JAR + dist |
| **Android** | `./gradlew --settings-file settings.android.gradle :RemixedDungeon:assembleAndroidFdroidDebug` | ✅ APK |

---

## Tickets Resolved

- **remixed-dungeon-ce9** — Replace LuaJ reflection with codegen maps for TeaVM web build ✅
- **remixed-dungeon-cna** — TeaVM: Extend official teavm-classlib with missing JDK classes from NYRDS fork ✅
- **remixed-dungeon-sy7** — Extend teavm-classlib with missing JDK classes ✅

---

## Future Maintenance

### To Rebuild Teavm from Submodule
```bash
cd teavm
git checkout remixed-patches-local  # or remotes/origin/remixed-patches
./gradlew :classlib:publishToMavenLocal :core:publishToMavenLocal :interop:core:publishToMavenLocal :jso:core:publishToMavenLocal :jso:apis:publishToMavenLocal :jso:impl:publishToMavenLocal :platform:publishToMavenLocal :tools:gradle:publishToMavenLocal :metaprogramming:api:publishToMavenLocal :metaprogramming:impl:publishToMavenLocal
```

### To Rebuild Luaj from Submodule
```bash
cd luaj
ant -f build.xml jar-jse
mvn install:install-file -Dfile=luaj-jse-3.0.2.jar -DgroupId=org.luaj -DartifactId=luaj-jse -Dversion=3.0.2 -Dpackaging=jar
```

### To Upgrade TeaVM Later
1. Check if newer TeaVM versions have fixed the `ClassInfoGenerator` NPE bug
2. If fixed, merge remixed-patches classlib stubs into newer TeaVM version
3. Update `teaVmVersion` in build.gradle
4. Rebuild and publish to mavenLocal
5. Test all 4 build targets

---

## Files Modified

### Core Changes
- `teavm` (submodule commit updated)
- `RemixedDungeonHtml/build.gradle`
- `supplemental-classlib/build.gradle`
- `supplemental-classlib/src/main/java/org/teavm/classlib/java/lang/TSystem.java`
- `supplemental-classlib/src/main/resources/META-INF/teavm.properties` (new)
- `RemixedDungeonHtml/src/html/java/com/nyrds/platform/lua/PlatformLuajavaLib.java`
- `RemixedDungeonHtml/src/html/java/com/nyrds/platform/lua/LuaReflectionSupplier.java`
- `RemixedDungeonHtml/src/html/java/com/nyrds/teavm/TeaVMRunner.java`
- `RemixedDungeonHtml/src/html/java/com/nyrds/pixeldungeon/ml/ResourceMap.java`
- `RemixedDungeonHtml/src/html/java/com/nyrds/pixeldungeon/ml/BuildConfig.java`
- `RemixedDungeonHtml/src/html/java/com/nyrds/pixeldungeon/ml/R.java`
- `RemixedDungeonHtml/src/html/java/com/nyrds/platform/util/Os.java`
- `RemixedDungeonHtml/src/html/java/com/nyrds/platform/app/Input.java` (new)
- `settings.html.gradle`

### Copied Generated Sources
- `RemixedDungeon/src/main/java/com/nyrds/pixeldungeon/levels/TerrainMap.java` (new)
- `RemixedDungeon/src/main/java/com/nyrds/pixeldungeon/ml/ResourceMap.java` (new)
- `RemixedDungeonDesktop/src/generated/java/com/nyrds/pixeldungeon/levels/TerrainMap.java` (new)
- `RemixedDungeonDesktop/src/generated/java/com/nyrds/pixeldungeon/ml/BuildConfig.java` (new)
- `RemixedDungeonDesktop/src/generated/java/com/nyrds/pixeldungeon/ml/ResourceMap.java` (new)

---

## Git Commit
```
0a7e23b35 chore: switch to local teavm (remixed-patches) and luaj from submodules
```