package com.nyrds.pixeldungeon.support;

import android.content.Intent;

import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileFilter;

/**
 * Stub implementation of PlayGamesAdapter for non-Google Play flavors
 * This provides a no-op implementation to allow compilation across all flavors
 */
public class PlayGamesAdapter {
    public static boolean usable() {
        return false;
    }

    public void connect() {
    }

    public boolean isConnected() {
        return false;
    }

    public void connectExplicit() {
    }

    public void disconnect() {
    }

    public void showBadges() {
    }

    public void showLeaderboard() {
    }

    public void backupProgress(IResult resultHandler) {
    }

    public void restoreProgress(IResult resultHandler) {
    }

    public void submitScores(int difficulty, int score) {
    }

    public void unlockAchievement(String achievementCode) {
    }

    public boolean haveSnapshot(String snapshotId) {
        return false;
    }

    public void loadSnapshots(@Nullable final Runnable doneCallback) {
    }

    public boolean packFilesToSnapshot(String id, File dir, FileFilter filter) {
        return false;
    }

    public void unpackSnapshotTo(String snapshotId, File readTo, IResult result) {
    }

    public void showVideoOverlay() {
    }
    
    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        return false;
    }

    public interface IResult {
        void status(final boolean status);
    }
}