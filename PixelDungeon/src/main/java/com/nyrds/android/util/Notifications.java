package com.nyrds.android.util;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;

public class Notifications {

    static public void displayNotification(String channelId, String title, String text) {

        Notifications.createNotificationChannel(channelId);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(Game.instance(), channelId)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(title)
                .setContentText(text)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(text))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(Game.instance());
        notificationManager.notify(1, builder.build());
    }

    static void createNotificationChannel(String channelId) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, channelId, NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(channelId);
            NotificationManager notificationManager = Game.instance().getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
