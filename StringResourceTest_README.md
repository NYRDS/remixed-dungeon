# Android String and String-Array Resource Test

This document describes the Android test created to query and try to use all string and string-array resources in the Remixed Dungeon project.

## Test Files Created

1. **StringResourceInstrumentationTest.java** - An instrumentation test that runs on an Android device/emulator and tests all string and string-array resources by:
   - Accessing resources through the Android Resources system
   - Validating that each resource can be retrieved without error
   - Testing formatted strings with dummy arguments
   - Checking for null values and other common issues

2. **StringResourceValidationTest.java** - A unit test that validates the XML syntax of resource files without requiring Android dependencies.

3. **StringResourceXmlValidationTest.java** - A unit test that parses XML files directly to validate structure and content.

4. **SimpleResourceTest.java** - A basic test to verify test infrastructure.

## Test Implementation Details

### StringResourceInstrumentationTest
This is the main test that performs the following operations:

1. Gets the application context and resources
2. Uses reflection to discover all string and string-array resource names
3. Iterates through each resource and attempts to retrieve its value
4. For formatted strings (containing % placeholders), tests formatting with dummy arguments
5. Validates that no resources return null values
6. For string arrays, validates each individual item

### Resource Discovery
The test uses reflection to discover all resource names:
- Accesses the R class dynamically
- Finds inner classes for "string" and "array" resource types
- Extracts field names which correspond to resource names

### Format String Validation
For strings containing format specifiers (%s, %d, %f, etc.), the test:
- Counts the number of format specifiers
- Creates appropriate dummy arguments (strings, integers, floats)
- Attempts to format the string to ensure it doesn't crash

## Resource Fixes Applied

The following issues in the project's resource files have been fixed:

### French strings_all.xml
- Fixed unescaped apostrophes in the "PlagueDoctorQuest_4_1_Prologue" string that were causing build failures

### Italian strings_all.xml
- Fixed unescaped apostrophes in the "PlagueDoctorNPC_Desc" string
- Fixed unescaped apostrophes in the "town_deco_statue_water_desc" string

### Japanese strings_all.xml
- Fixed non-positional format string in "MobAi_status" by adding positional indicators (%1$s, %2$s)

## Current Build Issues

After fixing the project's own resource files, the build still fails due to an external dependency issue:
- Invalid Unicode escape sequence in the material library (material-1.12.0) Italian values file
- The issue is in the gdpr_text string in the cached material library: /home/mike/.gradle/caches/8.12.1/transforms/4fa98587e48f38296e30e40f607b0c46/transformed/material-1.12.0/res/values-it/values-it.xml

This is an external dependency issue that is outside the scope of this project's resource files.

## How to Run the Tests

1. After ensuring the resource file issues are resolved:
   ```bash
   ./gradlew -c settings.android.gradle :RemixedDungeon:testAndroidGooglePlayDebugUnitTest
   ```

2. For instrumentation tests on a connected device/emulator:
   ```bash
   ./gradlew -c settings.android.gradle :RemixedDungeon:connectedAndroidTest
   ```

## Test Coverage

The tests cover:
- All string resources in strings_all.xml
- All string resources in other string files
- All string-array resources in string_arrays.xml
- Proper handling of formatted strings
- Null value validation
- Format specifier validation

## Benefits

This test ensures:
- All string resources can be accessed programmatically
- No malformed XML in resource files
- Proper formatting of strings with placeholders
- Early detection of resource-related issues
- Verification that localization strings are properly structured