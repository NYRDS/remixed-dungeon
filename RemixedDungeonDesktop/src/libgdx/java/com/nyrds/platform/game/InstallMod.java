package com.nyrds.platform.game;

import com.nyrds.platform.EventCollector;
import com.nyrds.platform.storage.FileSystem;

import com.nyrds.util.Unzip;
import com.nyrds.util.UnzipStateListener;
import com.watabou.noosa.InterstitialPoint;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.pixeldungeon.windows.WndError;
import com.watabou.pixeldungeon.windows.WndMessage;
import com.watabou.pixeldungeon.windows.WndModInstall;
import com.watabou.pixeldungeon.windows.WndModSelect;

import java.io.FileNotFoundException;


import lombok.SneakyThrows;

public class InstallMod extends RemixedDungeon implements UnzipStateListener, InterstitialPoint{

    private boolean permissionsRequested = false;

    public InstallMod() {
    }

    private String modFileName = Utils.EMPTY_STRING;



    public void render() {
        super.render();
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

    @SneakyThrows
    @Override
    public void returnToWork(boolean result) {

        GLog.i("Install mod: %b", result);

        if(!result) {
            return;
        }

        if(gameLoop.scene == null) {
            return;
        }

        Intent intent = getIntent();
        Uri data = intent.getData();

        if(data==null) {
            shutdown();
        }

        toast("Checking %s", String.valueOf(data.getPath()));

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
