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
package com.watabou.pixeldungeon.ui;

import com.nyrds.pixeldungeon.game.GameLoop;
import com.nyrds.platform.game.Game;
import com.watabou.pixeldungeon.scenes.TitleScene;

public class ExitButton extends ImageButton {

	public ExitButton() {
		super(Icons.EXIT.get());
	}

	@Override
	protected void onClick() {
		if (GameLoop.scene() instanceof TitleScene) {
			Game.shutdown();
		} else {
			GameLoop.switchNoFade( TitleScene.class );
		}
	}
}
