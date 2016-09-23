package com.nyrds.pixeldungeon.support;

import android.view.ViewGroup;
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
					ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 50);
					adView.setLayoutParams(params);
					adView.getSettings().setUseWideViewPort(true);
					//adView.getSettings().setLoadWithOverviewMode(true);

					String html = "<head><meta name=\"viewport\" content=\"user-scalable=no, initial-scale=1, maximum-scale=1, minimum-scale=1, width=device-width, height=device-height, target-densitydpi=device-dpi\" /></head>" +
							"<div width=100%>Рекламко</div>" +
							"<div align=\"right\">.</div>                                               ";

					adView.loadDataWithBaseURL(null,html, "text/html", "utf-8", null);
					Game.instance().getLayout().addView(adView, 0);
					Game.setNeedSceneRestart(true);
				}
			}
		});
	}
}
