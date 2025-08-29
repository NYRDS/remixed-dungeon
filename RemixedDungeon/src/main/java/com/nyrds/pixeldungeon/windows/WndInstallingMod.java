package com.nyrds.pixeldungeon.windows;

import com.nyrds.pixeldungeon.game.GameLoop;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.storage.AndroidSAF;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.ui.BusyIndicator;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.pixeldungeon.windows.WndTitledMessage;

public class WndInstallingMod extends WndTitledMessage implements AndroidSAF.IListener {

    public WndInstallingMod() {
        super(new BusyIndicator(), StringsManager.getVar(R.string.WndInstallingMod_please_wait), "");
    }

    private void showText(String text) {
        GameLoop.pushUiTask( () -> {
            setText(text);
        });
    }

    @Override
    public void onMessage(String message) {
        showText(String.format(message));
    }

    @Override
    public void onFileCopy(String path) {
        showText(String.format(StringsManager.getVar(R.string.WndInstallingMod_copying_file), path));
    }

    @Override
    public void onFileSkip(String path) {
        showText(Utils.format(R.string.WndInstallingMod_skipping_file, path));
    }

    @Override
    public void onFileDelete(String entry) {
        showText(Utils.format(R.string.WndInstallingMod_deleting_file, entry));
    }

    @Override
    public void onFileSelected(String path) {
        // Handle file selection
        showText(Utils.format(R.string.WndInstallingMod_selected_file, path));
    }

    @Override
    public void onFileSelectionCancelled() {
        // Handle file selection cancellation
        hide();
    }

    @Override
    public void onComplete() {
        hide();
    }

    @Override
    public void hide()  {
        super.hide();
        AndroidSAF.setListener(null);
    }

    @Override
    public void onBackPressed() {
    }
}
