package com.nyrds.platform.game;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.nyrds.pixeldungeon.game.GameLoop;
import com.nyrds.platform.EventCollector;
import com.nyrds.platform.storage.FileSystem;
import com.nyrds.util.Unzip;
import com.nyrds.util.UnzipStateListener;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.pixeldungeon.windows.WndError;
import com.watabou.pixeldungeon.windows.WndMessage;
import com.watabou.pixeldungeon.windows.WndModInstall;
import com.watabou.pixeldungeon.windows.WndModSelect;

import java.io.FileNotFoundException;

import lombok.SneakyThrows;


public class InstallMod extends RemixedDungeon implements UnzipStateListener {

    public InstallMod() {
    }

    private String modFileName = Utils.EMPTY_STRING;

    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        intent = getIntent();
    }


    private WndMessage unzipProgress;

    @Override
    public void UnzipComplete(final Boolean result) {
        GameLoop.pushUiTask(() -> {
            if(unzipProgress!=null) {
                unzipProgress.hide();
                unzipProgress = null;
            }

            if (result) {
                GameLoop.addToScene(new WndModSelect());
            } else {
                GameLoop.addToScene(new WndError(Utils.format("unzipping %s failed", modFileName)));
            }
        });

    }

    @Override
    public void UnzipProgress(Integer unpacked) {
        GameLoop.pushUiTask(() -> {
            if (unzipProgress == null) {
                unzipProgress = new WndMessage("Unpacking: ...") {
                    @Override
                    public void onBackPressed() { }
                };
                GameLoop.addToScene(unzipProgress);
            }
            if (unzipProgress.getParent() == GameLoop.scene()) {
                unzipProgress.setText(Utils.format("Unpacking: %d", unpacked));
            }
        });
    }

    public static void openPlayStore() {
        final String appPackageName = instance().getPackageName();
        try {
            instance().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
        } catch (android.content.ActivityNotFoundException anfe) {
            instance().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
        }
    }

    @SneakyThrows

    public void installMod() {

        if(gameLoop.scene == null) {
            GLog.debug("No scene found");
            return;
        }

        Uri data = intent.getData();

        if(data==null) {
            shutdown();
        }

        toast("Checking %s", String.valueOf(data.getPath()));
        GLog.debug("%s", data.getPath());

        var modDesc = Unzip.inspectMod(getContentResolver().openInputStream(data));
        modFileName = modDesc.name;

        EventCollector.logEvent("ManualModInstall", modDesc.name, String.valueOf(modDesc.version));

        WndModInstall wndModInstall = new WndModInstall(modDesc,
                () -> GameLoop.execute(
                        () -> {
                            try {
                                Unzip.unzipStream(getContentResolver().openInputStream(data), FileSystem.getExternalStorageFileName("./"), this);
                            } catch (FileNotFoundException e) {
                                EventCollector.logException(e);
                            }
                        }));
        gameLoop.scene.add(wndModInstall);
    }
}
