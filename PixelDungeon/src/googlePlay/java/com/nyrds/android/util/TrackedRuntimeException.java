package com.nyrds.android.util;

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

	public TrackedRuntimeException( Exception e) {
		super(e);

		NotificationCompat.Builder builder = new NotificationCompat.Builder(Game.instance(), "Remixed Dungeon")
				.setSmallIcon(R.drawable.ic_launcher)
				.setContentTitle("oops...")
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
}
