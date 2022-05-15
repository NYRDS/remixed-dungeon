package com.watabou.pixeldungeon.ui;

import com.nyrds.pixeldungeon.game.GameLoop;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.support.PlayGames;
import com.nyrds.platform.game.Game;
import com.nyrds.platform.storage.Preferences;
import com.nyrds.platform.util.StringsManager;
import com.nyrds.util.GuiProperties;
import com.watabou.noosa.Text;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.windows.WndMessage;

/**
 * Created by mike on 14.05.2017.
 * This file is part of Remixed Pixel Dungeon.
 */

public class WndPlayGames extends Window {

    private int y = GAP;

    public WndPlayGames() {

        boolean playGamesConnected = Game.instance().playGames.isConnected();
        resizeLimited(120);

        Text listTitle = PixelScene.createMultiline(R.string.WndPlayGames_Title, GuiProperties.mediumTitleFontSize());
        listTitle.hardlight(TITLE_COLOR);
        listTitle.maxWidth(width - GAP * 2);
        listTitle.setX((width - listTitle.width()) / 2);
        listTitle.setY(y);

        add(listTitle);

        y += listTitle.height() + GAP;

        CheckBox usePlayGames = new CheckBox(StringsManager.getVar(R.string.WndPlayGames_Use),
                Preferences.INSTANCE.getBoolean(Preferences.KEY_USE_PLAY_GAMES, false)
                && Game.instance().playGames.isConnected()
                )
        {
            @Override
            public void checked(boolean value) {
                super.checked(value);

                if (value) {
                    Game.instance().playGames.connectExplicit();
                    GameLoop.addToScene(new WndMessage(StringsManager.getVar(R.string.WndPlayGames_Connecting)));
                } else {
                    Game.instance().playGames.disconnect();
                }

                hide();
            }
        };

        addButton(usePlayGames);

        if (!playGamesConnected) {
            resize(width, y);
            return;
        }

        addButton(new RedButton(R.string.WndPlayGames_Show_Badges) {
            @Override
            protected void onClick() {
                super.onClick();
                Game.instance().playGames.showBadges();
            }
        });

        addButton(new RedButton(R.string.WndPlayGames_Show_Leaderboards) {
            @Override
            protected void onClick() {
                super.onClick();
                Game.instance().playGames.showLeaderboard();
            }
        });

        addButton(new RedButton(R.string.WndPlayGames_BackupProgress) {
            @Override
            protected void onClick() {
                super.onClick();
                Game.instance().playGames.backupProgress(new ResultHandler());
            }
        });

        addButton(new RedButton(R.string.WndPlayGames_RestoreProgress) {
            @Override
            protected void onClick() {
                super.onClick();
                Game.instance().playGames.restoreProgress(new ResultHandler());
            }
        });

        addButton(new RedButton(R.string.WndSettings_RecordVideo) {
            @Override
            protected void onClick() {
                super.onClick();
                Game.instance().playGames.showVideoOverlay();
            }
        });

        resize(width, y);
    }

    private void addButton(TextButton btn) {
        btn.setRect(0, y, width, BUTTON_HEIGHT);
        add(btn);
        y += btn.height();
    }

    public static class ResultHandler implements PlayGames.IResult {

        private final WndMessage working;
        ResultHandler() {
            working = new WndMessage(StringsManager.getVar(R.string.WndPlayGames_WorkInCloud));
            GameLoop.addToScene(working);
        }
        @Override
        public void status(final boolean status) {
            GameLoop.pushUiTask(() -> {
                working.hide();
                        int id = status ? R.string.WndPlayGames_Show_Ok : R.string.WndPlayGames_Show_Error;
                        String msg = StringsManager.getVar(id);
                GameLoop.addToScene(new WndMessage(msg));
            }
            );
        }
    }
}
