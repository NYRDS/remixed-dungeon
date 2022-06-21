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
package com.watabou.pixeldungeon.windows;

import com.nyrds.util.GuiProperties;
import com.watabou.noosa.Image;
import com.watabou.noosa.Text;
import com.watabou.noosa.ui.Component;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.ui.Highlighter;
import com.watabou.pixeldungeon.ui.Window;

public class WndTitledMessage extends Window {

	public WndTitledMessage( Image icon, String title, String message ) {
		
		this( new IconTitle( icon, title ), message );
	}
	
	public WndTitledMessage( Component titlebar, String message ) {
		
		super();

		resizeLimited(STD_WIDTH);

		titlebar.setRect( 0, 0, width, 0 );
		add( titlebar );
		
		Highlighter hl = new Highlighter( message );

		Text normal = PixelScene.createMultiline(hl.text, GuiProperties.regularFontSize());
		if (hl.isHighlighted()) {
			normal.mask = hl.inverted();
		}
		
		normal.maxWidth(width);
		normal.setX(titlebar.left());
		normal.setY(titlebar.bottom() + 2*GAP);
		add(normal);

		if (hl.isHighlighted()) {
			Text highlighted = PixelScene.createMultiline(hl.text, GuiProperties.regularFontSize());
			highlighted.mask = hl.mask;
			highlighted.maxWidth(normal.getMaxWidth());
			highlighted.setX(normal.getX());
			highlighted.setY(normal.getY());
			add(highlighted);
			
			highlighted.hardlight(TITLE_COLOR);
		}
		
		resize( width, (int)(normal.getY() + normal.height()+GAP) );
	}
}
