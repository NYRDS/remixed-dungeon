package com.nyrds.pixeldungeon.test;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Simple test to verify that the test infrastructure works
 */
public class SimpleResourceTest {

    @Test
    public void testResourceFileExistence() {
        // Verify that the resource files exist
        String stringsFilePath = "/home/mike/StudioProjects/remixed-dungeon/RemixedDungeon/src/main/res/values/strings_all.xml";
        String arraysFilePath = "/home/mike/StudioProjects/remixed-dungeon/RemixedDungeon/src/main/res/values/string_arrays.xml";
        
        java.io.File stringsFile = new java.io.File(stringsFilePath);
        java.io.File arraysFile = new java.io.File(arraysFilePath);
        
        assertTrue("strings_all.xml should exist", stringsFile.exists());
        assertTrue("string_arrays.xml should exist", arraysFile.exists());
        
        System.out.println("Resource files exist:");
        System.out.println("- " + stringsFile.getAbsolutePath() + " (size: " + stringsFile.length() + " bytes)");
        System.out.println("- " + arraysFile.getAbsolutePath() + " (size: " + arraysFile.length() + " bytes)");
    }
}