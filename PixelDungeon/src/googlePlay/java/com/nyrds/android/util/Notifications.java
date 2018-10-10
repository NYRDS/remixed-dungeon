package com.nyrds.android.util;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.ml.RemixedPixelDungeonApp;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.utils.GLog;

public class Notifications {

    static public void displayNotification(String channelId, String title, String text) {

        Notifications.createNotificationChannel(channelId);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(FileSystem.getExternalStorageFile(GLog.RE_PD_LOG_FILE_LOG)), "text/plain");

        PendingIntent viewLogIntent =
                PendingIntent.getBroadcast(RemixedPixelDungeonApp.getContext(), 0, intent, 0);


        NotificationCompat.Builder builder = new NotificationCompat.Builder(Game.instance(), channelId)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(title)
                .setContentText(text)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(text))
                .setContentIntent(viewLogIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(Game.instance());
        notificationManager.notify(1, builder.build());
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
