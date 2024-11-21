package com.nyrds.pixeldungeon.windows;

import com.nyrds.pixeldungeon.game.GameLoop;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.storage.FileSystem;
import com.nyrds.platform.util.StringsManager;
import com.nyrds.util.DownloadTask;
import com.nyrds.util.Mods;
import com.nyrds.util.Util;
import com.watabou.noosa.Group;
import com.watabou.pixeldungeon.ui.Icons;
import com.watabou.pixeldungeon.ui.ModsButton;
import com.watabou.pixeldungeon.windows.WndOptions;
import com.watabou.pixeldungeon.windows.WndTitledMessage;

import java.io.File;

public class WndModsDisclaimer extends WndOptions {
    private final ModsButton modsButton;
    private final boolean result;
    private final Group parent;

    public WndModsDisclaimer(ModsButton modsButton, boolean result, Group parent) {
        super(StringsManager.getVar(R.string.WndModsDisclaimer_title),
                StringsManager.getVar(R.string.Mods_Disclaimer),
                StringsManager.getVar(R.string.WndModsDisclaimer_manage),
                StringsManager.getVar(R.string.WndModsDisclaimer_install));
        this.modsButton = modsButton;
        this.result = result;
        this.parent = parent;
    }

    private void modsList() {
        GameLoop.pushUiTask(() -> {
            if (result) {
                if (Util.isConnectedToInternet()) {
                    File modsCommon = FileSystem.getExternalStorageFile(Mods.MODS_COMMON_JSON);
                    modsCommon.delete();
                    String downloadTo = modsCommon.getAbsolutePath();

                    GameLoop.execute(new DownloadTask(new DownloadProgressWindow("Downloading", modsButton),
                            "https://nyrds.github.io/NYRDS/mods2.json",
                            downloadTo));

                } else {
                    modsButton.DownloadComplete("no internet", true);
                }

            } else {
                parent.add(new WndTitledMessage(Icons.get(Icons.SKULL), "No permissions granted", "No permissions granted"));
            }
        });
    }

    @Override
    public void onSelect(int index) {
        switch (index)  {
            case 0:
                modsList();
                break;
            case 1:
                GameLoop.pushUiTask(() -> {
                    GameLoop.addToScene(new WndLocalModInstall());
                });
                break;
        }
    }

}
