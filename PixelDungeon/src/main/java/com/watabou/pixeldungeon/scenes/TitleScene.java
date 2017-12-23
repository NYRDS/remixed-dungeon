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
package com.watabou.pixeldungeon.scenes;

import com.nyrds.android.util.GuiProperties;
import com.nyrds.pixeldungeon.effects.NewFireball;
import com.nyrds.pixeldungeon.ml.R;
import com.watabou.gltextures.TextureCache;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.watabou.noosa.Text;
import com.watabou.noosa.audio.Music;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.ui.Button;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.PixelDungeon;
import com.watabou.pixeldungeon.effects.BannerSprites;
import com.watabou.pixeldungeon.effects.Fireball;
import com.watabou.pixeldungeon.ui.Archs;
import com.watabou.pixeldungeon.ui.ChangelogButton;
import com.watabou.pixeldungeon.ui.DonateButton;
import com.watabou.pixeldungeon.ui.ExitButton;
import com.watabou.pixeldungeon.ui.ModsButton;
import com.watabou.pixeldungeon.ui.PlayGamesButton;
import com.watabou.pixeldungeon.ui.PrefsButton;
import com.watabou.pixeldungeon.ui.PremiumPrefsButton;
import com.watabou.pixeldungeon.ui.RedButton;
import com.watabou.pixeldungeon.ui.StatisticsButton;

public class TitleScene extends PixelScene {

	private static final    int GAP           = 2;
	private static final int BTN_HEIGHT	= 16;
	private static final int WIDTH		= 120;

	private static final String TXT_PLAY = Game
			.getVar(R.string.TitleScene_Play);
	private static final String TXT_HIGHSCORES = Game
			.getVar(R.string.TitleScene_Highscores);
	private static final String TXT_BADGES = Game
			.getVar(R.string.TitleScene_Badges);
	private static final String TXT_ABOUT = Game
			.getVar(R.string.TitleScene_About);

	private static final String REMIXED_TITLE = "title.png";

	Text            pleaseSupport;
	DonateButton    btnDonate;
	
