package com.nyrds.lua;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class LuaSandbox {
    private static final Logger LOG = Logger.getLogger(LuaSandbox.class.getName());
    private static final String MAP_RESOURCE = "lua-interface-map.json";

    // Deduplication: track already-logged warnings
    private static final Set<String> LOGGED_WARNINGS = new HashSet<>();

    // File logging for Desktop
    private static FileHandler fileHandler = null;
    private static boolean fileLoggingInitialized = false;

    private static final Map<String, Set<String>> CLASS_METHODS = new HashMap<>();
    private static final Map<String, Set<String>> CLASS_FIELDS = new HashMap<>();
    private static final Map<String, Set<String>> CLASS_CONSTRUCTORS = new HashMap<>();
    private static final Set<String> CLASS_ANNOTATED = new HashSet<>();
    private static boolean initialized = false;

    static {
        initialize();
    }

    /**
     * Initializes file logging for Desktop platform.
     * Call this from Desktop launcher to enable file logging.
     */
    public static void initDesktopFileLogging(File logDir) {
        if (fileLoggingInitialized) return;

        try {
            if (!logDir.exists()) {
                logDir.mkdirs();
            }
            File logFile = new File(logDir, "lua-sandbox.log");
            fileHandler = new FileHandler(logFile.getAbsolutePath(), true); // append mode
            fileHandler.setFormatter(new SimpleFormatter() {
                @Override
                public String format(LogRecord record) {
                    return record.getLevel() + ": " + formatMessage(record) + "\n";
                }
            });
            LOG.addHandler(fileHandler);
            LOG.setUseParentHandlers(true); // Also log to console
            fileLoggingInitialized = true;
            LOG.info("LuaSandbox: Desktop file logging initialized at " + logFile.getAbsolutePath());
        } catch (IOException e) {
            LOG.severe("LuaSandbox: Failed to initialize file logging: " + e.getMessage());
        }
    }

    private static void initialize() {
        if (initialized) return;

        try (InputStream is = LuaSandbox.class.getClassLoader().getResourceAsStream(MAP_RESOURCE)) {
            if (is == null) {
                LOG.warning("LuaSandbox: " + MAP_RESOURCE + " not found on classpath - no sandbox enforcement");
                return;
            }

            // Read entire JSON into a string
            StringBuilder jsonBuilder = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    jsonBuilder.append(line).append('\n');
                }
            }
            String json = jsonBuilder.toString();

            // Parse JSON with custom parser (no GSON dependency)
            parseJsonMap(json);

            LOG.info("LuaSandbox initialized with " + CLASS_METHODS.size() + " classes");
            initialized = true;

        } catch (Exception e) {
            LOG.severe("LuaSandbox: Failed to load " + MAP_RESOURCE + ": " + e.getMessage());
        }
    }

    /**
     * Parses the JSON map using a simple custom parser.
     * Expected format:
     * {
     *   "className": { "methods": [...], "fields": [...], "constructors": [...], "classAnnotated": true },
     *   ...
     * }
     */
    private static void parseJsonMap(String json) {
        int i = 0;
        // Skip whitespace and opening brace
        i = skipWhitespace(json, i);
        if (i >= json.length() || json.charAt(i) != '{') return;
        i++;

        while (i < json.length()) {
            i = skipWhitespace(json, i);
            if (i >= json.length()) break;
            char c = json.charAt(i);
            if (c == '}') break; // End of object

            // Parse class name (key)
            String className = parseJsonString(json, i);
            if (className == null) break;
            i += className.length() + 2; // +2 for quotes

            i = skipWhitespace(json, i);
            if (i >= json.length() || json.charAt(i) != ':') break;
            i++; // Skip ':'

            i = skipWhitespace(json, i);
            if (i >= json.length() || json.charAt(i) != '{') break;
            i++; // Skip '{'

            // Parse class object
            boolean hasClassAnnotated = false;
            Set<String> methods = new HashSet<>();
            Set<String> fields = new HashSet<>();
            Set<String> constructors = new HashSet<>();

            while (i < json.length()) {
                i = skipWhitespace(json, i);
                if (i >= json.length()) break;
                c = json.charAt(i);
                if (c == '}') {
                    i++; // Skip '}'
                    break;
                }

                // Parse property name
                String propName = parseJsonString(json, i);
                if (propName == null) break;
                i += propName.length() + 2; // +2 for quotes

                i = skipWhitespace(json, i);
                if (i >= json.length() || json.charAt(i) != ':') break;
                i++; // Skip ':'

                i = skipWhitespace(json, i);
                if (i >= json.length()) break;

                if ("methods".equals(propName)) {
                    i = parseJsonArray(json, i, methods);
                } else if ("fields".equals(propName)) {
                    i = parseJsonArray(json, i, fields);
                } else if ("constructors".equals(propName)) {
                    i = parseJsonArray(json, i, constructors);
                } else if ("classAnnotated".equals(propName)) {
                    i = skipWhitespace(json, i);
                    if (i < json.length() && json.charAt(i) == 't') { // "true"
                        hasClassAnnotated = true;
                        i += 4;
                    }
                } else {
                    // Skip unknown property
                    i = skipJsonValue(json, i);
                }

                i = skipWhitespace(json, i);
                if (i < json.length() && json.charAt(i) == ',') {
                    i++; // Skip comma
                }
            }

            if (hasClassAnnotated) {
                CLASS_ANNOTATED.add(className);
            }
            if (!methods.isEmpty()) {
                CLASS_METHODS.put(className, methods);
            }
            if (!fields.isEmpty()) {
                CLASS_FIELDS.put(className, fields);
            }
            if (!constructors.isEmpty()) {
                CLASS_CONSTRUCTORS.put(className, constructors);
            }

            i = skipWhitespace(json, i);
            if (i < json.length() && json.charAt(i) == ',') {
                i++; // Skip comma between class entries
            }
        }
    }

    private static int skipWhitespace(String s, int i) {
        while (i < s.length() && Character.isWhitespace(s.charAt(i))) {
            i++;
        }
        return i;
    }

    private static String parseJsonString(String s, int i) {
        i = skipWhitespace(s, i);
        if (i >= s.length() || s.charAt(i) != '"') return null;
        i++; // Skip opening quote
        int start = i;
        while (i < s.length() && s.charAt(i) != '"') {
            if (s.charAt(i) == '\\') i += 2; // Skip escaped char
            else i++;
        }
        if (i >= s.length()) return null;
        return s.substring(start, i);
    }

    private static int parseJsonArray(String s, int i, Set<String> out) {
        i = skipWhitespace(s, i);
        if (i >= s.length() || s.charAt(i) != '[') return i;
        i++; // Skip '['

        while (i < s.length()) {
            i = skipWhitespace(s, i);
            if (i >= s.length()) break;
            if (s.charAt(i) == ']') {
                i++; // Skip ']'
                break;
            }

            String str = parseJsonString(s, i);
            if (str != null) {
                out.add(str);
                i += str.length() + 2; // +2 for quotes
            }

            i = skipWhitespace(s, i);
            if (i < s.length() && s.charAt(i) == ',') {
                i++; // Skip comma
            }
        }
        return i;
    }

    private static int skipJsonValue(String s, int i) {
        i = skipWhitespace(s, i);
        if (i >= s.length()) return i;
        char c = s.charAt(i);
        if (c == '"') {
            // String - skip to closing quote
            i++;
            while (i < s.length() && s.charAt(i) != '"') {
                if (s.charAt(i) == '\\') i += 2;
                else i++;
            }
            if (i < s.length()) i++; // Skip closing quote
        } else if (c == '{') {
            // Object - skip nested
            int depth = 1;
            i++;
            while (i < s.length() && depth > 0) {
                if (s.charAt(i) == '{') depth++;
                else if (s.charAt(i) == '}') depth--;
                else if (s.charAt(i) == '"') {
                    // Skip string inside object
                    i++;
                    while (i < s.length() && s.charAt(i) != '"') {
                        if (s.charAt(i) == '\\') i += 2;
                        else i++;
                    }
                    if (i < s.length()) i++;
                } else {
                    i++;
                }
            }
        } else if (c == '[') {
            // Array - skip nested
            int depth = 1;
            i++;
            while (i < s.length() && depth > 0) {
                if (s.charAt(i) == '[') depth++;
                else if (s.charAt(i) == ']') depth--;
                else if (s.charAt(i) == '"') {
                    i++;
                    while (i < s.length() && s.charAt(i) != '"') {
                        if (s.charAt(i) == '\\') i += 2;
                        else i++;
                    }
                    if (i < s.length()) i++;
                } else {
                    i++;
                }
            }
        } else {
            // Primitive (true, false, null, number) - skip to next comma/brace/bracket
            while (i < s.length()) {
                c = s.charAt(i);
                if (c == ',' || c == '}' || c == ']' || Character.isWhitespace(c)) break;
                i++;
            }
        }
        return i;
    }
    
    public static boolean canAccessClass(String className) {
        return CLASS_ANNOTATED.contains(className) 
            || CLASS_METHODS.containsKey(className)
            || CLASS_FIELDS.containsKey(className)
            || CLASS_CONSTRUCTORS.containsKey(className);
    }
    
    public static boolean canAccessMethod(String className, String methodName) {
        if (!canAccessClass(className)) return false;
        Set<String> methods = CLASS_METHODS.get(className);
        return methods != null && methods.contains(methodName);
    }
    
    public static boolean canAccessField(String className, String fieldName) {
        if (!canAccessClass(className)) return false;
        Set<String> fields = CLASS_FIELDS.get(className);
        return fields != null && fields.contains(fieldName);
    }
    
    public static boolean canAccessConstructor(String className) {
        if (!canAccessClass(className)) return false;
        Set<String> ctors = CLASS_CONSTRUCTORS.get(className);
        return ctors != null && ctors.contains("<init>");
    }
    
    public static void warnIfNotAllowed(String className, String member, String memberType) {
        if (!canAccessClass(className)) {
            String key = "class:" + className;
            if (LOGGED_WARNINGS.add(key)) { // Only log if not seen before
                LOG.warning("LuaSandbox: Mod attempted to access unregistered class '" + className
                    + "' (not annotated with @LuaInterface)");
            }
            return;
        }

        boolean allowed = false;
        if ("method".equals(memberType)) {
            allowed = canAccessMethod(className, member);
        } else if ("field".equals(memberType)) {
            allowed = canAccessField(className, member);
        } else if ("constructor".equals(memberType)) {
            allowed = canAccessConstructor(className);
        } else if ("class".equals(memberType)) {
            // For bindClass, just check if class is allowed
            allowed = true;
        }

        if (!allowed) {
            String key = memberType + ":" + className + ":" + member;
            if (LOGGED_WARNINGS.add(key)) { // Only log if not seen before
                LOG.warning("LuaSandbox: Mod attempted to access " + memberType + " '" + member
                    + "' on class '" + className + "' which is not exposed via @LuaInterface");
            }
        }
    }
}