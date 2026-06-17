/*
 * TeaVM supplemental classlib - minimal implementation for missing JDK classes
 */
package org.teavm.classlib.java.lang;

public final class TSystem {
    private TSystem() {}

    public static void exit(int status) {
        // In web environment, we can't really exit
        // Just do nothing or throw an exception
        throw new UnsupportedOperationException("System.exit not supported in TeaVM web environment");
    }

    public static void gc() {
        // No-op in web environment
    }

    public static long currentTimeMillis() {
        return System.currentTimeMillis();
    }

    public static long nanoTime() {
        return System.nanoTime();
    }

    public static String getProperty(String key) {
        return System.getProperty(key);
    }

    public static String getProperty(String key, String def) {
        return System.getProperty(key, def);
    }

    public static String setProperty(String key, String value) {
        return System.setProperty(key, value);
    }

    public static int identityHashCode(Object x) {
        return System.identityHashCode(x);
    }

    public static String getenv(String name) {
        return System.getenv(name);
    }

    public static String getTempDir() {
        return System.getProperty("java.io.tmpdir");
    }

    public static void arraycopy(Object src, int srcPos, Object dest, int destPos, int length) {
        // Delegate to actual System.arraycopy
        java.lang.System.arraycopy(src, srcPos, dest, destPos, length);
    }

    public static java.util.Map<String, String> getenv() {
        return System.getenv();
    }

    public static void loadLibrary(String libname) {
        throw new UnsupportedOperationException("System.loadLibrary not supported");
    }

    public static void load(String filename) {
        throw new UnsupportedOperationException("System.load not supported");
    }

    public static java.io.PrintStream out() {
        return System.out;
    }

    public static java.io.PrintStream err() {
        return System.err;
    }

    public static java.io.InputStream in() {
        return System.in;
    }
}