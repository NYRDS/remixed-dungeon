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

import com.nyrds.LuaInterface;
import com.nyrds.pixeldungeon.game.GameLoop;
import com.watabou.noosa.Scene;
import com.watabou.pixeldungeon.scenes.TitleScene;
import com.watabou.pixeldungeon.utils.GLog;

public class RemixedDungeon extends Game {

	public RemixedDungeon() {
		super(TitleScene.class);
	}


	public static boolean canDonate() {
		return false;
	}


	public static void landscape(boolean value) {
	}

	public static boolean storedLandscape() {
		return true;
	}

	public static boolean landscape() {
		return false;
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


	@Override
	public void create() {
		GLog.debug("create");
		super.create();
	}

	@Override
	public void resize(int width, int height) {
		GLog.debug("resize: %dx%d", width, height);
		super.resize(width, height);
	}

	@Override
	public void render() {
		GLog.debug("frame");
		super.render();
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