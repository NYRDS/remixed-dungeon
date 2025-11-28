package com.nyrds.pixeldungeon.support;

import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileFilter;

/**
 * No-op implementation of Play Games adapter for market_none flavor
 */
public class PlayGamesAdapter {
    public PlayGamesAdapter() {
    }

    public void connectExplicit() {
    }

    public void connect() {
    }

    public void disconnect() {
    }

    public void unlockAchievement(String achievementCode) {
    }

    public static boolean usable() {
        return false;
    }

    public void unpackSnapshotTo(String snapshotId, File readTo, IResult result) {
        if (result != null) {
            result.status(false);
        }
    }

    public boolean haveSnapshot(String snapshotId) {
        return false;
    }

    public boolean isConnected() {
        return false;
    }

    public void loadSnapshots(@Nullable final Runnable doneCallback) {
        if (doneCallback != null) {
            doneCallback.run();
        }
    }

    public boolean packFilesToSnapshot(String id, File dir, FileFilter filter) {
        return false;
    }

    // onActivityResult is not needed in desktop version

    public void backupProgress(final IResult resultCallback) {
        if (resultCallback != null) {
            resultCallback.status(false);
        }
    }

    public void restoreProgress(final IResult resultCallback) {
        if (resultCallback != null) {
            resultCallback.status(false);
        }
    }

    public void showBadges() {
    }

    public void submitScores(int level, int scores) {
    }

    public void showLeaderboard() {
    }

    public void showVideoOverlay() {
    }

    public interface IResult {
        void status(boolean status);
    }
}