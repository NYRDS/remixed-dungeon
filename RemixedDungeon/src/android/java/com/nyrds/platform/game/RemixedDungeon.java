/*
 * Pixel Dungeon
 * Copyright (C) 2012-2014  Oleg Dolya
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package com.nyrds.platform.game;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;

import com.nyrds.LuaInterface;
import com.nyrds.market.MarketOptions;
import com.nyrds.pixeldungeon.game.GameLoop;
import com.nyrds.pixeldungeon.game.GamePreferences;
import com.nyrds.pixeldungeon.support.AdsUtils;
import com.nyrds.pixeldungeon.support.EuConsent;
import com.nyrds.pixeldungeon.support.PlayGames;
import com.nyrds.platform.audio.Music;
import com.nyrds.platform.audio.Sample;
import com.nyrds.platform.storage.Preferences;
import com.nyrds.util.ModdingMode;
import com.nyrds.util.Util;
import com.watabou.noosa.Scene;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.scenes.TitleScene;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;

public class RemixedDungeon extends Game {

	public static final double[] MOVE_TIMEOUTS = new double[]{250, 500, 1000, 2000, 5000, 10000, 30000, 60000, Double.POSITIVE_INFINITY };
	private static boolean isDev = false;

	public RemixedDungeon() {
		super(TitleScene.class);

		// remix 0.5
		com.watabou.utils.Bundle.addAlias(
				com.watabou.pixeldungeon.items.food.Ration.class,
				"com.watabou.pixeldungeon.items.food.Food");
		// remix 23.1.alpha
		com.watabou.utils.Bundle.addAlias(
				com.nyrds.pixeldungeon.mobs.guts.SuspiciousRat.class,
				"com.nyrds.pixeldungeon.mobs.guts.Wererat");
		// remix 23.2.alpha
		com.watabou.utils.Bundle.addAlias(
				com.nyrds.pixeldungeon.items.guts.weapon.melee.Claymore.class,
				"com.nyrds.pixeldungeon.items.guts.weapon.melee.BroadSword");
		// remix 24
		com.watabou.utils.Bundle.addAlias(
				com.nyrds.pixeldungeon.items.accessories.Bowknot.class,
				"com.nyrds.pixeldungeon.items.accessories.BowTie");
		// remix 24
		com.watabou.utils.Bundle.addAlias(
				com.nyrds.pixeldungeon.items.accessories.Nightcap.class,
				"com.nyrds.pixeldungeon.items.accessories.SleepyHat");
		// remix 27.2.beta
		com.watabou.utils.Bundle.addAlias(
				com.nyrds.pixeldungeon.items.books.TomeOfKnowledge.class,
				"com.nyrds.pixeldungeon.items.books.SpellBook");

		com.watabou.utils.Bundle.addAlias(
				com.nyrds.pixeldungeon.mechanics.buffs.RageBuff.class,
			"com.watabou.pixeldungeon.items.quest.CorpseDust.UndeadRageAuraBuff"
		);

		com.watabou.utils.Bundle.addAlias(
				com.watabou.pixeldungeon.actors.mobs.FireElemental.class,
				"com.watabou.pixeldungeon.actors.mobs.Elemental"
		);

	}

    public static boolean isAlpha() {
        return version.contains("alpha") || isDev;
    }

	public static boolean isDev() {
		return isDev;
	}

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		isDev = version.contains("in_dev");
		
		EuConsent.check(this);
		playGames = new PlayGames();
    }

	@Override
	public void onResume() {
		super.onResume();

		GamePreferences.activeMod(ModdingMode.activeMod());

		//if(!Utils.canUseClassicFont(GamePreferences.uiLanguage())) {
			GamePreferences.classicFont(false);
		//}

		ModdingMode.setClassicTextRenderingMode(GamePreferences.classicFont());

		GamePreferences.setSelectedLanguage();

		updateImmersiveMode();

		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		boolean landscape = metrics.widthPixels > metrics.heightPixels;

		if (Preferences.INSTANCE.getBoolean(Preferences.KEY_LANDSCAPE, false) != landscape) {
			landscape(!landscape);
		}

		Music.INSTANCE.enable(GamePreferences.music());
		Sample.INSTANCE.enable(GamePreferences.soundFx());

		if (Preferences.INSTANCE.getBoolean(Preferences.KEY_USE_PLAY_GAMES, false)) {
			playGames.connect();
		}

		AdsUtils.initRewardVideo();
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);

		if (hasFocus) {
			updateImmersiveMode();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		String extras = Utils.EMPTY_STRING;
		if(data!=null) {
			extras = Util.bundle2string(data.getExtras());
		}

		GLog.debug("onActivityResult(" + requestCode + "," + resultCode + "," + data +" "+extras);

		if(playGames.onActivityResult(requestCode, resultCode, data)) {
			return;
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	public static void switchNoFade(Class<? extends PixelScene> c) {
		PixelScene.noFade = true;
		GameLoop.switchScene(c);
	}

	public static boolean canDonate() {
		return MarketOptions.haveDonations() && Game.instance().iap.isReady() || Util.isDebug();
	}
	
	/*
	 * ---> Android Preferences
	 */

	public static void landscape(boolean value) {
		Game.instance()
				.setRequestedOrientation(value ? ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
						: ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		Preferences.INSTANCE.put(Preferences.KEY_LANDSCAPE, value);
	}

	public static boolean storedLandscape() {
		return Preferences.INSTANCE.getBoolean(Preferences.KEY_LANDSCAPE, false);
	}

	public static boolean landscape() {
		return width() > height();
	}


	@SuppressLint("NewApi")
	public static void updateImmersiveMode() {
		if (android.os.Build.VERSION.SDK_INT >= 19) {
			if (instance() != null) {
				instance().getWindow()
						.getDecorView()
						.setSystemUiVisibility(
								GamePreferences.immersed() ? View.SYSTEM_UI_FLAG_LAYOUT_STABLE
										| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
										| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
										| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
										| View.SYSTEM_UI_FLAG_FULLSCREEN
										| View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
										: 0);
			}
		}
	}

	//Still here for lua scripts compatibility
	@LuaInterface
	public static Scene scene() {
		return GameLoop.scene();
	}

	@LuaInterface
	public static float getDifficultyFactor() {
		return GameLoop.getDifficultyFactor();
	}

	@LuaInterface
	public static void resetScene() {
		GameLoop.resetScene();
	}

}