package com.nyrds.platform.app;

import com.nyrds.util.ModdingMode;
import com.watabou.pixeldungeon.utils.GLog;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Abstracts filesystem access for the Web Server to handle
 * different platforms and mod resources consistently
 */
public class FilesystemAccess {
    
    /**
     * Get an input stream for reading a file/resource
     */
    public static InputStream getInputStream(String path) {
        // Try to get from mod resources first
        try {
            InputStream stream = ModdingMode.getInputStream(path);
            if (stream != null) {
                return stream;
            }
        } catch (Exception e) {
            GLog.debug("Failed to get input stream from ModdingMode for path: " + path + ", error: " + e.getMessage());
        }

        // For resources specifically, they might be located under assets/ in the built JAR
        // So if path is "html/template.html", look for "assets/html/template.html"
        String assetsPath = "assets/" + path;
        try {
            InputStream stream = FilesystemAccess.class.getClassLoader().getResourceAsStream(assetsPath);
            if (stream != null) {
                GLog.debug("Successfully loaded resource from: " + assetsPath);
                return stream;
            } else {
                GLog.debug("Resource not found at path: " + assetsPath);
            }
        } catch (Exception e) {
            GLog.debug("Failed to get input stream from class loader for path: " + assetsPath + ", error: " + e.getMessage());
        }

        // Also try the path as-is (in case files are stored at other locations in the JAR)
        try {
            InputStream stream = FilesystemAccess.class.getClassLoader().getResourceAsStream(path);
            if (stream != null) {
                GLog.debug("Successfully loaded resource from: " + path);
                return stream;
            } else {
                GLog.debug("Resource not found at path: " + path);
            }
        } catch (Exception e) {
            GLog.debug("Failed to get input stream from class loader for path: " + path + ", error: " + e.getMessage());
        }

        // Try with Thread's context class loader as well (sometimes needed in different environments)
        try {
            ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
            if (contextClassLoader != null) {
                InputStream stream = contextClassLoader.getResourceAsStream(assetsPath);
                if (stream != null) {
                    GLog.debug("Successfully loaded resource from context class loader: " + assetsPath);
                    return stream;
                } else {
                    GLog.debug("Resource not found at context class loader path: " + assetsPath);
                }
            }
        } catch (Exception e) {
            GLog.debug("Failed to get input stream from context class loader for path: " + assetsPath + ", error: " + e.getMessage());
        }

        // As a last resort, try with just the filename part if it's in a subdirectory
        // For example, if path is "html/template.html", try just "template.html"
        int lastSlashIndex = path.lastIndexOf('/');
        if (lastSlashIndex != -1) {
            String filename = path.substring(lastSlashIndex + 1);
            try {
                InputStream stream = FilesystemAccess.class.getClassLoader().getResourceAsStream(filename);
                if (stream != null) {
                    GLog.debug("Successfully loaded resource from filename only: " + filename);
                    return stream;
                } else {
                    GLog.debug("Resource not found at filename only: " + filename);
                }
            } catch (Exception e) {
                GLog.debug("Failed to get input stream from class loader for filename only: " + filename + ", error: " + e.getMessage());
            }
        }

        return null;
    }
    
    /**
     * Check if a resource exists at the given path
     */
    public static boolean resourceExists(String path) {
        // Try mod resources first
        if (ModdingMode.isResourceExist(path)) {
            return true;
        }
        
        // Check in classpath
        try (InputStream stream = FilesystemAccess.class.getClassLoader().getResourceAsStream("assets/" + path)) {
            return stream != null;
        } catch (Exception e) {
            // Try without assets prefix too
            try (InputStream stream = FilesystemAccess.class.getClassLoader().getResourceAsStream(path)) {
                return stream != null;
            } catch (Exception e2) {
                GLog.debug("Resource does not exist at path: " + path);
                return false;
            }
        }
    }
    
    /**
     * Check if a path represents a directory
     */
    public static boolean isDirectory(String path) {
        // For Remixed mod, check both assets and external files using ModdingMode
        if (ModdingMode.activeMod().equals(ModdingMode.REMIXED)) {
            // Try to list the path, if it has content it's a directory
            try {
                java.util.List<String> contents = ModdingMode.listResources(path, (dir, name) -> true);
                return contents != null && contents.size() > 0;
            } catch (Exception e) {
                GLog.debug("Error checking if path is directory in Remixed mod: " + path + ", error: " + e.getMessage());
            }
        }

        // For other mods, try to list resources as well
        try {
            java.util.List<String> contents = ModdingMode.listResources(path, (dir, name) -> true);
            return contents != null && contents.size() > 0;
        } catch (Exception e) {
            GLog.debug("Error checking if path is directory in other mod: " + path + ", error: " + e.getMessage());
        }

        return false;
    }
    
    /**
     * List contents of a directory for the current mod
     * Returns an array of names (not full paths) of direct children
     */
    public static String[] listDirectoryContents(String path) {
        try {
            // Get all resources from the mod that start with the given path
            java.util.List<String> resourceList = ModdingMode.listResources(path, (dir, name) -> true);

            // Filter the resources to only include those that are in the current path (not subdirectories)
            List<String> filteredList = new ArrayList<>();
            String currentPath = path.isEmpty() ? "" : path + "/";

            for (String resource : resourceList) {
                if (resource.startsWith(currentPath)) {
                    String relativePath = resource.substring(currentPath.length());
                    // Only include direct children, not nested items
                    if (!relativePath.contains("/")) {
                        filteredList.add(relativePath);
                    }
                }
            }

            GLog.debug("Found " + filteredList.size() + " items in directory: '" + path + "'");

            return filteredList.toArray(new String[0]);
        } catch (Exception e) {
            GLog.debug("Error listing directory contents: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Check if a specific item in a directory is itself a directory
     */
    public static boolean isDirectoryItem(String parentPath, String itemName) {
        // Check if the full path represents a directory
        String fullPath = parentPath.isEmpty() ? itemName : parentPath + "/" + itemName;
        return isDirectory(fullPath);
    }
}