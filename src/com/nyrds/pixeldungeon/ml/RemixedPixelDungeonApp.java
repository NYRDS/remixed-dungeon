package com.nyrds.pixeldungeon.ml;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

import android.app.Application;
import com.nyrds.pixeldungeon.ml.R;

@ReportsCrashes(mailTo = "nyrdsofficial@gmail.com", mode = ReportingInteractionMode.TOAST, resToastText = R.string.RemixedPixelDungeonApp_sendCrash)
public class RemixedPixelDungeonApp extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		ACRA.init(this);
	}
}
