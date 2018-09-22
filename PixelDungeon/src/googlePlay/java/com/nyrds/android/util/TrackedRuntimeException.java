package com.nyrds.android.util;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.nyrds.pixeldungeon.ml.EventCollector;
import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.utils.GLog;

/**
 * Created by DeadDie on 18.03.2016
 */
public class TrackedRuntimeException extends RuntimeException {

	static private String ChannelId = "Remixed Dungeon Errors";

	public TrackedRuntimeException( Exception e) {
		super(e);

		createNotificationChannel();
		NotificationCompat.Builder builder = new NotificationCompat.Builder(Game.instance(),ChannelId)
				.setSmallIcon(R.drawable.ic_launcher)
				.setContentTitle("RD oops...")
				.setContentText(e.getMessage())
				.setStyle(new NotificationCompat.BigTextStyle().bigText(e.getMessage()))
				.setPriority(NotificationCompat.PRIORITY_DEFAULT);

		NotificationManagerCompat notificationManager = NotificationManagerCompat.from(Game.instance());
		notificationManager.notify(1, builder.build());

		GLog.toFile(e.getMessage());
		EventCollector.logException(e,"");
	}

	public TrackedRuntimeException( String s) {
		super(s);
		EventCollector.logException(this,s);
	}

	public TrackedRuntimeException( String s,Exception e) {
		super(s,e);
		EventCollector.logException(this,s);
	}

	static private void createNotificationChannel() {
		// Create the NotificationChannel, but only on API 26+ because
		// the NotificationChannel class is new and not in the support library
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			int importance = NotificationManager.IMPORTANCE_DEFAULT;
			NotificationChannel channel = new NotificationChannel(ChannelId, ChannelId, importance);
			channel.setDescription(ChannelId);
			// Register the channel with the system; you can't change the importance
			// or other notification behaviors after this
			NotificationManager notificationManager = Game.instance().getSystemService(NotificationManager.class);
			notificationManager.createNotificationChannel(channel);
		}
	}

}

