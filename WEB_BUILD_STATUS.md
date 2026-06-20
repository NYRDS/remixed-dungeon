# Web Build (TeaVM) - Current State Documentation

## Date
2026-06-15

## Branch
teavm-classlib-stubs (134 commits ahead of origin/teavm-classlib-stubs, includes pet inventory merge from master)

## Project Structure
- **RemixedDungeonHtml** - HTML/TeaVM module
- **settings.html.gradle** - Settings file for HTML build
- **TeaVM version**: 0.14.1 (via org.teavm Gradle plugin 0.14.1)
- **LibGDX version**: 1.12.1 (resolved to 1.13.5 via gdx-teavm)
- **gdx-teavm version**: 1.3.0

## Build Commands
```bash
# Compile Java sources
./gradlew --settings-file settings.html.gradle :RemixedDungeonHtml:compileJava --no-daemon

# Run TeaVM compilation (custom task with dependency propagation)
./gradlew --settings-file settings.html.gradle :RemixedDungeonHtml:compileTeavmApp --no-daemon
```

## Changes Made

### 1. Dependency Propagation Fix (2026-06-15)
**SOLVED**: The TeaVM Gradle plugin 0.14.1 now properly includes project dependencies via:
```gradle
configurations {
    teavm {
        extendsFrom runtimeClasspath
    }
}
```
And a custom `compileTeavmApp` task that runs TeaVMTool directly with the `teavm` configuration classpath.

### 2. TeaVMRunner (RemixedDungeonHtml/src/html/java/com/nyrds/teavm/TeaVMRunner.java)
- Wrapper class for TeaVMTool with proper problem reporting
- Implements TeaVMToolLog interface with full method coverage
- Sets context classloader
- Reports problems via getProblemProvider().getProblems()

### 3. Custom compileTeavmApp Gradle Task
- JavaExec task running TeaVMRunner
- Uses `configurations.teavm + sourceSets.main.output` for classpath
- Arguments: mainClass, targetDir, targetFileName, classpath, sourcePath, reflectionEnabled
- Copies output to standard location (`build/teavm/teavm-app.js`)

## Current Build Results

### compileJava: SUCCESS
- All Java sources compile with stubs
- Annotation processor runs (BundleHelper, LuaClassMap generated)

### compileTeavmApp: RUNS BUT FAILS
**Progress**: TeaVM compilation now executes and processes dependencies!
**Blocker**: Missing JDK classes in official teavm-classlib:0.14.1

Error output:
```
ERROR: There's no main class: '{{c0}}'
```
- 22 placeholder errors `{{c0}}`/`{{m0}}`/`{{f0}}`
- TeaVM can't resolve main class due to missing JDK class dependencies

## Technical Root Cause - UPDATED
**Dependency propagation: SOLVED** ✅
The `teavm { extendsFrom runtimeClasspath }` configuration now includes all 50+ project dependencies (LibGDX, LuaJ, Guava, HJSON, etc.) in the TeaVM compilation classpath.

**Remaining blocker: Missing JDK classes in official teavm-classlib:0.14.1** ❌
TeaVM cannot resolve 15+ JDK classes used by the project:
- AtomicIntegerArray, Proxy, CountDownLatch, Future, Process
- InetAddress, NetworkInterface, SocketException
- Collator, IOException, and others

The NYRDS fork has these classes but format is incompatible with official TeaVM (placeholder `{{c0}}` errors).

## Files Modified/Created
```
RemixedDungeonHtml/build.gradle (added teavm config + compileTeavmApp task)
RemixedDungeonHtml/src/html/java/com/nyrds/teavm/TeaVMRunner.java
settings.html.gradle (added TeaVM maven repository)
```

## Next Steps Required
1. **Extend official teavm-classlib:0.14.1** with missing JDK classes from NYRDS fork
   - Repackage NYRDS classes to java.* packages
   - Add to supplemental-classlib.jar
   - Key classes: AtomicIntegerArray, Proxy, CountDownLatch, Future, Process, InetAddress, NetworkInterface, SocketException, Collator, IOException

2. **Alternative**: Upgrade to TeaVM 0.15+ (requires Java 17)
   - May have better classlib coverage
   - But project uses Java 11

## Other Builds (Working)
- **Desktop**: `./gradlew :RemixedDungeonDesktop:build` - SUCCESS
- **Android**: `./gradlew --settings-file settings.android.gradle :RemixedDungeon:assembleAndroidFdroidDebug` - SUCCESS

## Notes
- GSON → org.json migration completed (remixed-dungeon-5vb)
- Pet inventory feature merged from master (39 commits) included in this branch
- Desktop/Android builds verified working with pet inventory + GSON migration