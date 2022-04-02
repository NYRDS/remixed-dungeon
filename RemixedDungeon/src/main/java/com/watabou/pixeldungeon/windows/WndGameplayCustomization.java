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

import com.nyrds.pixeldungeon.game.GamePreferences;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.nyrds.util.GuiProperties;
import com.watabou.noosa.Text;
import com.watabou.pixeldungeon.Challenges;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.ui.CheckBox;
import com.watabou.pixeldungeon.ui.Window;

import java.util.ArrayList;

public class WndGameplayCustomization extends Window {

	private static final int WIDTH		= 108;

	private boolean editable;
	private ArrayList<CheckBox> boxes;
	
	public WndGameplayCustomization(int checked, boolean editable ) {
		
		super();

		this.editable = editable;

        Text title = PixelScene.createText(StringsManager.getVar(R.string.WndChallenges_Title), GuiProperties.titleFontSize() );
		title.hardlight( TITLE_COLOR );
		title.setX(PixelScene.align( camera, (WIDTH - title.width()) / 2 ));
		add( title );
		
		boxes = new ArrayList<>();
		
		float pos = title.height() + GAP;
		final String[] challenges = StringsManager.getVars(R.array.Challenges_Names);

		for (int i=0; i < Challenges.MASKS.length; i++) {

			CheckBox cb = new CheckBox( challenges[i] );
			cb.checked( (checked & Challenges.MASKS[i]) != 0 );
			cb.active = editable;

			if (i > 0) {
				pos += GAP;
			}
			cb.setRect( 0, pos, WIDTH, BUTTON_HEIGHT );
			pos = cb.bottom();
			
			add( cb );
			boxes.add( cb );
		}
		
		resize( WIDTH, (int)pos );
	}
	
	@Override
	public void onBackPressed() {
		
		if (editable) {
			int value = 0;
			for (int i=0; i < boxes.size(); i++) {
				if (boxes.get( i ).checked()) {
					value |= Challenges.MASKS[i];
				}
			}
			Dungeon.setChallenges(value);
		}

		super.onBackPressed();
	}
}
