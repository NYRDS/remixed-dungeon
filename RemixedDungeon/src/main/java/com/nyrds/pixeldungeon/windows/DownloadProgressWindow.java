package com.nyrds.pixeldungeon.windows;

import com.nyrds.pixeldungeon.game.GameLoop;
import com.nyrds.util.DownloadStateListener;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.pixeldungeon.windows.WndMessage;

import org.jetbrains.annotations.NotNull;

/**
 * Created by mike on 14.04.2018.
 * This file is part of Remixed Pixel Dungeon.
 */
public class DownloadProgressWindow implements DownloadStateListener {
    private WndMessage        progress;
    private final String            prefix;
    private final IDownloadComplete onComplete;

    public DownloadProgressWindow(@NotNull String prefix, @NotNull IDownloadComplete onComplete) {
        this.prefix = prefix;
        this.onComplete = onComplete;
    }

    @Override
    public void DownloadProgress(final String file, final Integer bytes) {
        GameLoop.pushUiTask(() -> {
            if (progress == null) {
                progress = new WndMessage(Utils.format("%s ...", prefix)) {
                    @Override
                    public void onBackPressed() { }
                };
                GameLoop.addToScene(progress);
            }
            if (progress.getParent() == GameLoop.scene()) {
                progress.setText(Utils.format("%s  %4.2fMb", prefix, bytes / 1024f / 1024f));
            }
        });
    }

    @Override
    public void DownloadComplete(final String file, final Boolean result) {
        GameLoop.pushUiTask(() -> {
            if (progress != null) {
                progress.hide();
                progress = null;
            }

            onComplete.DownloadComplete(file, result);
        });
    }
}
