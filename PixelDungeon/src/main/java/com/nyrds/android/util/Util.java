package com.nyrds.android.util;

import org.acra.ACRA;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Created by mike on 01.03.2016.
 */
public class Util {
	static public String stackTraceToString(Throwable e) {
		StringWriter sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw));
		return sw.toString();
	}

	static public void storeEventInAcra(String eventKey,Exception e) {
		ACRA.getErrorReporter().putCustomData(eventKey, e.getMessage() + "\n" + Util.stackTraceToString(e) + "\n");
	}

	static public void storeEventInAcra(String eventKey,String str) {
		ACRA.getErrorReporter().putCustomData(eventKey, str);
	}

}
