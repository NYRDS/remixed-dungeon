package com.nyrds.platform.app;


import java.util.concurrent.atomic.AtomicInteger;

public class Notifications {

    static private AtomicInteger notificationId = new AtomicInteger((int)(System.currentTimeMillis()/1000));

    static public void displayNotification(String channelId, String title, String text) {
    }

    static private void createNotificationChannel(String channelId) {
    }
}
