package com.nyrds.pixeldungeon.windows;

import com.nyrds.pixeldungeon.game.GameLoop;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.storage.FileSystem;
import com.nyrds.platform.util.Os;
import com.nyrds.platform.util.StringsManager;
import com.nyrds.util.ParallelDownloadTask;
import com.nyrds.util.Mods;
import com.watabou.noosa.Group;
import com.watabou.pixeldungeon.ui.Icons;
import com.watabou.pixeldungeon.ui.ModsButton;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.pixeldungeon.windows.WndOptions;
import com.watabou.pixeldungeon.windows.WndTitledMessage;

import java.io.File;

public class WndModsDisclaimer extends WndOptions {
    private final ModsButton modsButton;
    private final boolean result;
    private final Group parent;

    static String [] platformOptions;
    static {
        if (Utils.isAndroid()) {
            platformOptions = new String[] {StringsManager.getVar(R.string.WndModsDisclaimer_manage),
                    StringsManager.getVar(R.string.WndModsDisclaimer_install)};
        } else {
            platformOptions =  new String[] {StringsManager.getVar(R.string.WndModsDisclaimer_manage)};
        }

    }

    public WndModsDisclaimer(ModsButton modsButton, boolean result, Group parent) {

        super(StringsManager.getVar(R.string.WndModsDisclaimer_title),
                StringsManager.getVar(R.string.Mods_Disclaimer),platformOptions);

        this.modsButton = modsButton;
        this.result = result;
        this.parent = parent;
    }

    private void modsList() {
        GameLoop.pushUiTask(() -> {
            if (result) {
                if (Os.isConnectedToInternet()) {
                    File modsCommon = FileSystem.getExternalStorageFile(Mods.MODS_COMMON_JSON);
                    modsCommon.delete();
                    String downloadTo = modsCommon.getAbsolutePath();

                    // Try both URLs in parallel - first one to succeed will be used
                    String[] urls = {
                        "https://ru.nyrds.net/rpd/mods2.json",
                        "https://nyrds.net/rpd/mods2.json"
                    };
                    
                    GameLoop.execute(new ParallelDownloadTask(new DownloadProgressWindow("Downloading", modsButton),
                            urls,
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
