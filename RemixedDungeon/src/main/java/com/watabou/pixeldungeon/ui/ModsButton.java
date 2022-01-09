package com.watabou.pixeldungeon.ui;

import android.Manifest;

import com.nyrds.pixeldungeon.game.GameLoop;
import com.nyrds.pixeldungeon.game.GamePreferences;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.windows.DownloadProgressWindow;
import com.nyrds.platform.game.Game;
import com.nyrds.platform.storage.FileSystem;
import com.nyrds.platform.util.StringsManager;
import com.nyrds.platform.util.Util;
import com.nyrds.util.DownloadStateListener;
import com.nyrds.util.DownloadTask;
import com.nyrds.util.GuiProperties;
import com.nyrds.util.Mods;
import com.watabou.noosa.Group;
import com.watabou.noosa.Image;
import com.watabou.noosa.InterstitialPoint;
import com.watabou.noosa.Scene;
import com.watabou.noosa.Text;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.windows.WndMessage;
import com.watabou.pixeldungeon.windows.WndModSelect;
import com.watabou.pixeldungeon.windows.WndTitledMessage;

import java.io.File;

public class ModsButton extends ImageButton implements InterstitialPoint, DownloadStateListener.IDownloadComplete {

    private Text text;
    private Text text2;

    static private boolean needUpdate;

    public ModsButton() {
        super(new Image(Assets.DASHBOARD, DashboardItem.IMAGE_SIZE, 5));

        text = PixelScene.createText(GuiProperties.titleFontSize());
        text.text(R.string.TitleScene_Mods);
        add(text);

        text2 = PixelScene.createText(GuiProperties.titleFontSize());
        text2.text(GamePreferences.activeMod());
        add(text2);

        setSize(DashboardItem.SIZE, DashboardItem.SIZE);
    }

    static public void modUpdated() {
        needUpdate = true;
    }

    @Override
    public void update() {
        if (needUpdate) {
            needUpdate = false;
            text2.text(GamePreferences.activeMod());
        }
        super.update();
    }


    @Override
    protected void layout() {
        super.layout();

        image.x = PixelScene.align(x + (width - image.width()) / 2);
        image.y = PixelScene.align(y);

        text.x = PixelScene.align(x + (width - text.width()) / 2);
        text.y = PixelScene.align(image.y + image.height() + 2);

        text2.x = PixelScene.align(x + (width - text2.width()) / 2);
        text2.y = PixelScene.align(text.y + text.height() + 2);
    }

    @Override
    protected void onClick() {
        String[] requiredPermissions = {Manifest.permission.INTERNET};
        Game.instance().doPermissionsRequest(this, requiredPermissions);
    }

    @Override
    public void returnToWork(final boolean result) {
        final Group parent = getParent();

        Scene scene = GameLoop.scene();

        if(scene==null) {
            return;
        }

        scene.add(new WndMessage(StringsManager.getVar(R.string.Mods_Disclaimer)) {
                             @Override
                             public void hide() {
                                 super.hide();
                                 GameLoop.pushUiTask(() -> {
                                     if (result) {
                                         if (Util.isConnectedToInternet()) {
                                             File modsCommon = FileSystem.getExternalStorageFile(Mods.MODS_COMMON_JSON);
                                             modsCommon.delete();
                                             String downloadTo = modsCommon.getAbsolutePath();

                                             GameLoop.execute(new DownloadTask(new DownloadProgressWindow("Downloading", ModsButton.this),
                                                     "https://nyrds.github.io/NYRDS/mods.json",
                                                     downloadTo));

                                         } else {
                                             DownloadComplete("no internet", true);
                                         }

                                     } else {
                                         parent.add(new WndTitledMessage(Icons.get(Icons.SKULL), "No permissions granted", "No permissions granted"));
                                     }
                                 });

                             }
                         }
        );


    }

    @Override
    public void DownloadComplete(String file, final Boolean result) {
        GameLoop.pushUiTask(() -> {
            GameLoop.addToScene(new WndModSelect());

            if (!result) {
                Game.toast("Mod list download failed :(");
            }
        });
    }
}
