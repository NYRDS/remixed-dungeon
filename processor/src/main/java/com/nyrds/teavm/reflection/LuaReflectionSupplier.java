package com.nyrds.teavm.reflection;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.teavm.classlib.ReflectionContext;
import org.teavm.classlib.ReflectionSupplier;
import org.teavm.model.AnnotationContainerReader;
import org.teavm.model.ClassReader;
import org.teavm.model.ElementModifier;
import org.teavm.model.FieldReader;
import org.teavm.model.MethodDescriptor;
import org.teavm.model.MethodReader;
import org.teavm.model.ValueType;

/**
 * Build-time reflection metadata supplier for the TeaVM web build.
 *
 * Lua scripts reach the game exclusively through luajava
 * bindClass/newInstance, and every lua-reachable class carries
 *
 * @LuaInterface - expose members of exactly those classes so TeaVM's runtime
 * reflection can serve them, keeping everything else (and its transitive
 * bodies) out of reflection linking.
 *
 * On top of @LuaInterface classes, a fixed allowlist of JDK classes is
 * exposed: lua calls methods on RETURNED objects (a DungeonGenerator
 * returning ArrayList means lua needs ArrayList:size/get). On the JVM plain
 * reflection covers that; TeaVM needs metadata per class.
 *
 * Members whose signatures reference classes TeaVM cannot link
 * (java.util.logging, java.text) are withheld, as are native methods (they
 * have no renderable reflection callable).
 *
 * Loaded via META-INF/services from the TeaVM tool's classpath.
 */
public class LuaReflectionSupplier implements ReflectionSupplier {

    private static final String LUA_INTERFACE = "com.nyrds.LuaInterface";
    private static final String MAP_RESOURCE = "lua-interface-map.json";

    private static Set<String> luaClassesByName;

    /**
     * The annotation processor emits lua-interface-map.json listing every
     * @LuaInterface class. TeaVM only links classes reachable from the entry
     * point - lua-bound classes often are not - so declare them all as
     * findable by name.
     */
    private static synchronized Set<String> luaClassesByName(ReflectionContext context) {
        if (luaClassesByName != null) {
            return luaClassesByName;
        }
        Set<String> names = new HashSet<>();
        try (java.io.InputStream in = context.getClassLoader().getResourceAsStream(MAP_RESOURCE)) {
            if (in != null) {
                java.util.Scanner scanner = new java.util.Scanner(in, "UTF-8").useDelimiter("\\A");
                String text = scanner.hasNext() ? scanner.next() : "";
                java.util.regex.Matcher matcher =
                        java.util.regex.Pattern.compile("\"([a-zA-Z0-9_.]+)\"\\s*:").matcher(text);
                while (matcher.find()) {
                    names.add(matcher.group(1));
                }
            }
        } catch (Exception e) {
            System.err.println("LUAREFL: failed to read " + MAP_RESOURCE + ": " + e);
        }
        System.err.println("LUAREFL: " + names.size() + " lua classes findable by name");
        luaClassesByName = names;
        return names;
    }

    @Override
    public Collection<String> getClassesFoundByName(ReflectionContext context) {
        return luaClassesByName(context);
    }

    @Override
    public boolean isClassFoundByName(ReflectionContext context, String name) {
        return luaClassesByName(context).contains(name);
    }

    private static boolean isLuaClass(ReflectionContext context, String className) {
        // the processor-generated map covers classes with any @LuaInterface
        // member (method-level annotations included), so trust it first
        if (luaClassesByName(context).contains(className)) {
            return true;
        }
        ClassReader cls = context.getClassSource().get(className);
        if (cls == null) {
            return false;
        }
        AnnotationContainerReader annotations = cls.getAnnotations();
        if (annotations != null && annotations.get(LUA_INTERFACE) != null) {
            return true;
        }
        // lua binds a few base types through non-annotated parents (enums,
        // abstract bases) - accept their subclasses so runtime instances
        // of exact subclasses stay usable
        String parent = cls.getParent();
        while (parent != null && !parent.equals("java.lang.Object")) {
            ClassReader parentCls = context.getClassSource().get(parent);
            if (parentCls == null) {
                return false;
            }
            if (parentCls.getAnnotations().get(LUA_INTERFACE) != null) {
                return true;
            }
            parent = parentCls.getParent();
        }
        return false;
    }

