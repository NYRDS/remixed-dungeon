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
import com.watabou.noosa.Text;
import com.watabou.noosa.Visual;
import com.watabou.noosa.ui.Component;
import com.watabou.pixeldungeon.Logbook;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.sprites.CharSprite;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.utils.Signal;

import java.util.regex.Pattern;

public class GameLog extends Component implements Signal.Listener<String> {

	private static final int MAX_MESSAGES = 3;
	
	private static final Pattern PUNCTUATION = Pattern.compile( ".*[.,;?! ]$" );
	
	private Text lastEntry;
	private int lastColor;
	
	public GameLog() {
		super();
		GLog.update.add( this );
		
		newLine();
	}
	
	public void newLine() {
		lastEntry = null;
	}

	@Override
	public void onSignal(String text ) {
		if(Game.isPaused()) {
			return;
		}

		int color = CharSprite.DEFAULT;
		if (text.startsWith( GLog.POSITIVE )) {
			text = text.substring( GLog.POSITIVE.length() );
			color = CharSprite.POSITIVE;
		} else if (text.startsWith( GLog.NEGATIVE )) {
			text = text.substring( GLog.NEGATIVE.length() );
			color = CharSprite.NEGATIVE;
		} else if (text.startsWith( GLog.WARNING )) {
			text = text.substring( GLog.WARNING.length() );
			color = CharSprite.WARNING;
		} else if (text.startsWith( GLog.HIGHLIGHT )) {
			text = text.substring( GLog.HIGHLIGHT.length() );
			color = CharSprite.NEUTRAL;
		}

		if(text.isEmpty()) {
			return;
		}

		text = Utils.capitalize( text ) + 
			(PUNCTUATION.matcher( text ).matches() ? Utils.EMPTY_STRING : ".");

		Logbook.addPlayerLogMessage( text, color );	// Store the message to show in log book tab

		if (lastEntry != null && color == lastColor && lastEntry.lines()<3) {
			
			String lastMessage = lastEntry.text();
			lastEntry.text( lastMessage.length() == 0 ? text : lastMessage + " " + text );

		} else {
			lastEntry = PixelScene.createMultiline( text, GuiProperties.regularFontSize());
			lastEntry.maxWidth((int)width);
			lastEntry.hardlight( color );
			lastColor = color;
			add( lastEntry );
			
		}
		
		int texts = 0;
		for (int i = 0; i< getLength(); i++) {
			if(members.get( i ) instanceof Text)
				texts++;
		}
		
		if (texts > MAX_MESSAGES) {
			for(int i = 0; i< getLength(); i++) {
				if(members.get( i ) instanceof Text) {
					Text txt = (Text) members.get( i );
					remove ( txt );
					txt.destroy();
					break;
				}
			}
		}
		
		layout();
	}
	
	@Override
	protected void layout() {
		float pos = y;
		for (int i = getLength() -1; i >= 0; i--) {
			Visual entry = (Visual) members.get( i );
			if(entry instanceof Text) {
				entry.setX(x);
				entry.setY(pos - entry.height());
				pos = entry.getY();
			}
		}
	}
	
	@Override
	public void destroy() {
		GLog.update.remove( this );
		super.destroy();
	}
}
