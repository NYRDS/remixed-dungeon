package org.luaj.vm2.lib.jse;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;

/**
 * Public accessor for package-private {@link JavaClass#forClass(Class)}.
 * Also provides hierarchy-walking method/field lookup for TeaVM where
 * reflection may only expose methods on specific classes in the hierarchy.
 */
public class JavaClassHelper {
    private JavaClassHelper() {}

    public static Varargs forClass(Class<?> clazz) {
        return JavaClass.forClass(clazz);
    }

    /**
     * Look up a method by name, walking up the superclass chain if not found
     * on the concrete class. This is needed in TeaVM because reflection only
     * exposes methods that were declared in classes registered via ReflectionSupplier.
     */
    public static LuaValue getMethodWithHierarchy(Class<?> clazz, LuaValue key) {
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            JavaClass jc = JavaClass.forClass(current);
            LuaValue method = jc.getMethod(key);
            if (method != null) {
                return method;
            }
            current = current.getSuperclass();
        }
        return null;
    }

    /**
     * Look up a field by name, walking up the superclass chain if not found
     * on the concrete class.
     */
    public static Field getFieldWithHierarchy(Class<?> clazz, LuaValue key) {
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            try {
                Field[] fields = current.getDeclaredFields();
                for (Field f : fields) {
                    if (Modifier.isPublic(f.getModifiers()) && f.getName().equals(key.tojstring())) {
                        if (!f.isAccessible()) {
                            f.setAccessible(true);
                        }
                        return f;
                    }
                }
            } catch (SecurityException ignored) {
            }
            current = current.getSuperclass();
        }
        return null;
    }
}