	@Override
	public void create() {
		super.create();

		Music.INSTANCE.play(Assets.THEME, true);
		Music.INSTANCE.volume(1f);

		uiCamera.setVisible(false);

		int w = Camera.main.width;
		int h = Camera.main.height;

		float height = 180;

		Image title = new Image(REMIXED_TITLE);
		add(title);

		title.x = (w - title.width()) / 2;
		title.y = (title.height() * 0.10f) / 2;

		if (PixelDungeon.landscape()){
			title.y = -(title.height() * 0.05f);
		}

		placeTorch(title.x + title.width/2 + 3, title.y + title.height/2 - 3);
		placeTorch(title.x + title.width/2 - 5, title.y + title.height/2 + 5);
		placeTorch(title.x + title.width/2 - 14, title.y + title.height/2 + 14) ;

		RedButton btnBadges = new RedButton( TXT_BADGES ) {
			@Override
			protected void onClick() {
				PixelDungeon.switchNoFade(BadgesScene.class);
			}
		};
		btnBadges.setRect( 0, 0, WIDTH/2, BTN_HEIGHT );
		add(btnBadges);

		RedButton btnAbout = new RedButton(TXT_ABOUT) {
			@Override
			protected void onClick() {
				PixelDungeon.switchNoFade(AboutScene.class);
			}
		};
		btnAbout.setRect( 0, 0, WIDTH/2, BTN_HEIGHT );
		add(btnAbout);

		RedButton btnPlay = new RedButton(TXT_PLAY) {
			@Override
			protected void onClick() {
				PixelDungeon.switchNoFade(StartScene.class);
			}
		};
		btnPlay.setRect( 0, 0, WIDTH/2, BTN_HEIGHT );
		add(btnPlay);

		RedButton btnHighscores = new RedButton(TXT_HIGHSCORES) {
			@Override
			protected void onClick() {
				PixelDungeon.switchNoFade(RankingsScene.class);
			}
		};
		btnHighscores.setRect( 0, 0, WIDTH/2, BTN_HEIGHT );
		add(btnHighscores);

		btnDonate = new DonateButton();

		pleaseSupport = PixelScene.createText(GuiProperties.titleFontSize());
		pleaseSupport.text(btnDonate.getText());
		pleaseSupport.measure();
		pleaseSupport.setPos((w - pleaseSupport.width()) / 2,
				h - pleaseSupport.height() * 2);

		btnDonate.setPos((w - btnDonate.width()) / 2, pleaseSupport.y
				- btnDonate.height());

		float dashBaseline = btnDonate.top();

		if (PixelDungeon.landscape()) {
			btnHighscores.setPos(w / 2 - btnHighscores.width(), dashBaseline);
			btnBadges.setPos(w / 2, dashBaseline);
			btnPlay.setPos(btnHighscores.left() - btnPlay.width(), dashBaseline);
			btnAbout.setPos(btnBadges.right(), dashBaseline);
		} else {
			btnPlay.setPos(w / 4, title.bottom() * 0.85f + GAP);
			btnHighscores.setPos(w / 4, btnPlay.bottom());
			btnBadges.setPos(w / 4, btnHighscores.bottom());
			btnAbout.setPos(w / 4, btnBadges.bottom());
		}

		Archs archs = new Archs();
		archs.setSize(w, h);
		addToBack(archs);

		Text version = Text.createBasicText("v " + Game.version, font1x);
		version.measure();
		version.hardlight(0x888888);
		version.setPos(w - version.width(), h - version.height());
		add(version);

		float freeInternalStorage = Game.getAvailableInternalMemorySize();

		if (freeInternalStorage < 2) {
			Text lowInteralStorageWarning = PixelScene
					.createMultiline(GuiProperties.regularFontSize());
			lowInteralStorageWarning.text(Game
					.getVar(R.string.TitleScene_InternalStorageLow));
			lowInteralStorageWarning.measure();
			lowInteralStorageWarning.setPos(0,
					h - lowInteralStorageWarning.height());
			lowInteralStorageWarning.hardlight(0.95f, 0.1f, 0.1f);
			add(lowInteralStorageWarning);
		}

		PrefsButton btnPrefs = new PrefsButton();
		btnPrefs.setPos(0, 0);
		add(btnPrefs);

		PlayGamesButton btnPlayGames = new PlayGamesButton();
		btnPlayGames.setPos(btnPrefs.right() + 2, 0);
		add(btnPlayGames);

		ModsButton btnMods = new ModsButton();
		btnMods.setPos(0, btnPrefs.bottom() + 2);
		
		add(btnMods);
		
		if (PixelDungeon.donated() > 0) {
			PremiumPrefsButton btnPPrefs = new PremiumPrefsButton();
			btnPPrefs.setPos(btnPrefs.right() + 2, 0);
			add(btnPPrefs);
			btnPlayGames.setPos(btnPPrefs.right() + 2, 0);
		}

		ExitButton btnExit = new ExitButton();
		btnExit.setPos(w - btnExit.width(), 0);
		add(btnExit);
		
		ChangelogButton btnChangelog = new ChangelogButton();
		btnChangelog.setPos(w - btnChangelog.width(), btnExit.bottom() + 2);
		add(btnChangelog);

		StatisticsButton btnStats = new StatisticsButton();
		btnStats.setPos(w - btnStats.width(), btnChangelog.bottom() + 2);
		add(btnStats);

		fadeIn();
	}

	private double time = 0;
	private boolean donationAdded = false;
	@Override
	public void update() {
		super.update();
		time += Game.elapsed;
		
		if(!donationAdded) {
			if (PixelDungeon.canDonate()) {
				add(pleaseSupport);
				add(btnDonate);
			}
			donationAdded = true;
		} else {
			float cl = (float) Math.sin(time) * 0.5f + 0.5f;
			pleaseSupport.hardlight(cl, cl, cl);
		}

	}

	private void placeTorch(float x, float y) {
		NewFireball fb = new NewFireball();
		fb.setPos(x, y);
		add(fb);
	}
}
