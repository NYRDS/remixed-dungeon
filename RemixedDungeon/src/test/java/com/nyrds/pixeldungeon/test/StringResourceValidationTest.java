package com.nyrds.pixeldungeon.test;

import org.junit.Test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Unit test to verify that string and string-array resources exist and have valid syntax
 */
public class StringResourceValidationTest {

    private static final String PROJECT_ROOT = "/home/mike/StudioProjects/remixed-dungeon";
    private static final String VALUES_DIR = PROJECT_ROOT + "/RemixedDungeon/src/main/res/values";

    @Test
    public void validateStringResourcesSyntax() throws IOException {
        Path stringsPath = Paths.get(VALUES_DIR, "strings_all.xml");
        Path arraysPath = Paths.get(VALUES_DIR, "string_arrays.xml");
        
        assertTrue("strings_all.xml should exist", Files.exists(stringsPath));
        assertTrue("string_arrays.xml should exist", Files.exists(arraysPath));
        
        String stringsContent = Files.readString(stringsPath);
        String arraysContent = Files.readString(arraysPath);
        
        // Validate basic XML structure for string resources
        validateStringResources(stringsContent);
        
        // Validate basic XML structure for string array resources
        validateStringArrayResources(arraysContent);
        
        System.out.println("Successfully validated string and string-array resource files.");
    }
    
    private void validateStringResources(String content) {
        // Pattern to match string resources
        Pattern stringPattern = Pattern.compile("<string\\s+name=\"([^\"]+)\"[^>]*>(.*?)</string>", Pattern.DOTALL);
        Matcher matcher = stringPattern.matcher(content);
        
        int stringCount = 0;
        while (matcher.find()) {
            String name = matcher.group(1);
            String value = matcher.group(2);
            
            // Validate that name is not empty
            assertFalse("String resource name should not be empty", name.isEmpty());
            
            // Check for common issues in values
            validateStringValue(name, value);
            
            stringCount++;
        }
        
        System.out.println("Validated " + stringCount + " string resources.");
    }
    
    private void validateStringArrayResources(String content) {
        // Pattern to match string array resources
        Pattern arrayPattern = Pattern.compile("<string-array\\s+name=\"([^\"]+)\"[^>]*>(.*?)</string-array>", Pattern.DOTALL);
        Matcher arrayMatcher = arrayPattern.matcher(content);
        
        int arrayCount = 0;
        while (arrayMatcher.find()) {
            String arrayName = arrayMatcher.group(1);
            String arrayContent = arrayMatcher.group(2);
            
            // Validate that array name is not empty
            assertFalse("String array name should not be empty", arrayName.isEmpty());
            
            // Validate individual items in the array
            Pattern itemPattern = Pattern.compile("<item>(.*?)</item>", Pattern.DOTALL);
            Matcher itemMatcher = itemPattern.matcher(arrayContent);
            
            int itemCount = 0;
            while (itemMatcher.find()) {
                String itemValue = itemMatcher.group(1);
                
                // Check for common issues in values
                validateStringValue(arrayName + "[item]", itemValue);
                
                itemCount++;
            }
            
            System.out.println("Validated string array '" + arrayName + "' with " + itemCount + " items.");
            arrayCount++;
        }
        
        System.out.println("Validated " + arrayCount + " string array resources.");
    }
    
    private void validateStringValue(String resourceName, String value) {
        // Check for unescaped quotes that might cause issues
        if (value.contains("\"")) {
            System.out.println("Warning: Resource " + resourceName + " contains unescaped quotes: " + value);
        }
        
        // Check for common formatting placeholders
        if (value.contains("%")) {
            // Check if format placeholders are properly formed
            Pattern formatPattern = Pattern.compile("%[0-9]*\\$?[sdff]");
            if (!formatPattern.matcher(value).find()) {
                System.out.println("Warning: Resource " + resourceName + " contains % but may have invalid format: " + value);
            }
        }
        
        // Basic validation - value should not be null
        assertNotNull("Resource " + resourceName + " should not have null value", value);
    }
}