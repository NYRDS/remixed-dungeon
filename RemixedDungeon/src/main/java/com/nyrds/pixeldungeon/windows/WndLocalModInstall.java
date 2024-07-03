package com.nyrds.pixeldungeon.windows;

import com.nyrds.pixeldungeon.game.GameLoop;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.game.Game;
import com.nyrds.platform.storage.copyFromSAF;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.windows.WndOptions;

public class WndLocalModInstall extends WndOptions  {
    public WndLocalModInstall() {
        super(StringsManager.getVar(R.string.WndLocalModInstall_title), StringsManager.getVar(R.string.WndLocalModInstall_text), StringsManager.getVar(R.string.WndLocalModInstall_pick));
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
