
package com.watabou.pixeldungeon.windows;

import com.nyrds.pixeldungeon.game.GameLoop;
import com.nyrds.pixeldungeon.game.GamePreferences;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.windows.WndInGameUiSettings;
import com.nyrds.platform.game.Game;
import com.nyrds.platform.storage.Preferences;
import com.nyrds.platform.util.StringsManager;
import com.watabou.noosa.Camera;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.utils.GLog;


public class WndSettingsInGame extends WndMenuCommon {

    @Override
    protected void createItems() {
        addSoundControls(menuItems);

        menuItems.add(new MenuCheckBox(StringsManager.getVar(R.string.WndSettings_Brightness), GamePreferences.brightness()) {
            @Override
            protected void onClick() {
                super.onClick();
                GamePreferences.brightness(checked());
            }
        });

        menuItems.add(createZoomButtons());

        var isometricModeCheckBox = new MenuCheckBox(R.string.WndSettingsInGame_IsometricTiles, Preferences.INSTANCE.getBoolean(Preferences.KEY_USE_ISOMETRIC_TILES, false)) {
            @Override
            protected void onClick() {
                super.onClick();
                Preferences.INSTANCE.put(Preferences.KEY_USE_ISOMETRIC_TILES, checked());
                Dungeon.setIsometricMode(checked());
                GameLoop.pushUiTask(GameLoop::resetScene);
            }
        };

        isometricModeCheckBox.enable(Dungeon.isometricModeAllowed);
        menuItems.add(isometricModeCheckBox);


        menuItems.add(new MenuButton(R.string.WndSettings_InGameUiSettings) {
            @Override
            protected void onClick() {
                super.onClick();
                WndSettingsInGame.this.add(new WndInGameUiSettings());
            }
        });

        if (Game.instance().playGames.isConnected()) {
            menuItems.add(new MenuButton(R.string.WndSettings_RecordVideo) {
                @Override
                protected void onClick() {
                    super.onClick();
                    Game.instance().playGames.showVideoOverlay();

                }
            });
        }

        menuItems.add(new MenuCheckBox(R.string.WndSettingsInGame_GameLog, GLog.enabled) {
            @Override
            protected void onClick() {
                super.onClick();
                GLog.enabled = checked();
            }
        });
    }

    private Selector createZoomButtons() {
        return new Selector(WIDTH, BUTTON_HEIGHT, StringsManager.getVar(R.string.WndSettings_ZoomDef), new Selector.PlusMinusDefault() {

            @Override
            public void onPlus(Selector s) {
                zoom(Camera.main.zoom + 0.1f, s);
            }

            @Override
            public void onMinus(Selector s) {
                zoom(Camera.main.zoom - 0.1f, s);
            }

            @Override
            public void onDefault(Selector s) {
                zoom(PixelScene.defaultZoom, s);
            }

            private void zoom(float value, Selector s) {
                Camera.main.zoom(value);
                GamePreferences.zoom(value - PixelScene.defaultZoom);

                float zoom = Camera.main.zoom;
                s.enable(zoom < PixelScene.maxZoom, zoom > PixelScene.minZoom, true);
            }
        });
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        GameLoop.resetScene();
    }
}
