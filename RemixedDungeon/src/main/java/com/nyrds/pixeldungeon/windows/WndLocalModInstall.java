package com.nyrds.pixeldungeon.windows;

import com.nyrds.pixeldungeon.game.GameLoop;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.storage.AndroidSAF;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.windows.WndOptions;

public class WndLocalModInstall extends WndOptions  {
    public WndLocalModInstall() {
        super(StringsManager.getVar(R.string.WndLocalModInstall_title), StringsManager.getVar(R.string.WndLocalModInstall_text), StringsManager.getVar(R.string.WndLocalModInstall_pick));
    }

    @Override
    public void onSelect(int index) {
        AndroidSAF.pickDirectoryForModInstall();
    }

    public static void onDirectoryPicked()  {
        if(AndroidSAF.mBasePath != null) {
            var wnd = new WndInstallingMod();
            AndroidSAF.setListener(wnd);
            GameLoop.pushUiTask(() -> {
                GameLoop.addToScene(wnd);
            });
            GameLoop.execute(() -> AndroidSAF.copyModToAppStorage());
        }
    }
}
