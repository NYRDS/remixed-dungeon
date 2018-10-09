package com.nyrds.android.util;

import com.nyrds.pixeldungeon.ml.EventCollector;
import com.watabou.pixeldungeon.utils.GLog;

/**
 * Created by DeadDie on 18.03.2016
 */
public class TrackedRuntimeException extends RuntimeException {

	public TrackedRuntimeException( Exception e) {
		super(e);

		Notifications.displayNotification(this.getClass().getSimpleName(), e.getClass().getSimpleName(), e.getMessage());

		GLog.toFile(e.getMessage());
		EventCollector.logException(e,"");
	}

	public TrackedRuntimeException( String s) {
		super(s);
		Notifications.displayNotification(this.getClass().getSimpleName(), s, s);

		EventCollector.logException(this,s);
	}

	public TrackedRuntimeException( String s,Exception e) {
		super(s,e);
		Notifications.displayNotification(this.getClass().getSimpleName(), s, e.getMessage());

		EventCollector.logException(this,s);
	}

}