    /**
     * JDK classes lua operates on through RETURNED objects. Deliberately an
     * explicit allowlist rather than an automatic signature scan: linking the
     * members of a class pulls its whole body into TeaVM, and broad automatic
     * exposure (guava, java.io) trips over classlib APIs TeaVM never links.
     * Classes missing here surface as "attempt to call a nil value" lua errors
     * naming the class - add them case by case.
     */
    private static final Set<String> EXTRA_EXPOSED = Set.of(
            // collections
            "java.util.List",
            "java.util.ArrayList",
            "java.util.Arrays$ArrayList",
            "java.util.Arrays",
            "java.util.Collection",
            "java.util.Collections",
            "java.util.Deque",
            "java.util.Enumeration",
            "java.util.HashMap",
            "java.util.HashSet",
            "java.util.Hashtable",
            "java.util.Iterator",
            "java.util.LinkedHashMap",
            "java.util.LinkedHashSet",
            "java.util.LinkedList",
            "java.util.ListIterator",
            "java.util.Map",
            "java.util.Map$Entry",
            "java.util.NavigableMap",
            "java.util.NavigableSet",
            "java.util.PriorityQueue",
            "java.util.Queue",
            "java.util.Random",
            "java.util.Set",
            "java.util.SortedMap",
            "java.util.SortedSet",
            "java.util.Stack",
            "java.util.TreeMap",
            "java.util.TreeSet",
            "java.util.Vector",
            // java.lang basics - Class/Object/Throwable deliberately excluded:
            // exposing them links TeaVM runtime internals (getNameImpl,
            // RuntimeClass.unpack) that have no renderable callable
            "java.lang.Boolean",
            "java.lang.Byte",
            "java.lang.Character",
            "java.lang.Double",
            "java.lang.Enum",
            "java.lang.Float",
            "java.lang.Integer",
            "java.lang.Long",
            "java.lang.Math",
            "java.lang.Number",
            "java.lang.Short",
            "java.lang.String",
            "java.lang.StringBuffer",
            "java.lang.StringBuilder",
            // json
            "org.json.JSONObject",
            "org.json.JSONArray",
            "org.json.JSONTokener"
    );

    private static boolean isExposed(ReflectionContext context, String className) {
        return isLuaClass(context, className) || EXTRA_EXPOSED.contains(className);
    }

    private static boolean typeLinkable(ReflectionContext context, ValueType type) {
        if (!(type instanceof ValueType.Object)) {
            return true;
        }
        String name = ((ValueType.Object) type).getClassName();
        if (name.startsWith("java.util.logging.") || name.startsWith("java.text.")) {
            return false;
        }
        if (name.startsWith("java.") || name.startsWith("javax.")) {
            return true;
        }
        return context.getClassSource().get(name) != null;
    }

    private static boolean methodLinkable(ReflectionContext context, MethodDescriptor descriptor) {
        if (!typeLinkable(context, descriptor.getResultType())) {
            return false;
        }
        for (ValueType param : descriptor.getSignature()) {
            if (!typeLinkable(context, param)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Collection<String> getAccessibleFields(ReflectionContext context, String className) {
        if (!isExposed(context, className)) {
            return Collections.emptyList();
        }
        ClassReader cls = context.getClassSource().get(className);
        if (cls == null) {
            return Collections.emptyList();
        }
        Set<String> fields = new HashSet<>();
        for (FieldReader field : cls.getFields()) {
            if (typeLinkable(context, field.getType())) {
                fields.add(field.getName());
            }
        }
        return fields;
    }

    @Override
    public Collection<MethodDescriptor> getAccessibleMethods(ReflectionContext context, String className) {
        if (!isExposed(context, className)) {
            return Collections.emptyList();
        }
        ClassReader cls = context.getClassSource().get(className);
        if (cls == null) {
            return Collections.emptyList();
        }
        Set<MethodDescriptor> methods = new HashSet<>();
        for (MethodReader method : cls.getMethods()) {
            // native methods (JSBody shims etc.) have no renderable reflection
            // callable - linking them via Method.invoke fails the build with
            // "Native method has no implementation"
            if (method.hasModifier(ElementModifier.NATIVE)) {
                continue;
            }
            if (methodLinkable(context, method.getDescriptor())) {
                methods.add(method.getDescriptor());
            }
        }
        return methods;
    }
}
