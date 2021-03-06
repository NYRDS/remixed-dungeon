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

import com.nyrds.platform.game.Game;
import com.nyrds.util.GuiProperties;
import com.watabou.pixeldungeon.Chrome;
import com.watabou.pixeldungeon.scenes.PixelScene;

public class RedButton extends TextButton {

	public RedButton( int labelStringId ) {
		this(Game.getVar(labelStringId));
	}

	public RedButton( String label ) {
		super(label);
	}
	
	@Override
	protected void createChildren() {
		super.createChildren();
		
		bg = Chrome.get( Chrome.Type.BUTTON );
		add( bg );

		text = PixelScene.createText(GuiProperties.titleFontSize());
		add( text );
	}

	public void regenText() {
		String txt = text.text();
		text.destroy();

		text = PixelScene.createText(GuiProperties.titleFontSize());
		text.text(txt);
		add( text );
		layout();
	}
}
