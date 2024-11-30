package com.nyrds.platform.util;

import com.nyrds.platform.EventCollector;
import com.nyrds.platform.app.Notifications;
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
		GLog.toFile(message);
		EventCollector.logException(e, Utils.EMPTY_STRING);
		Notifications.displayNotification(this.getClass().getSimpleName(), e.getClass().getSimpleName(), message);
	}

	public TrackedRuntimeException( String s) {
		super(s);
		GLog.toFile(s);
		EventCollector.logException(this,s);
		Notifications.displayNotification(this.getClass().getSimpleName(), s, s);
	}

	public TrackedRuntimeException( String s,Exception e) {
		super(s,e);
		GLog.toFile(s);
		String message = e.getMessage();

		if(message==null) {
			message=Utils.EMPTY_STRING;
		}

		GLog.toFile(message);
		EventCollector.logException(this,s);
		Notifications.displayNotification(this.getClass().getSimpleName(), s, message);
	}

}

