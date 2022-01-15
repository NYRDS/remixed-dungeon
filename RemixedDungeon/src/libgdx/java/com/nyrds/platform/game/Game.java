package com.nyrds.platform.game;


import android.widget.LinearLayout;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.nyrds.pixeldungeon.game.GameLoop;
import com.nyrds.pixeldungeon.support.Iap;
import com.nyrds.pixeldungeon.support.PlayGames;
import com.nyrds.platform.audio.Music;
import com.nyrds.platform.audio.Sample;
import com.watabou.glscripts.Script;
import com.watabou.gltextures.TextureCache;
import com.watabou.noosa.InterstitialPoint;
import com.watabou.noosa.Scene;

import org.jetbrains.annotations.NotNull;


public class Game implements ApplicationListener {
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

    public static void toast(final String text, final Object... args) { }


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

    @Override
    public void create() {
        paused = false; // we may not get resume

        Gdx.gl20.glEnable(Gdx.gl20.GL_BLEND);
        Gdx.gl20.glBlendFunc(Gdx.gl20.GL_SRC_ALPHA, Gdx.gl20.GL_ONE_MINUS_SRC_ALPHA);

        Gdx.gl20.glEnable(Gdx.gl20.GL_SCISSOR_TEST);


        //SystemText.invalidate();
        TextureCache.clear();
    }

    @Override
    public void resize(int width, int height) {
        GameLoop.width(width);
        GameLoop.height(height);
        GameLoop.setNeedSceneRestart();
    }

    @Override
    public void render() {
        if (instance() == null || GameLoop.width() == 0 || GameLoop.height() == 0) {
            gameLoop.framesSinceInit = 0;
            return;
        }

        if (paused) {
            gameLoop.framesSinceInit = 0;
            return;
        }


        gameLoop.onFrame();
    }

    @Override
    public void pause() {
        paused = true;

        if (gameLoop.scene != null) {
            gameLoop.scene.pause();
        }

        Music.INSTANCE.pause();
        Sample.INSTANCE.pause();

        Script.reset();
    }

    @Override
    public void resume() {
        instance = this;
        gameLoop.onResume();

        if (gameLoop.scene != null) {
            gameLoop.scene.resume();
        }

        paused = false;
    }

    @Override
    public void dispose() {
        if (gameLoop.scene != null) {
            gameLoop.scene.destroy();
            gameLoop.scene = null;
        }

        Music.INSTANCE.mute();
        Sample.INSTANCE.reset();
    }
}
