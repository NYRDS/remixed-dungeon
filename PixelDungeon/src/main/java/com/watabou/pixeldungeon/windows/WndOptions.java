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

import java.util.ArrayList;

import com.watabou.noosa.Text;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.ui.RedButton;
import com.watabou.pixeldungeon.ui.SystemRedButton;
import com.watabou.pixeldungeon.ui.TextButton;
import com.watabou.pixeldungeon.ui.Window;

public class WndOptions extends Window {

	private static final int WIDTH			= 120;
	private static final int MARGIN 		= 2;
	private static final int BUTTON_HEIGHT	= 20;
	
	private ArrayList<TextButton> buttons = new ArrayList<>();
	
	public WndOptions( String title, String message, boolean systemFont, String... options ) {
		super();
		
		Text tfTitle = PixelScene.createSystemText( title, 9 );
		tfTitle.hardlight( TITLE_COLOR );
		tfTitle.x = tfTitle.y = MARGIN;
		tfTitle.maxWidth(WIDTH - MARGIN * 2);
		tfTitle.measure();
		add( tfTitle );
		
		Text tfMesage = PixelScene.createSystemText( message, 8 );
		tfMesage.maxWidth(WIDTH - MARGIN * 2);
		tfMesage.measure();
		tfMesage.x = MARGIN;
		tfMesage.y = tfTitle.y + tfTitle.height() + MARGIN;
		add( tfMesage );
		
		float pos = tfMesage.y + tfMesage.height() + MARGIN;
		
		for (int i=0; i < options.length; i++) {
			final int index = i;
			SystemRedButton btn = new SystemRedButton( options[i]) {
				@Override
				protected void onClick() {
					hide();
					onSelect( index );
				}
			};
			buttons.add(btn);
			btn.setRect( MARGIN, pos, WIDTH - MARGIN * 2, BUTTON_HEIGHT );
			add( btn );
			
			pos += BUTTON_HEIGHT + MARGIN;
		}
		
		resize( WIDTH, (int)pos );
	}

	
	public WndOptions( String title, String message, String... options ) {
		super();
		
		Text tfTitle = PixelScene.createMultiline( title, 9 );
		tfTitle.hardlight( TITLE_COLOR );
		tfTitle.x = tfTitle.y = MARGIN;
		tfTitle.maxWidth(WIDTH - MARGIN * 2);
		tfTitle.measure();
		add( tfTitle );
		
		Text tfMesage = PixelScene.createMultiline( message, 8 );
		tfMesage.maxWidth(WIDTH - MARGIN * 2);
		tfMesage.measure();
		tfMesage.x = MARGIN;
		tfMesage.y = tfTitle.y + tfTitle.height() + MARGIN;
		add( tfMesage );
		
		float pos = tfMesage.y + tfMesage.height() + MARGIN;
		
		for (int i=0; i < options.length; i++) {
			final int index = i;
			RedButton btn = new RedButton( options[i] ) {
				@Override
				protected void onClick() {
					hide();
					onSelect( index );
				}
			};
			buttons.add(btn);
			btn.setRect( MARGIN, pos, WIDTH - MARGIN * 2, BUTTON_HEIGHT );
			add( btn );
			
			pos += BUTTON_HEIGHT + MARGIN;
		}
		
		resize( WIDTH, (int)pos );
	}
	
	public void setEnabled(int i, boolean enabled) {
		buttons.get(i).enable(enabled);
	}
	
	protected void onSelect( int index ) {};
}
