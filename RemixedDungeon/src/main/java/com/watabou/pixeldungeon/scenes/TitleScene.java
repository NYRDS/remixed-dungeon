
package com.watabou.pixeldungeon.scenes;

import com.nyrds.pixeldungeon.game.GameLoop;
import com.nyrds.pixeldungeon.game.GamePreferences;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.support.PlayGames;
import com.nyrds.pixeldungeon.windows.VBox;
import com.nyrds.pixeldungeon.windows.WndLocalModInstall;
import com.nyrds.platform.EventCollector;
import com.nyrds.platform.audio.Music;
import com.nyrds.platform.game.Game;
import com.nyrds.platform.game.InstallMod;
import com.nyrds.platform.game.RemixedDungeon;
import com.nyrds.platform.storage.copyFromSAF;
import com.nyrds.platform.util.StringsManager;
import com.nyrds.util.GuiProperties;
import com.nyrds.util.ModdingMode;
import com.nyrds.util.Util;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Image;
import com.watabou.noosa.Text;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Dungeon;
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

import org.luaj.vm2.LuaError;

public class TitleScene extends PixelScene {

    private Text pleaseSupport;
    private DonateButton btnDonate;
    private boolean changelogUpdated;
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

        title.setX((w - title.width()) / 2);
        title.setY((title.height() * 0.10f) / 2);

        if (RemixedDungeon.landscape()) {
            title.setY(-(title.height() * 0.05f));
        }

        DashboardItem btnBadges = new DashboardItem(StringsManager.getVar(R.string.TitleScene_Badges), 3) {
            @Override
            protected void onClick() {
                RemixedDungeon.switchNoFade(BadgesScene.class);
            }
        };
        final int w_center = w / 2;

        btnBadges.setPos(w_center - btnBadges.width(), (h + height) / 2
                - DashboardItem.SIZE);
        add(btnBadges);


        ModsButton btnMods = new ModsButton();
        btnMods.setPos(w_center, (h + height) / 2 - DashboardItem.SIZE);
        add(btnMods);

        DashboardItem btnPlay = new DashboardItem(StringsManager.getVar(R.string.TitleScene_Play), 0) {
            @Override
            protected void onClick() {
                RemixedDungeon.switchNoFade(StartScene.class);
            }
        };
        btnPlay.setPos(w_center - btnPlay.width(), btnMods.top()
                - DashboardItem.SIZE);
        add(btnPlay);

        DashboardItem btnHighscores = new DashboardItem(StringsManager.getVar(R.string.TitleScene_Highscores), 2) {
            @Override
            protected void onClick() {
                RemixedDungeon.switchNoFade(RankingsScene.class);
            }
        };
        btnHighscores.setPos(w_center, btnPlay.top());
        add(btnHighscores);

        btnDonate = new DonateButton(this);

        pleaseSupport = PixelScene.createText(GuiProperties.titleFontSize());
        pleaseSupport.text(btnDonate.getText());
        pleaseSupport.setPos((w - pleaseSupport.width()) / 2,
                h - pleaseSupport.height() * 2);

        btnDonate.setPos((w - btnDonate.width()) / 2, pleaseSupport.getY()
                - btnDonate.height());

        float dashBaseline = btnDonate.top() - DashboardItem.SIZE;

        if (RemixedDungeon.landscape()) {
            btnPlay.setPos(w_center - btnPlay.width() * 2, dashBaseline);
            btnHighscores.setPos(w_center - btnHighscores.width(), dashBaseline + btnHighscores.height() / 3);
            btnBadges.setPos(w_center, dashBaseline + btnBadges.height() / 3);
            btnMods.setPos(btnBadges.right(), dashBaseline);
        } else {
            btnPlay.setPos(w_center - btnPlay.width(), btnMods.top()
                    - DashboardItem.SIZE + 5);
            btnHighscores.setPos(w_center, btnPlay.top());
            btnBadges.setPos(w_center - btnBadges.width(), dashBaseline + 5);
            btnMods.setPos(w_center, dashBaseline + 5);
        }

        Archs archs = new Archs();
        archs.setSize(w, h);
        sendToBack(archs);

