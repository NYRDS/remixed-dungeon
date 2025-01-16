package com.nyrds.platform.game;

import com.badlogic.gdx.Gdx;
import com.nyrds.LuaInterface;
import com.nyrds.pixeldungeon.game.GameLoop;
import com.nyrds.pixeldungeon.game.GamePreferences;
import com.nyrds.platform.util.PUtil;
import com.nyrds.util.ModdingMode;
import com.watabou.noosa.Scene;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.scenes.TitleScene;
import com.watabou.pixeldungeon.utils.GLog;

import java.util.Arrays;

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
		return true;
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


	public static void switchNoFade(Class<? extends PixelScene> c) {
		PixelScene.noFade = true;
		GameLoop.switchScene(c);
	}

	public static boolean isDev() {
		return false;
	}


	@Override
	public void create() {
		PUtil.slog("game", "Creating game");
		PUtil.slog("game", "audio devices: " + Arrays.toString(Gdx.audio.getAvailableOutputDevices()));

		GamePreferences.classicFont(GamePreferences.classicFont());
		ModdingMode.selectMod(GamePreferences.activeMod());
		GamePreferences.uiLanguage(GamePreferences.uiLanguage());

		updateFpsLimit();

		super.create();
	}

	@Override
	public void pause() {
		GLog.debug("pause");
		super.pause();
	}

	@Override
	public void resume() {
		GLog.debug("resume");
		super.resume();
	}

	@Override
	public void dispose() {
		GLog.debug("dispose");
		super.dispose();
	}
}