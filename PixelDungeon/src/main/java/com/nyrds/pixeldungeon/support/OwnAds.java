package com.nyrds.pixeldungeon.support;

import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.LinearLayout;

import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.utils.Utils;

/**
 * Created by mike on 18.09.2016.
 */
public class OwnAds {

	private static final String adTemplate = "<head><meta name=\"viewport\" content=\"user-scalable=no, initial-scale=1, maximum-scale=1, minimum-scale=1, width=device-width, height=device-height, target-densitydpi=device-dpi\" /></head>" +
			"<div width=100%%>%s</div>" +
			"<div align=\"right\">.</div>";

	public static void displayBanner() {
		Game.instance().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				LinearLayout layout = Game.instance().getLayout();
				if (layout.getChildCount() == 1) {
					WebView adView = new WebView(Game.instance());

					int adViewHeight = Math.max(50,layout.getHeight()/10);

					ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, adViewHeight);
					adView.setLayoutParams(params);

/*
					String html = "<head><meta name=\"viewport\" content=\"user-scalable=no, initial-scale=1, maximum-scale=1, minimum-scale=1, width=device-width, height=device-height, target-densitydpi=device-dpi\" /></head>" +
							"<div width=100%>Рекламко</div>" +
							"<div align=\"right\">.</div>                                               ";
*/
					adView.loadDataWithBaseURL(null, Utils.format(adTemplate,"Рекламко"), "text/html", "utf-8", null);
					Game.instance().getLayout().addView(adView, 0);
					Game.setNeedSceneRestart(true);
				}
			}
		});
	}
}
