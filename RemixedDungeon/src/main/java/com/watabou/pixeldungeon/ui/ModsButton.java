package com.watabou.pixeldungeon.ui;

import com.nyrds.pixeldungeon.game.GameLoop;
import com.nyrds.pixeldungeon.game.GamePreferences;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.windows.WndModsDisclaimer;
import com.nyrds.platform.game.Game;
import com.nyrds.util.DownloadStateListener;
import com.nyrds.util.GuiProperties;
import com.watabou.noosa.Group;
import com.watabou.noosa.Image;
import com.watabou.noosa.InterstitialPoint;
import com.watabou.noosa.Scene;
import com.watabou.noosa.Text;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.windows.WndModSelect;

public class ModsButton extends ImageButton implements InterstitialPoint, DownloadStateListener.IDownloadComplete {

    private final Text text;
    private final Text text2;

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

        image.setX(PixelScene.align(x + (width - image.width()) / 2));
        image.setY(PixelScene.align(y));

        text.setX(PixelScene.align(x + (width - text.width()) / 2));
        text.setY(PixelScene.align(image.getY() + image.height() + 2));

        text2.setX(PixelScene.align(x + (width - text2.width()) / 2));
        text2.setY(PixelScene.align(text.getY() + text.height() + 2));
    }

    @Override
    protected void onClick() {
        Game.requestInternetPermission(this);
    }

    @Override
    public void returnToWork(final boolean result) {
        final Group parent = getParent();

        Scene scene = GameLoop.scene();

        if (scene == null) {
            return;
        }

        GameLoop.pushUiTask(() -> {
            GameLoop.addToScene(new WndModsDisclaimer(this, result, parent)
            );
        });


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
