package com.nyrds.platform.game;

import com.nyrds.LuaInterface;
import com.nyrds.pixeldungeon.game.GameLoop;
import com.nyrds.pixeldungeon.game.GamePreferences;
import com.nyrds.platform.util.PUtil;
import com.nyrds.util.ModdingMode;
import com.watabou.noosa.Scene;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.scenes.TitleScene;

// headless: mirrors the desktop platform game class
public class RemixedDungeon extends Game {

	public RemixedDungeon() {
		super(TitleScene.class);
	}

	public static boolean canDonate() {
		return false;
	}

	public static void landscape(boolean value) {
	}

	public static boolean landscape() {
		if (GameLoop.width <= 0 || GameLoop.height <= 0) {
			return true;
		}
		return GameLoop.width > GameLoop.height;
	}

	public static void updateImmersiveMode() {
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

	// Method for handling Lua callbacks by ID for item selection
	@LuaInterface
	public static void luaCallByGlobalId(int callbackId, Object... params) {
		GameLoop.callByGlobalId(callbackId, params);
	}

	public static void switchNoFade(Class<? extends PixelScene> c) {
		PixelScene.noFade = true;
		GameLoop.switchScene(c);
	}

	public static boolean isDev() {
		return false;
	}

	@Override
	public void create() {
		PUtil.slog("game", "Creating game (headless)");

		GamePreferences.classicFont(GamePreferences.classicFont());
		ModdingMode.selectMod(GamePreferences.activeMod());
		GamePreferences.uiLanguage(GamePreferences.uiLanguage());

		super.create();
	}
}
