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

import com.nyrds.pixeldungeon.game.GameLoop;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.nyrds.util.GuiProperties;
import com.watabou.noosa.Text;
import com.watabou.noosa.ui.Component;
import com.watabou.pixeldungeon.Chrome;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.ui.ScrollPane;
import com.watabou.pixeldungeon.ui.Window;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class WndStory extends Window {

	private static final int WIDTH  = 120;
	private static final int HEIGHT = 120;
	
	private static final int MARGIN = 6;
	
	private static final float bgR	= 0.77f;
	private static final float bgG	= 0.73f;
	private static final float bgB	= 0.62f;
	
	public static final int ID_SEWERS		= 0;
	public static final int ID_PRISON		= 1;
	public static final int ID_CAVES		= 2;
	public static final int ID_METROPOLIS	= 3;
	public static final int ID_HALLS		= 4;
	public static final int ID_SPIDERS		= 5;
	public static final int ID_GUTS         = 6;
	
	private static final Map<Integer,String> CHAPTERS = new HashMap<>();
	
	static {
		CHAPTERS.put(ID_SEWERS, StringsManager.getVar(R.string.WndStory_Sewers));
		CHAPTERS.put(ID_PRISON, StringsManager.getVar(R.string.WndStory_Prision));
		CHAPTERS.put(ID_CAVES, StringsManager.getVar(R.string.WndStory_Caves));
		CHAPTERS.put(ID_METROPOLIS, StringsManager.getVar(R.string.WndStory_Metropolis));
		CHAPTERS.put(ID_HALLS, StringsManager.getVar(R.string.WndStory_Halls));
		CHAPTERS.put(ID_SPIDERS, StringsManager.getVar(R.string.WndStory_Spiders));
		CHAPTERS.put(ID_GUTS, StringsManager.getVar(R.string.WndStory_Guts));
	}

	public WndStory( String text ) {
		super( 0, 0, Chrome.get( Chrome.Type.SCROLL ) );

		Text tf = PixelScene.createMultiline(StringsManager.maybeId(text), GuiProperties.regularFontSize());
		tf.maxWidth(WIDTH - MARGIN * 2);
		tf.ra = bgR;
		tf.ga = bgG;
		tf.ba = bgB;
		tf.rm = -bgR;
		tf.gm = -bgG;
		tf.bm = -bgB;
		tf.setX(MARGIN);
		
		int h = (int) Math.min(HEIGHT - MARGIN, tf.height());
		int w = (int)(tf.width() + MARGIN * 2);
		
		resize( w, h );
		
		Component content = new Component();
		
		content.add(tf);
		
		content.setSize(tf.width(), tf.height());
		
		ScrollPane list = new ScrollPane(content);
		add(list);

		list.setRect(0, 0, w, h);
	}

	public static void showCustomStory( @NotNull String text ) {
			GameLoop.addToScene( new WndStory( text ) );
	}
	
	public static void showChapter( int id ) {
		
		if (Dungeon.chapters.contains( id )) {
			return;
		}
		
		String text = CHAPTERS.get( id );
		if (text != null) {
			WndStory wnd = new WndStory( text );
			
			GameLoop.addToScene( wnd );
			
			Dungeon.chapters.add( id );
		}
	}
}
