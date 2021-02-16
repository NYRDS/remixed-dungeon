package com.nyrds.android;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;

import com.nyrds.android.util.FileSystem;
import com.nyrds.android.util.Unzip;
import com.nyrds.android.util.UnzipStateListener;
import com.nyrds.android.util.UnzipTask;
import com.nyrds.pixeldungeon.ml.EventCollector;
import com.watabou.noosa.Game;
import com.watabou.noosa.InterstitialPoint;
import com.watabou.pixeldungeon.RemixedDungeon;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.pixeldungeon.windows.WndError;
import com.watabou.pixeldungeon.windows.WndMessage;
import com.watabou.pixeldungeon.windows.WndModInstall;
import com.watabou.pixeldungeon.windows.WndModSelect;

import org.jetbrains.annotations.NotNull;

import javax.microedition.khronos.opengles.GL10;

import lombok.SneakyThrows;
import lombok.var;

public class InstallMod extends RemixedDungeon implements UnzipStateListener, @NotNull InterstitialPoint {

    private UnzipTask modUnzipTask;

    private boolean permissionsRequested = false;

    public InstallMod() {
    }

    private String modFileName = Utils.EMPTY_STRING;


    @Override
    public void onDrawFrame(GL10 gl) {
        super.onDrawFrame(gl);

        if(!permissionsRequested) {
            permissionsRequested = true;

            String[] requiredPermissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
            Game.instance().doPermissionsRequest(this, requiredPermissions);
        }
    }

    private WndMessage unzipProgress;

    @Override
    public void UnzipComplete(final Boolean result) {
        Game.pushUiTask(() -> {
            if(unzipProgress!=null) {
                unzipProgress.hide();
                unzipProgress = null;
            }

            if (result) {
                Game.addToScene(new WndModSelect());
            } else {
                Game.addToScene(new WndError(Utils.format("unzipping %s failed", modFileName)));
            }
        });

    }

    @Override
    public void UnzipProgress(Integer unpacked) {
        Game.pushUiTask(() -> {
            if (unzipProgress == null) {
                unzipProgress = new WndMessage(Utils.EMPTY_STRING);
                Game.addToScene(unzipProgress);
            }
            if (unzipProgress.getParent() == Game.scene()) {
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
    @Override
    public void returnToWork(boolean result) {

        GLog.i("Install mod: %b", result);

        if(!result) {
            return;
        }

        if(scene == null) {
            return;
        }

        Intent intent = getIntent();
        Uri data = intent.getData();

        if(data==null) {
            shutdown();
        }

        var modStream = getContentResolver().openInputStream(data);
        var modDesc = Unzip.inspectMod(modStream);

        EventCollector.logEvent("ManualModInstall", modDesc.name, String.valueOf(modDesc.version));

        WndModInstall wndModInstall = new WndModInstall(modDesc,
                () -> Game.execute(
                        () -> Unzip.unzipStream(modStream, FileSystem.getExternalStorageFileName(modDesc.name), null)));
        scene.add(wndModInstall);

    }
}
