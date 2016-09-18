package com.nyrds.pixeldungeon.support;

import android.webkit.WebView;

import com.watabou.noosa.Game;

/**
 * Created by mike on 18.09.2016.
 */
public class OwnAds {

	public static void displayBanner() {
		Game.instance().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (Game.instance().getLayout().getChildCount() == 1) {
					WebView adView = new WebView(Game.instance());
					adView.loadDataWithBaseURL(null,"Рекламко", "text/html", "utf-8", null);
					Game.instance().getLayout().addView(adView, 0);
					Game.setNeedSceneRestart(true);
				}
			}
		});
	}
}
