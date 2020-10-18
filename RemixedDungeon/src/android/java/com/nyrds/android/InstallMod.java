package com.nyrds.android;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;

import com.nyrds.android.util.FileSystem;
import com.nyrds.android.util.UnzipStateListener;
import com.nyrds.android.util.UnzipTask;
import com.watabou.noosa.Game;
import com.watabou.noosa.InterstitialPoint;
import com.watabou.pixeldungeon.RemixedDungeon;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.pixeldungeon.windows.WndError;
import com.watabou.pixeldungeon.windows.WndMessage;
import com.watabou.pixeldungeon.windows.WndModInstall;
import com.watabou.pixeldungeon.windows.WndModSelect;

import org.jetbrains.annotations.NotNull;

import javax.microedition.khronos.opengles.GL10;

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
            permissionsRequested = false;

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

    @Override
    public void returnToWork(boolean result) {
        if(result) {
            if(scene != null && modFileName.isEmpty()) {
                Intent intent = getIntent();
                Uri data = intent.getData();

                modFileName = data.getLastPathSegment().split(":")[1];
                modUnzipTask = new UnzipTask(this, modFileName, false);
                var modDesc = modUnzipTask.previewMod();
                modUnzipTask.setTgtDir(FileSystem.getExternalStorageFileName(modDesc.name));
                WndModInstall wndModInstall = new WndModInstall(modDesc, () -> Game.execute(modUnzipTask));
                scene.add(wndModInstall);
            }

        }
    }
}