        Text version = Text.createBasicText(Game.version, font1x);
        version.hardlight(0x888888);
        version.setPos(w - version.width(), h - version.height());
        add(version);

        float freeInternalStorage = Util.getAvailableInternalMemorySize();

        if (freeInternalStorage < 2) {
            Text lowInternalStorageWarning = PixelScene
                    .createMultiline(GuiProperties.regularFontSize());
            lowInternalStorageWarning.text(StringsManager.getVar(R.string.TitleScene_InternalStorageLow));
            lowInternalStorageWarning.setPos(0,
                    h - lowInternalStorageWarning.height());
            lowInternalStorageWarning.hardlight(0.95f, 0.1f, 0.1f);
            add(lowInternalStorageWarning);
        }

        VBox leftGroup = new VBox();

        leftGroup.add(new PrefsButton());
        if (GamePreferences.donated() > 0) {
            leftGroup.add(new PremiumPrefsButton());
        }

        if (PlayGames.usable()) {
            leftGroup.add(new PlayGamesButton());
        }

        String lang = GamePreferences.uiLanguage();
        final boolean ruUser = lang.equals("ru");

        Icons social = ruUser ? Icons.VK : Icons.FB;
        if (ruUser) {
            leftGroup.add(new ImageButton(social.get()) {
                @Override
                protected void onClick() {
                    Game.instance().openUrl("Visit us on social network", ruUser ? "https://vk.com/pixel_dungeon_remix" : "https://fb.me/RemixedDungeon");
                }
            });
        }

        if(!ruUser) {
            leftGroup.add(new ImageButton(Icons.DISCORD.get()) {
                @Override
                protected void onClick() {
                    Game.instance().openUrl("Let talk on Discord", "https://discord.gg/AMXrhQZ");
                }
            });
        }

        leftGroup.add(new ImageButton(Icons.TG.get()) {
            @Override
            protected void onClick() {
                Game.instance().openUrl("Join our Telegram group", "https://t.me/RemixedDungeon");
            }
        });


        Image img = new Image(Assets.DASHBOARD, DashboardItem.IMAGE_SIZE, 1);

        img.setScaleXY(0.45f, 0.45f);
        ImageButton btnAbout = new ImageButton(img) {
            @Override
            protected void onClick() {
                RemixedDungeon.switchNoFade(AboutScene.class);
            }
        };


        leftGroup.add(btnAbout);

        leftGroup.setPos(0, 0);
        add(leftGroup);

        ExitButton btnExit = new ExitButton();
        btnExit.setPos(w - btnExit.width(), 0);
        add(btnExit);

        if (GamePreferences.version() != Game.versionCode) {
            if (Utils.differentVersions(GamePreferences.versionString(), Game.version)) {
                changelogUpdated = true;
            }
        }

        btnChangelog = new ChangelogButton();
        btnChangelog.setPos(w - btnChangelog.width(), btnExit.bottom() + 2);

        add(btnChangelog);

        StatisticsButton btnStats = new StatisticsButton();
        btnStats.setPos(w - btnStats.width(), btnChangelog.bottom() + 2);
        add(btnStats);

        Dungeon.reset();

        fadeIn();

        if (copyFromSAF.mBasePath!=null) {
            GameLoop.pushUiTask(() -> WndLocalModInstall.onDirectoryPicked());
        }

        if(Game.instance() instanceof InstallMod) {
            GameLoop.pushUiTask(()  -> {((InstallMod) Game.instance()).installMod();});
        }

    }

    private double time = 0;
    private boolean donationAdded = false;

    @Override
    public void update() {
        try {
            super.update();
        } catch (LuaError e) {
            EventCollector.logException(e, "TitleScene lua error");
            ModdingMode.selectMod(ModdingMode.REMIXED);
            RemixedDungeon.instance().doRestart();
        }

        time += GameLoop.elapsed;
        float cl = (float) Math.sin(time) * 0.5f + 0.5f;
        if (!donationAdded) {
            if (RemixedDungeon.canDonate()) {
                add(pleaseSupport);
                add(btnDonate);
            }
            donationAdded = true;
        } else {
            pleaseSupport.hardlight(cl, cl, cl);
        }

        if (changelogUpdated) {
            btnChangelog.brightness(cl + 1);
        }

    }
}
