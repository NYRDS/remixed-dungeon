package com.nyrds.platform.app;

/**
 * HTML version of Notifications
 */
public class Notifications {
    
    public static void show(String title, String text) {
        // Simple implementation for HTML version - no notifications
        System.out.println("Notification: " + title + " - " + text);
    }
    
    public static void cancelAll() {
        // Simple implementation for HTML version - no notifications
    }
    
    // Additional method needed for HTML version
    public static void displayNotification(String title, String text, String errorMsg) {
        // Simple implementation for HTML version - no notifications
        System.out.println("Notification: " + title + " - " + text + " - " + errorMsg);
    }
}