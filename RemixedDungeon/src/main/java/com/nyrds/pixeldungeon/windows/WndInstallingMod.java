package com.nyrds.pixeldungeon.windows;

import com.nyrds.pixeldungeon.game.GameLoop;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.storage.copyFromSAF;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.ui.BusyIndicator;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.pixeldungeon.windows.WndTitledMessage;

public class WndInstallingMod extends WndTitledMessage implements copyFromSAF.IListener {

    public WndInstallingMod() {
        super(new BusyIndicator(), StringsManager.getVar(R.string.WndInstallingMod_please_wait), "");
    }

    @Override
    public void onFileCopy(String path) {
        GameLoop.pushUiTask( () -> {
            setText(String.format(StringsManager.getVar(R.string.WndInstallingMod_copying_file), path));
        });
    }

    @Override
    public void onFileSkip(String path) {
        GameLoop.pushUiTask( () -> {
            setText(Utils.format(R.string.WndInstallingMod_skipping_file, path));
        });
    }

    @Override
    public void onFileDelete(String entry) {
        GameLoop.pushUiTask( () -> {
            setText(Utils.format(R.string.WndInstallingMod_deleting_file, entry));
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

    @Override
    public void onBackPressed() {
    }
}
