package com.nyrds.teavm.reflection;


public class ReflectionConfig {
    /**
     * This method is a powerful debugging tool that references classes to prevent
     * TeaVM from removing them during optimization.
     *
     * WARNING: DO NOT USE THIS IN PRODUCTION. It will dramatically increase the size
     * of your JavaScript file. Use this only to identify the exact classes that
     * need reflection, then annotate them with @Reflectable individually.
     */
    public static void enableReflectionForDebugging() {
        System.out.println("Enabling reflection for debugging purposes.");
        
    }
}