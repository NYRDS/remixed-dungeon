/*
 * TeaVM supplemental classlib - minimal implementation for missing JDK classes
 */
package org.teavm.classlib.java.lang.reflect;

public interface InvocationHandler {
    Object invoke(Object proxy, Object method, Object[] args) throws Throwable;
}