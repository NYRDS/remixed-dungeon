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

import com.nyrds.android.util.TrackedRuntimeException;
import com.nyrds.retrodungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.PixelDungeon;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.scenes.InterlevelScene;
import com.watabou.pixeldungeon.scenes.RankingsScene;
import com.watabou.pixeldungeon.scenes.TitleScene;
import com.watabou.pixeldungeon.ui.Icons;
import com.watabou.pixeldungeon.ui.RedButton;
import com.watabou.pixeldungeon.ui.Window;

public class WndGame extends Window {

	private static final String TXT_SETTINGS = Game
			.getVar(R.string.WndGame_Settings);
	private static final String TXT_CHALLEGES = Game
			.getVar(R.string.WndGame_Challenges);
	private static final String TXT_RANKINGS = Game
			.getVar(R.string.WndGame_Ranking);
	private static final String TXT_START = Game.getVar(R.string.WndGame_Start);
	private static final String TXT_MENU = Game.getVar(R.string.WndGame_menu);
	private static final String TXT_EXIT = Game.getVar(R.string.WndGame_Exit);
	private static final String TXT_RETURN = Game
			.getVar(R.string.WndGame_Return);

	private static final int WIDTH = 120;

	private int pos;

	public WndGame() {
		
		super();
		
		addButton( new RedButton( TXT_SETTINGS ) {
			@Override
			protected void onClick() {
				hide();
				GameScene.show( new WndSettingsInGame() );
			}
		} );
		
		
		if(Dungeon.hero.getDifficulty() < 2 && Dungeon.hero.isAlive()) {
			addButton( new RedButton( Game.getVar(R.string.WndGame_Save) ) {
				@Override
				protected void onClick() {
					GameScene.show(new WndSaveSlotSelect(true,Game.getVar(R.string.WndSaveSlotSelect_SelectSlot)));
				}
			} );
		}
		
		if(Dungeon.hero.getDifficulty() < 2) {
			addButton( new RedButton( Game.getVar(R.string.WndGame_Load) ) {
				@Override
				protected void onClick() {
					GameScene.show(new WndSaveSlotSelect(false,Game.getVar(R.string.WndSaveSlotSelect_SelectSlot)));
				}
			} );
		}
		
		if (Dungeon.challenges > 0) {
			addButton( new RedButton( TXT_CHALLEGES ) {
				@Override
				protected void onClick() {
					hide();
					GameScene.show( new WndChallenges( Dungeon.challenges, false ) );
				}
			} );
		}
		
		if (!Dungeon.hero.isAlive()) {
			
			RedButton btnStart;
			addButton( btnStart = new RedButton( TXT_START ) {
				@Override
				protected void onClick() {
					Dungeon.hero = null;
					PixelDungeon.challenges( Dungeon.challenges );
					InterlevelScene.mode = InterlevelScene.Mode.DESCEND;
					InterlevelScene.noStory = true;
					Game.switchScene( InterlevelScene.class );
				}
			} );
			btnStart.icon( Icons.get( Dungeon.hero.heroClass ) );
			
			addButton( new RedButton( TXT_RANKINGS ) {
				@Override
				protected void onClick() {
					InterlevelScene.mode = InterlevelScene.Mode.DESCEND;
					Game.switchScene( RankingsScene.class );
				}
			} );
		}
				
		addButton( new RedButton( TXT_MENU ) {
			@Override
			protected void onClick() {
				try {
					Dungeon.saveAll();
				} catch (Exception e) {
					throw new TrackedRuntimeException(e);
				}
				Game.switchScene( TitleScene.class );
			}
		} );
		
		addButton( new RedButton( TXT_EXIT ) {
			@Override
			protected void onClick() {
				Game.shutdown();
			}
		} );
		
		addButton( new RedButton( TXT_RETURN ) {
			@Override
			protected void onClick() {
				hide();
			}
		} );
		
		resize( WIDTH, pos );
	}

	private void addButton(RedButton btn) {
		add(btn);
		btn.setRect(0, pos > 0 ? pos += GAP : 0, WIDTH, BUTTON_HEIGHT);
		pos += BUTTON_HEIGHT;
	}
}
