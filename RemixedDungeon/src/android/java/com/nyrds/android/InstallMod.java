package com.nyrds.android;

import android.content.Intent;
import android.net.Uri;

import com.nyrds.android.util.UnzipStateListener;
import com.nyrds.android.util.UnzipTask;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.RemixedDungeon;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.pixeldungeon.windows.WndError;
import com.watabou.pixeldungeon.windows.WndMessage;
import com.watabou.pixeldungeon.windows.WndModSelect;

import javax.microedition.khronos.opengles.GL10;

public class InstallMod extends RemixedDungeon implements UnzipStateListener {
    public InstallMod() {
    }

    private String modFile = Utils.EMPTY_STRING;

    @Override
    public void onDrawFrame(GL10 gl) {
        super.onDrawFrame(gl);

        if(scene != null && modFile.isEmpty()) {
            Intent intent = getIntent();
            Uri data = intent.getData();

            toast(data.getPath());

            modFile = data.getPath();
            Game.execute(new UnzipTask(this, modFile));
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
                Game.addToScene(new WndError(Utils.format("unzipping %s failed", modFile)));
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
}
