/*
 * TeaVM supplemental classlib - minimal implementation for missing JDK classes
 */
package org.teavm.classlib.java.lang;

public class TRuntime {
    private static final TRuntime INSTANCE = new TRuntime();

    private TRuntime() {}

    public static TRuntime getRuntime() {
        return INSTANCE;
    }

    public void exit(int status) {
        TSystem.exit(status);
    }

    public void addShutdownHook(Thread hook) {
        // No-op in web environment
    }

    public boolean removeShutdownHook(Thread hook) {
        return false;
    }

    public Process exec(String command) throws Exception {
        throw new UnsupportedOperationException("Runtime.exec not supported in TeaVM web environment");
    }

    public Process exec(String[] cmdarray) throws Exception {
        return exec(String.join(" ", cmdarray));
    }

    public Process exec(String command, String[] envp) throws Exception {
        return exec(command);
    }

    public Process exec(String[] cmdarray, String[] envp) throws Exception {
        return exec(cmdarray);
    }

    public Process exec(String command, String[] envp, java.io.File dir) throws Exception {
        return exec(command);
    }

    public Process exec(String[] cmdarray, String[] envp, java.io.File dir) throws Exception {
        return exec(cmdarray);
    }

    public int availableProcessors() {
        return 1;
    }

    public long freeMemory() {
        return Runtime.getRuntime().freeMemory();
    }

    public long totalMemory() {
        return Runtime.getRuntime().totalMemory();
    }

    public long maxMemory() {
        return Runtime.getRuntime().maxMemory();
    }

    public void gc() {
        Runtime.getRuntime().gc();
    }

    public void runFinalization() {
        Runtime.getRuntime().runFinalization();
    }

    public void load(String filename) {
        Runtime.getRuntime().load(filename);
    }

    public void loadLibrary(String libname) {
        Runtime.getRuntime().loadLibrary(libname);
    }
}