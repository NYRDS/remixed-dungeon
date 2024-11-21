package com.nyrds.platform.util;

import com.nyrds.platform.events.EventCollector;
import com.nyrds.util.Utils;

/**
 * Created by DeadDie on 18.03.2016
 */
public class TrackedRuntimeException extends RuntimeException {

	public TrackedRuntimeException( Exception e) {
		super(e);

		String message = e.getMessage();
		if(message == null) {
			EventCollector.logException(e,"exception with empty message");
		}

		EventCollector.logException(e, Utils.EMPTY_STRING);
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

