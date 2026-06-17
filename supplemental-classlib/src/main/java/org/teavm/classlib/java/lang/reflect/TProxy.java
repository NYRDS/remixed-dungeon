/*
 * TeaVM supplemental classlib - minimal implementation for missing JDK classes
 */
package org.teavm.classlib.java.lang.reflect;

import org.teavm.classlib.java.io.TSerializable;

public class TProxy implements TSerializable {
    private static final long serialVersionUID = 1L;

    protected Object h;

    protected TProxy(Object h) {
        this.h = h;
    }

    public static Object newProxyInstance(ClassLoader loader, Class<?>[] interfaces, java.lang.reflect.InvocationHandler h) {
        if (loader == null) {
            throw new IllegalArgumentException("Null class loader");
        }
        if (interfaces == null) {
            throw new IllegalArgumentException("Null interfaces");
        }
        if (h == null) {
            throw new IllegalArgumentException("Null handler");
        }
        // In TeaVM web environment, we can't create real proxies
        // Return a stub that implements the interfaces
        throw new UnsupportedOperationException("Proxy.newProxyInstance not fully implemented in TeaVM web environment");
    }

    public static boolean isProxyClass(Class<?> cls) {
        return false;
    }

    public static java.lang.reflect.InvocationHandler getInvocationHandler(Object proxy) {
        if (proxy == null || !(proxy instanceof TProxy)) {
            throw new IllegalArgumentException("Not a proxy instance");
        }
        return (java.lang.reflect.InvocationHandler) ((TProxy) proxy).h;
    }
}