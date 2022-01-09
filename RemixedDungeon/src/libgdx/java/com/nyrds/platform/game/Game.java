package com.nyrds.platform.game;


import android.widget.LinearLayout;

import com.nyrds.pixeldungeon.game.GameLoop;
import com.nyrds.pixeldungeon.support.Iap;
import com.nyrds.pixeldungeon.support.PlayGames;
import com.watabou.noosa.InterstitialPoint;
import com.watabou.noosa.Scene;

import org.jetbrains.annotations.NotNull;


public class Game {
    private static Game instance;

    private static volatile boolean paused = true;

    protected GameLoop gameLoop;

    public PlayGames playGames = new PlayGames();
    public Iap iap = new Iap();

    public Game(Class<? extends Scene> c) {
        super();
        instance = this;
        gameLoop = new GameLoop(c);

        GameLoop.version = "None";
        GameLoop.versionCode = 0;
    }

    public void doRestart() {
    }

    public static void shutdown() {
        paused = true;
        System.exit(0);
    }

    public static void toast(final String text, final Object... args) {

    }


    static public void runOnMainThread(Runnable runnable) {
        GameLoop.pushUiTask( runnable );
    }


    public static void syncAdsState() {
    }



    public static boolean isPaused() {
        return paused;
    }


    public static void vibrate(int milliseconds) {
    }

    public synchronized static Game instance() {
        return instance;
    }

    public void doPermissionsRequest(@NotNull InterstitialPoint returnTo, String[] permissions) {
        returnTo.returnToWork(true);
    }


    public LinearLayout getLayout() {
        return null;
    }

    public void openUrl(String prompt, String address) {
    }

    public void sendEmail(String emailUri, String subject) {
    }

    static public void openPlayStore() {

    }
}
