package com.nyrds.pixeldungeon.support;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.LinearLayout;

import com.nyrds.pixeldungeon.ml.BuildConfig;
import com.watabou.noosa.Game;
import com.watabou.noosa.InterstitialPoint;
import com.watabou.pixeldungeon.utils.Utils;

/**
 * Created by mike on 18.09.2016.
 */
class OfflineAds {

	private static final String adTemplate = "<head><meta name=\"viewport\" content=\"user-scalable=no, initial-scale=1, maximum-scale=1, minimum-scale=1, width=device-width, height=device-height, target-densitydpi=device-dpi\" /></head>" +
			"<div width=100%%>%s</div>" +
			"<div align=\"right\">.</div>";


	private static final String isAdTemplate = "<head><meta name=\"viewport\" content=\"user-scalable=no, initial-scale=1, maximum-scale=1, minimum-scale=1, width=device-width, height=device-height, target-densitydpi=device-dpi\" /></head>" +
			"<div width=100%% height=100%%>%s</div>" +
			"<div align=\"right\"height=100%%>.</div>";

	static void displayBanner() {
		if (BuildConfig.DEBUG) {
			Game.instance().runOnUiThread(new Runnable() {
				@Override
				public void run() {
					LinearLayout layout = Game.instance().getLayout();
					if (layout.getChildCount() == 1) {

						WebView adView = new WebView(Game.instance());

						int adViewHeight = Math.max(50, layout.getHeight() / 10);

						ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, adViewHeight);
						adView.setLayoutParams(params);

						adView.loadDataWithBaseURL(null, Utils.format(adTemplate, "Рекламко"), "text/html", "utf-8", null);
						Game.instance().getLayout().addView(adView, 0);
						Game.setNeedSceneRestart(true);
					}
				}
			});
		}
	}

	static void displayIsAd(final InterstitialPoint work) {
		if (BuildConfig.DEBUG) {
			Game.instance().runOnUiThread(new Runnable() {

				                              @Override
				                              public void run() {

					                              final AlertDialog.Builder alert = new AlertDialog.Builder(Game.instance());

					                              WebView adView = new WebView(Game.instance());

					                              ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
					                              adView.setLayoutParams(params);

					                              adView.loadDataWithBaseURL(null, Utils.format(isAdTemplate, "Рекламко Рекламко Рекламко Рекламко Рекламко Рекламко Рекламко Рекламко Рекламко Рекламко Рекламко Рекламко Рекламко Рекламко"), "text/html", "utf-8", null);
					                              alert.setView(adView);
					                              alert.setCustomTitle(null);

					                              final AlertDialog dialog = alert.create();

					                              dialog.setCanceledOnTouchOutside(true);
					                              dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
						                              @Override
						                              public void onCancel(DialogInterface dialog) {
							                              dialog.dismiss();
							                              work.returnToWork(true);
						                              }
					                              });
					                              dialog.show();
				                              }
			                              }
			);
		} else {
			work.returnToWork(true);
		}
	}
}
