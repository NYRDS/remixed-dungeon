package com.nyrds.platform.app;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.game.Game;

import java.util.concurrent.atomic.AtomicInteger;

public class Notifications {

    static private final AtomicInteger notificationId = new AtomicInteger((int)(System.currentTimeMillis()/1000));

    static public void displayNotification(String channelId, String title, String text) {

        Notifications.createNotificationChannel(channelId);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(Game.instance(), channelId)
                .setSmallIcon(R.drawable.notification_ic)
                .setContentTitle(title)
                .setContentText(text)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(text))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        Notification notification = builder.build();
        notification.flags |= Notification.FLAG_AUTO_CANCEL;

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(Game.instance());
        notificationManager.notify(notificationId.getAndIncrement(), notification);
    }

    static private void createNotificationChannel(String channelId) {
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
