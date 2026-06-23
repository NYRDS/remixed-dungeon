# Luaj Reflection Removal for TeaVM Compatibility

## Phase 1: COMPLETED ✅

### Changes Made
Removed unused luajava functions from `luaj/src/jse/org/luaj/vm2/lib/jse/LuajavaLib.java`:
- `CREATEPROXY` (opcode 4) - used `Proxy.newProxyInstance()` and `InvocationHandler`
- `LOADLIB` (opcode 5) - used `Class.getMethod()` and `Method.invoke()`
- `ProxyInvocationHandler` inner class
- Unused imports: `Proxy`, `InvocationHandler`, `Array`, `InvocationTargetException`

### Remaining luajava functions (what Remixed actually uses)
- `bindClass("classname")` → `Class.forName()` → `JavaClass.forClass()`
- `newInstance("classname", args...)` → `Class.forName()` → constructor lookup
- `newInstance(ClassObject, args...)` → direct constructor lookup

### Build Verification - All Passed
| Build | Command | Status |
|-------|---------|--------|
| Core (shared) | `./gradlew compileJava` | ✅ SUCCESS |
| Desktop (JVM) | `./gradlew --settings-file settings.desktop.gradle :RemixedDungeonDesktop:build` | ✅ SUCCESS |
| Android | `./gradlew --settings-file settings.android.gradle :RemixedDungeon:assembleAndroidFdroidDebug` | ✅ SUCCESS |

---

## Phase 2: Replace Reflection with Static Maps (For TeaVM)

### Remaining Reflection Hotspots to Replace

#### 1. `JavaClass.java` - Class member discovery
```java
// Current (uses reflection)
Field[] f = ((Class)m_instance).getFields();
Method[] m = ((Class)m_instance).getMethods();
Constructor[] c = ((Class)m_instance).getConstructors();
Class[] c = ((Class)m_instance).getClasses();

// Replace with: Pre-computed static caches
Map<Class, Map<String, Field>> FIELD_CACHE
Map<Class, Map<String, List<JavaMethod>>> METHOD_CACHE
Map<Class, Map<String, List<JavaConstructor>>> CONSTRUCTOR_CACHE
Map<Class, Map<String, Class>> INNER_CLASS_CACHE
```

#### 2. `CoerceLuaToJava.inheritanceLevels()` - Inheritance hierarchy
```java
// Current (recursive reflection)
Class[] ifaces = subclass.getInterfaces();
inheritanceLevels(baseclass, subclass.getSuperclass())

// Replace with: Pre-computed depth cache
Map<Class, Map<Class, Integer>> INHERITANCE_DEPTH_CACHE
```

#### 3. `JavaMethod.invoke()` / `JavaConstructor.newInstance()` / `JavaInstance` field access
- Use **MethodHandle** / **VarHandle** (Java 7+/9+) instead of reflection
- Much faster and TeaVM-compatible with `@Reflected` annotations

### TeaVM Build Integration Strategy

#### 1. Build-time Registry Generator (Gradle task in `RemixedDungeonHtml`)
- Scan all `.lua` files for `luajava.bindClass()` and `luajava.newInstance()` calls
- Extract unique class names (~100 classes)
- Generate `LuajavaRegistry.java`:
```java
Map<String, Class<?>> CLASS_MAP = Map.of(
    "com.watabou.pixeldungeon.Dungeon", Dungeon.class,
    "com.watabou.pixeldungeon.utils.GLog", GLog.class,
    // ... all ~100 classes
);

Map<String, Function<Varargs, Object>> FACTORY_MAP = Map.of(
    "com.watabou.pixeldungeon.Dungeon", args -> new Dungeon(),
    "com.watabou.pixeldungeon.items.wands.WandOfBlink", 
        args -> new WandOfBlink(args.arg(1).toint(), ...),
    // ... constructors with args
);
```

#### 2. TeaVM variant of luaj or SPI interface
- `LuajavaLib` accepts optional `LuajavaRegistry`
- Falls back to reflection if not provided (for Desktop/Android)
- Uses static maps for TeaVM

#### 3. Enable TeaVM reflection fallback for public members
```gradle
properties.put('org.teavm.reflect.enableRef', 'true')
```

---

## Files Modified in Phase 1
- `luaj/src/jse/org/luaj/vm2/lib/jse/LuajavaLib.java` - Removed unused reflection code

## Next Steps for Phase 2
1. Create Gradle task to scan Lua scripts and generate `LuajavaRegistry.java`
2. Modify `JavaClass.java` to use static caches instead of reflection
3. Modify `CoerceLuaToJava.java` to use cached inheritance depths
4. Replace `Method.invoke()` with `MethodHandle` in `JavaMethod.java`
5. Replace `Constructor.newInstance()` with `MethodHandle` in `JavaConstructor.java`
6. Replace `Field.get/set()` with `VarHandle` in `JavaInstance.java`
7. Create TeaVM-compatible `LuajavaLib` variant that uses registry
8. Test TeaVM build