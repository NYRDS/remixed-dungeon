package com.nyrds.android.util;

import com.nyrds.pixeldungeon.ml.EventCollector;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;

/**
 * Created by DeadDie on 18.03.2016
 */
public class TrackedRuntimeException extends RuntimeException {

	public TrackedRuntimeException( Exception e) {
		super(e);

		String message = e.getMessage();
		if(message == null) {
			message = "";
			EventCollector.logException(e,"exception with empty message");
		}
		Notifications.displayNotification(this.getClass().getSimpleName(), e.getClass().getSimpleName(), message);

		GLog.toFile(message);
		EventCollector.logException(e, Utils.EMPTY_STRING);
	}

	public TrackedRuntimeException( String s) {
		super(s);
		Notifications.displayNotification(this.getClass().getSimpleName(), s, s);
		GLog.toFile(s);

		EventCollector.logException(this,s);
	}

	public TrackedRuntimeException( String s,Exception e) {
		super(s,e);
		Notifications.displayNotification(this.getClass().getSimpleName(), s, e.getMessage());
		GLog.toFile(s);
		GLog.toFile(e.getMessage());
		EventCollector.logException(this,s);
	}

}

