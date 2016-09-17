package com.nyrds.pixeldungeon.support;

import android.widget.Button;

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
					Button adView = new Button(Game.instance());
					adView.setText("Рекламко");
					Game.instance().getLayout().addView(adView, 0);
					Game.setNeedSceneRestart(true);
				}
			}
		});
	}
}
