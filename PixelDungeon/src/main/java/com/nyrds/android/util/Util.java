package com.nyrds.android.util;

import com.nyrds.pixeldungeon.ml.EventCollector;

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

	static public String toString(Exception e) {
		return e.getMessage() + "\n" + Util.stackTraceToString(e) + "\n";
	}

	static public void storeEventInAcra(String eventKey,Exception e) {
		EventCollector.logException(e);
		if(!ACRA.isInitialised()){
			return;
		}
		ACRA.getErrorReporter().putCustomData(eventKey, toString(e));
	}

	static public void storeEventInAcra(String eventKey,String str) {
		if(!ACRA.isInitialised()){
			return;
		}
		ACRA.getErrorReporter().putCustomData(eventKey, str);
	}

}
