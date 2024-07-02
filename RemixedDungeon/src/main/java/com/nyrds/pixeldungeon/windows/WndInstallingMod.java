package com.nyrds.pixeldungeon.windows;

import com.nyrds.pixeldungeon.game.GameLoop;
import com.nyrds.platform.storage.copyFromSAF;
import com.watabou.pixeldungeon.ui.Icons;
import com.watabou.pixeldungeon.windows.WndTitledMessage;

public class WndInstallingMod extends WndTitledMessage implements copyFromSAF.IListener {

    public WndInstallingMod() {
        super(Icons.get(Icons.BUSY),"Installing mod, please wait...", "");
    }

    @Override
    public void onFileCopy(String path) {
        GameLoop.pushUiTask( () -> {
            setText(String.format("Copying %s to Remixed Dungeon storage", path));
        });
    }

    @Override
    public void onFileSkip(String path) {
        GameLoop.pushUiTask( () -> {
            setText(String.format("Skipping %s", path));
        });
    }


    @Override
    public void onComplete() {
        hide();
    }

    @Override
    public void hide()  {
        super.hide();
        copyFromSAF.setListener(null);
    }
}
