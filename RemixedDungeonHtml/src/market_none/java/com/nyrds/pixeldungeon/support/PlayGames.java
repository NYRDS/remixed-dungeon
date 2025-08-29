package com.nyrds.pixeldungeon.support;

import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileFilter;

/**
 * HTML version of PlayGames with market_none flavor
 */
public class PlayGames {
    public static boolean usable() {
        // Play Games services are not supported in HTML
        return false;
    }

    public void connect() {
        // Play Games services are not supported in HTML
        System.out.println("Play Games connect not supported in HTML version");
    }

    public boolean isConnected() {
        // Play Games services are not supported in HTML
        return false;
    }

    public void connectExplicit() {
        // Play Games services are not supported in HTML
        System.out.println("Play Games connect explicit not supported in HTML version");
    }

    public void disconnect() {
        // Play Games services are not supported in HTML
        System.out.println("Play Games disconnect not supported in HTML version");
    }

    public void showBadges() {
        // Play Games services are not supported in HTML
        System.out.println("Play Games badges not supported in HTML version");
    }

    public void showLeaderboard() {
        // Play Games services are not supported in HTML
        System.out.println("Play Games leaderboard not supported in HTML version");
    }

    public void backupProgress(Object resultHandler) {
        // Play Games services are not supported in HTML
        System.out.println("Play Games backup progress not supported in HTML version");
    }

    public void restoreProgress(Object resultHandler) {
        // Play Games services are not supported in HTML
        System.out.println("Play Games restore progress not supported in HTML version");
    }

    public void submitScores(int difficulty, int score) {
        // Play Games services are not supported in HTML
        System.out.println("Play Games submit scores not supported in HTML version");
    }

    public void unlockAchievement(String achievementCode) {
        // Play Games services are not supported in HTML
        System.out.println("Play Games unlock achievement not supported in HTML version");
    }

    public boolean haveSnapshot(String snapshotId) {
        // Play Games services are not supported in HTML
        return false;
    }

    public void loadSnapshots(@Nullable final Runnable doneCallback) {
        // Play Games services are not supported in HTML
        System.out.println("Play Games load snapshots not supported in HTML version");
        if (doneCallback != null) {
            doneCallback.run();
        }
    }

    public boolean packFilesToSnapshot(String id, File dir, FileFilter filter) {
        // Play Games services are not supported in HTML
        System.out.println("Play Games pack files to snapshot not supported in HTML version");
        return false;
    }

    public void unpackSnapshotTo(String snapshotId, File readTo, IResult result) {
        // Play Games services are not supported in HTML
        System.out.println("Play Games unpack snapshot not supported in HTML version");
        if (result != null) {
            result.status(false);
        }
    }

    public void showVideoOverlay() {
        // Play Games services are not supported in HTML
        System.out.println("Play Games video overlay not supported in HTML version");
    }

    public interface IResult {
        void status(final boolean status);
    }
}