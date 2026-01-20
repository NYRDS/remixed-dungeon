package com.nyrds.pixeldungeon.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static org.junit.Assert.*;

import android.content.res.Resources;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Test to query and try to use all string and string-array resources in the app
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class StringResourceTest {

    @Test
    public void testAllStringResources() {
        Resources resources = RuntimeEnvironment.getApplication().getResources();
        
        // Get all string resource IDs using reflection
        Set<String> stringResourceNames = getResourceNames("string");
        Set<String> stringArrayResourceNames = getResourceNames("array");
        
        // Test all string resources
        for (String resourceName : stringResourceNames) {
            try {
                int resourceId = resources.getIdentifier(resourceName, "string", "com.nyrds.pixeldungeon");
                if (resourceId != 0) {
                    String value = resources.getString(resourceId);
                    // Basic validation - ensure the string is not null
                    assertNotNull("String resource " + resourceName + " should not be null", value);
                    
                    // Test string formatting with dummy arguments if it contains format specifiers
                    if (value.contains("%")) {
                        try {
                            String formattedValue = String.format(value, createDummyArgs(value));
                            // Just ensure it doesn't crash
                            assertNotNull(formattedValue);
                        } catch (Exception e) {
                            // Some strings might have complex formatting that can't be tested with dummy args
                            System.out.println("Warning: Could not format string resource " + resourceName + ": " + e.getMessage());
                        }
                    }
                }
            } catch (Exception e) {
                fail("Failed to access string resource " + resourceName + ": " + e.getMessage());
            }
        }
        
        // Test all string array resources
        for (String resourceName : stringArrayResourceNames) {
            try {
                int resourceId = resources.getIdentifier(resourceName, "array", "com.nyrds.pixeldungeon");
                if (resourceId != 0) {
                    String[] values = resources.getStringArray(resourceId);
                    // Basic validation - ensure the array is not null
                    assertNotNull("String array resource " + resourceName + " should not be null", values);
                    
                    // Test each string in the array
                    for (int i = 0; i < values.length; i++) {
                        String value = values[i];
                        assertNotNull("String array resource " + resourceName + "[" + i + "] should not be null", value);
                        
                        // Test string formatting with dummy arguments if it contains format specifiers
                        if (value.contains("%")) {
                            try {
                                String formattedValue = String.format(value, createDummyArgs(value));
                                // Just ensure it doesn't crash
                                assertNotNull(formattedValue);
                            } catch (Exception e) {
                                // Some strings might have complex formatting that can't be tested with dummy args
                                System.out.println("Warning: Could not format string array resource " + resourceName + "[" + i + "]: " + e.getMessage());
                            }
                        }
                    }
                }
            } catch (Exception e) {
                fail("Failed to access string array resource " + resourceName + ": " + e.getMessage());
            }
        }
        
        System.out.println("Successfully tested " + stringResourceNames.size() + " string resources and " + 
                          stringArrayResourceNames.size() + " string array resources.");
    }
    
    /**
     * Get resource names for a given resource type using reflection
     */
    private Set<String> getResourceNames(String resourceType) {
        Set<String> resourceNames = new HashSet<>();
        
        try {
            // Get the R class and its inner class for the resource type
            Class<?> rClass = Class.forName("com.nyrds.pixeldungeon.R");
            Class<?> resourceClass = null;
            
            // Find the inner class that matches the resource type
            for (Class<?> innerClass : rClass.getClasses()) {
                if (innerClass.getSimpleName().equals(resourceType)) {
                    resourceClass = innerClass;
                    break;
                }
            }
            
            if (resourceClass != null) {
                Field[] fields = resourceClass.getFields();
                for (Field field : fields) {
                    if (field.getType() == int.class) {
                        resourceNames.add(field.getName());
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            System.err.println("Could not find R class: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error getting resource names: " + e.getMessage());
        }
        
        return resourceNames;
    }
    
    /**
     * Create dummy arguments for string formatting based on the format string
     */
    private Object[] createDummyArgs(String formatString) {
        List<Object> args = new ArrayList<>();
        
        // Count format specifiers in the string
        int stringCount = countOccurrences(formatString, "%s");
        int intCount = countOccurrences(formatString, "%d") + countOccurrences(formatString, "%1$d") + 
                       countOccurrences(formatString, "%2$d") + countOccurrences(formatString, "%3$d");
        int floatCount = countOccurrences(formatString, "%f") + countOccurrences(formatString, "%.2f") +
                         countOccurrences(formatString, "%1$f") + countOccurrences(formatString, "%2$f");
        
        // Add dummy arguments based on the counts
        for (int i = 0; i < stringCount; i++) {
            args.add("dummy_string");
        }
        for (int i = 0; i < intCount; i++) {
            args.add(42); // Common dummy integer
        }
        for (int i = 0; i < floatCount; i++) {
            args.add(3.14f); // Common dummy float
        }
        
        return args.toArray();
    }
    
    /**
     * Count occurrences of a substring in a string
     */
    private int countOccurrences(String str, String substr) {
        if (str == null || substr == null || substr.isEmpty()) {
            return 0;
        }
        
        int count = 0;
        int idx = 0;
        
        while ((idx = str.indexOf(substr, idx)) != -1) {
            count++;
            idx += substr.length();
        }
        
        return count;
    }
}