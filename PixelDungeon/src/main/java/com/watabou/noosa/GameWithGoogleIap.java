package com.watabou.noosa;

import com.nyrds.pixeldungeon.ml.EventCollector;
import com.nyrds.pixeldungeon.support.Ads;
import com.nyrds.pixeldungeon.support.Iap;
import com.watabou.pixeldungeon.Preferences;

public abstract class GameWithGoogleIap extends Game {

	static Iap mIap;
	static Ads mAds;

	public GameWithGoogleIap(Class<? extends Scene> c) {
		super(c);

		mIap = new Iap(this);
		mAds = new Ads();

		if(googleAnalyticsUsable()) {
			initEventCollector();
		}
	}

	@Override
	public void initIap() {
		mIap.initIap();
	}


	private static boolean googleAnalyticsUsable() {
		if(Preferences.INSTANCE.getInt(Preferences.KEY_COLLECT_STATS,0) > 0) {
			return android.os.Build.VERSION.SDK_INT >= 9;
		}
		return false;
	}

	@Override
	public void initEventCollector() {
		if(googleAnalyticsUsable()) {
			EventCollector.init(this);
		} else {
			EventCollector.disable();
		}
	}

	public abstract void setDonationLevel(int level);

	public static GameWithGoogleIap instance() {
		return (GameWithGoogleIap) Game.instance();
	}
}
