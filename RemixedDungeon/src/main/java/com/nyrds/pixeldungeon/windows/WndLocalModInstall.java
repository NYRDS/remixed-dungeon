package com.nyrds.pixeldungeon.windows;

import com.nyrds.pixeldungeon.game.GameLoop;
import com.nyrds.platform.game.Game;
import com.nyrds.platform.storage.copyFromSAF;
import com.watabou.pixeldungeon.windows.WndOptions;

public class WndLocalModInstall extends WndOptions  {
    public WndLocalModInstall() {
        super("Install local mod from directory", "Step 1:" +
                "\nSelect directory to install mod from", "Pick mod directory");
    }

    @Override
    public void onSelect(int index) {
        Game.instance().pickDirectory();
    }

    public static void onDirectoryPicked()  {
        if(copyFromSAF.mBasePath != null) {
            var wnd = new WndInstallingMod();
            copyFromSAF.setListener(wnd);
            GameLoop.pushUiTask(() -> {
                GameLoop.addToScene(wnd);
            });
            GameLoop.execute(() -> copyFromSAF.copyModToAppStorage());
        }
    }
}
