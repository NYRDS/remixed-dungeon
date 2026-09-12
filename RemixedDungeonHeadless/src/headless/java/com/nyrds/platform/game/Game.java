package com.nyrds.platform.game;

import com.nyrds.pixeldungeon.game.GameLoop;
import com.nyrds.pixeldungeon.ml.BuildConfig;
import com.nyrds.pixeldungeon.support.PlayGames;
import com.nyrds.platform.gfx.SystemText;
import com.nyrds.platform.support.Iap;
import com.nyrds.platform.util.PUtil;
import com.watabou.gltextures.TextureCache;
import com.watabou.noosa.InterstitialPoint;
import com.watabou.noosa.Scene;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

// headless: no GL context - a plain thread pumps gameLoop.onFrame()
public class Game {
    private static Game instance;

    private static volatile boolean paused = true;
    public static boolean softPaused;

    protected GameLoop gameLoop;
    public Iap iap = new Iap();
    public PlayGames playGames = new PlayGames();
    public PlayGames playGamesAdapter = playGames;

    // virtual window size, drives PixelScene layout headless
    public static final int VIRTUAL_WIDTH = 800;
    public static final int VIRTUAL_HEIGHT = 480;

    private volatile boolean running = false;
    private Thread loopThread;
    public static volatile int frameInterval = 33; // ms between frames

    public Game(Class<? extends Scene> c) {
        super();
        instance = this;
        gameLoop = new GameLoop(c);

        GameLoop.version = BuildConfig.VERSION_NAME;
        GameLoop.versionCode = BuildConfig.VERSION_CODE;
    }

    public void doRestart() {
        shutdown();
    }

    public static void shutdown() {
        GameLoop.pushUiTask(() -> {
            if (instance != null) {
                instance.stopLoop();
                instance.pause();
                instance.dispose();
            }
            System.exit(0);
        });
    }

    public static void exit(int code) {
        if (instance != null) {
            instance.stopLoop();
        }
        System.exit(code);
    }

    public static void toast(final String text, final Object... args) {
    }

    static public void runOnMainThread(Runnable runnable) {
        GameLoop.pushUiTask(runnable);
    }

    public static boolean smallResScreen() {
        return false;
    }

    public static void syncAdsState() {
    }

    public static void vibrate(int milliseconds) {
    }

    public synchronized static Game instance() {
        return instance;
    }

    public static void openUrl(String prompt, String address) {
        PUtil.slog("game", "openUrl requested: " + address);
    }

    public static void sendEmail(String emailUri, String subject) {
        PUtil.slog("game", "sendEmail requested: " + emailUri);
    }

    static public void openPlayStore() {
    }

    public static boolean isPaused() {
        return paused;
    }

    public void create() {
        SystemText.invalidate();
        TextureCache.clear();
        resize(VIRTUAL_WIDTH, VIRTUAL_HEIGHT);
        resume();
    }

    public void resize(int width, int height) {
        GameLoop.width = width;
        GameLoop.height = height;
        GameLoop.setNeedSceneRestart();
    }

    private void loop() {
        while (running) {
            try {
                if (GameLoop.width == 0 || GameLoop.height == 0) {
                    gameLoop.framesSinceInit = 0;
                } else if (paused) {
                    gameLoop.framesSinceInit = 0;
                } else {
                    gameLoop.onFrame();
                }
                Thread.sleep(frameInterval);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            } catch (Exception e) {
                PUtil.slog("game", "loop error: " + e);
                e.printStackTrace();
            }
        }
    }

    public void startLoop() {
        if (running) {
            return;
        }
        running = true;
        loopThread = new Thread(this::loop, "headless-game-loop");
        loopThread.setDaemon(false);
        loopThread.setUncaughtExceptionHandler((t, e) -> {
            PUtil.slog("game", "loop crashed: " + e);
            e.printStackTrace();
            System.exit(1);
        });
        loopThread.start();
    }

    public void stopLoop() {
        running = false;
        if (loopThread != null) {
            loopThread.interrupt();
            try {
                loopThread.join(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            loopThread = null;
        }
    }

    public void pause() {
        paused = true;

        if (gameLoop.scene != null) {
            gameLoop.scene.pause();
        }

        com.watabou.glscripts.Script.reset();
    }

    public void resume() {
        instance = this;
        gameLoop.onResume();

        if (gameLoop.scene != null) {
            gameLoop.scene.resume();
        }

        paused = false;
    }

    public void dispose() {
        if (gameLoop.scene != null) {
            gameLoop.scene.destroy();
            gameLoop.scene = null;
        }
    }

    public void toggleFullscreen() {
    }

    public static boolean deleteFile(String path) {
        File file = new File(path);
        if (file.exists()) {
            return file.delete();
        }
        return false;
    }

    public static void runOnUiThread(Runnable action) {
        runOnMainThread(action);
    }

    static public void requestInternetPermission(InterstitialPoint returnTo) {
        returnTo.returnToWork(true);
    }

    public InputStream openFileInput(String bonesFile) {
        try {
            return new FileInputStream(bonesFile);
        } catch (Exception e) {
            return null;
        }
    }

    public static void takeScreenshot() {
    }

    public static void updateFpsLimit() {
    }

    static public void copyToClipboard(String label, String text) {
    }
}
