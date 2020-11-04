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
import com.nyrds.android.util.Util;
import com.nyrds.pixeldungeon.effects.NewFireball;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.support.PlayGames;
import com.nyrds.pixeldungeon.windows.VBox;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.watabou.noosa.Text;
import com.watabou.noosa.audio.Music;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.RemixedDungeon;
import com.watabou.pixeldungeon.ui.Archs;
import com.watabou.pixeldungeon.ui.ChangelogButton;
import com.watabou.pixeldungeon.ui.DashboardItem;
import com.watabou.pixeldungeon.ui.DonateButton;
import com.watabou.pixeldungeon.ui.ExitButton;
import com.watabou.pixeldungeon.ui.Icons;
import com.watabou.pixeldungeon.ui.ImageButton;
import com.watabou.pixeldungeon.ui.ModsButton;
import com.watabou.pixeldungeon.ui.PlayGamesButton;
import com.watabou.pixeldungeon.ui.PrefsButton;
import com.watabou.pixeldungeon.ui.PremiumPrefsButton;
import com.watabou.pixeldungeon.ui.StatisticsButton;
import com.watabou.pixeldungeon.utils.Utils;

public class TitleScene extends PixelScene {

	private Text         pleaseSupport;
	private DonateButton btnDonate;
	private boolean      changelogUpdated;
	private ChangelogButton btnChangelog;

	@Override
	public void create() {
		super.create();

		Music.INSTANCE.play(Assets.THEME, true);
		Music.INSTANCE.volume(1f);

		uiCamera.setVisible(false);

		int w = Camera.main.width;
		int h = Camera.main.height;

		float height = 180;

		Image title = new Image(Assets.getTitle());
		add(title);

		title.x = (w - title.width()) / 2;
		title.y = (title.height() * 0.10f) / 2;

		if (RemixedDungeon.landscape()){
			title.y = -(title.height() * 0.05f);
		}

		//placeTorch(title.x + title.width/2 + 3, title.y + title.height/2 - 3);
		//placeTorch(title.x + title.width/2 - 5, title.y + title.height/2 + 5);
		//placeTorch(title.x + title.width/2 - 14, title.y + title.height/2 + 14) ;

		DashboardItem btnBadges = new DashboardItem(Game.getVar(R.string.TitleScene_Badges), 3) {
			@Override
			protected void onClick() {
				RemixedDungeon.switchNoFade(BadgesScene.class);
			}
		};
		btnBadges.setPos(w / 2 - btnBadges.width(), (h + height) / 2
				- DashboardItem.SIZE);
		add(btnBadges);


		ModsButton btnMods = new ModsButton();
		btnMods.setPos(w / 2, (h + height) / 2 - DashboardItem.SIZE);
		add(btnMods);

		DashboardItem btnPlay = new DashboardItem(Game.getVar(R.string.TitleScene_Play), 0) {
			@Override
			protected void onClick() {
				RemixedDungeon.switchNoFade(StartScene.class);
			}
		};
		btnPlay.setPos(w / 2 - btnPlay.width(), btnMods.top()
				- DashboardItem.SIZE);
		add(btnPlay);

		DashboardItem btnHighscores = new DashboardItem(Game.getVar(R.string.TitleScene_Highscores), 2) {
			@Override
			protected void onClick() {
				RemixedDungeon.switchNoFade(RankingsScene.class);
			}
		};
		btnHighscores.setPos(w / 2, btnPlay.top());
		add(btnHighscores);

		btnDonate = new DonateButton(this);

		pleaseSupport = PixelScene.createText(GuiProperties.titleFontSize());
		pleaseSupport.text(btnDonate.getText());
		pleaseSupport.setPos((w - pleaseSupport.width()) / 2,
				h - pleaseSupport.height() * 2);

		btnDonate.setPos((w - btnDonate.width()) / 2, pleaseSupport.y
				- btnDonate.height());

		float dashBaseline = btnDonate.top() - DashboardItem.SIZE;

		if (RemixedDungeon.landscape()) {
			btnPlay.setPos(w / 2 - btnPlay.width() * 2, dashBaseline);
			btnHighscores.setPos(w / 2 - btnHighscores.width(), dashBaseline + btnHighscores.height()/3);
			btnBadges.setPos(w / 2, dashBaseline + btnBadges.height()/3);
			btnMods.setPos(btnBadges.right(), dashBaseline);
		} else {
			btnPlay.setPos(w / 2 - btnPlay.width(), btnMods.top()
					- DashboardItem.SIZE + 5);
			btnHighscores.setPos(w / 2, btnPlay.top());
			btnBadges.setPos(w / 2 - btnBadges.width(), dashBaseline + 5);
			btnMods.setPos(w / 2, dashBaseline + 5);
		}

		Archs archs = new Archs();
		archs.setSize(w, h);
		addToBack(archs);

		Text version = Text.createBasicText(Game.version, font1x);
		version.hardlight(0x888888);
		version.setPos(w - version.width(), h - version.height());
		add(version);

		float freeInternalStorage = Util.getAvailableInternalMemorySize();

		if (freeInternalStorage < 2) {
			Text lowInteralStorageWarning = PixelScene
					.createMultiline(GuiProperties.regularFontSize());
			lowInteralStorageWarning.text(Game
					.getVar(R.string.TitleScene_InternalStorageLow));
			lowInteralStorageWarning.setPos(0,
					h - lowInteralStorageWarning.height());
			lowInteralStorageWarning.hardlight(0.95f, 0.1f, 0.1f);
			add(lowInteralStorageWarning);
		}

		VBox leftGroup = new VBox();

		leftGroup.add(new PrefsButton());
        if (RemixedDungeon.donated() > 0) {
            leftGroup.add(new PremiumPrefsButton());
        }

        if(PlayGames.usable()) {
			leftGroup.add(new PlayGamesButton());
		}

        String lang = RemixedDungeon.uiLanguage();
        final boolean useVk = lang.equals("ru");

        Icons social =  useVk ? Icons.VK : Icons.FB;
        leftGroup.add(new ImageButton(social.get()){
            @Override
            protected void onClick() {
                Game.instance().openUrl("Visit us on social network", useVk ? "https://vk.com/pixel_dungeon_remix" : "https://fb.me/RemixedDungeon");
            }
        });

        Image img = new Image(Assets.DASHBOARD,DashboardItem.IMAGE_SIZE,1);

        img.setScale(0.45f,0.45f);
        ImageButton btnAbout = new ImageButton(img){
			@Override
			protected void onClick() {
				RemixedDungeon.switchNoFade(AboutScene.class);
			}
		};


        leftGroup.add(btnAbout);

		leftGroup.setPos(0,0);
		add(leftGroup);

		ExitButton btnExit = new ExitButton();
		btnExit.setPos(w - btnExit.width(), 0);
		add(btnExit);

		if (RemixedDungeon.version() != Game.versionCode) {
			if(Utils.differentVersions(RemixedDungeon.versionString(),Game.version)) {
				changelogUpdated = true;
			}
		}

		btnChangelog = new ChangelogButton();
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
		float cl = (float) Math.sin(time) * 0.5f + 0.5f;
		if(!donationAdded) {
			if (RemixedDungeon.canDonate()) {
				add(pleaseSupport);
				add(btnDonate);
			}
			donationAdded = true;
		} else {
			pleaseSupport.hardlight(cl, cl, cl);
		}

		if(changelogUpdated) {
			btnChangelog.brightness(cl + 1);
		}

	}

	private void placeTorch(float x, float y) {
		NewFireball fb = new NewFireball();
		fb.setPos(x, y);
		add(fb);
	}

}
