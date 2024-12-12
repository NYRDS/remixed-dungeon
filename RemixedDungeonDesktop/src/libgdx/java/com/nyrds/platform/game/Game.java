package com.nyrds.platform.game;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.files.FileHandle;
import com.nyrds.pixeldungeon.game.GameLoop;
import com.nyrds.pixeldungeon.support.PlayGames;
import com.nyrds.platform.app.RemixedDungeonApp;
import com.nyrds.platform.audio.MusicManager;
import com.nyrds.platform.audio.Sample;
import com.nyrds.platform.gfx.SystemText;
import com.nyrds.platform.gl.Gl;
import com.nyrds.platform.input.PointerEvent;
import com.nyrds.platform.support.Iap;
import com.watabou.glscripts.Script;
import com.watabou.gltextures.TextureCache;
import com.watabou.noosa.InterstitialPoint;
import com.watabou.noosa.Scene;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;


public class Game implements ApplicationListener, InputProcessor {
    private static Game instance;

    private static volatile boolean paused = true;
    public static boolean softPaused;

    protected GameLoop gameLoop;
    public Iap iap = new Iap();

    public PlayGames playGames = new PlayGames();

    public Game(Class<? extends Scene> c) {
        super();
        instance = this;
        gameLoop = new GameLoop(c);

        GameLoop.version = "None";
        GameLoop.versionCode = 0;
    }

    public void doRestart() {
        RemixedDungeonApp.restartApp();
    }

    public static void shutdown() {
        paused = true;
        System.exit(0);
    }

    public static void toast(final String text, final Object... args) { }


    static public void runOnMainThread(Runnable runnable) {
        GameLoop.pushUiTask( runnable );
    }

    public static boolean smallResScreen() {
        return false;
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

    public static void openUrl(String prompt, String address) {
    }

    public static void sendEmail(String emailUri, String subject) {
    }

    static public void openPlayStore() {
    }

    @Override
    public void create() {
        SystemText.invalidate();
        TextureCache.clear();
        Gdx.input.setInputProcessor(this);

        resume();
    }

    @Override
    public void resize(int width, int height) {
        GameLoop.width=width;
        GameLoop.height=height;
        GameLoop.setNeedSceneRestart();
    }

    @Override
    public void render() {
        if (instance() == null || GameLoop.width == 0 || GameLoop.height == 0) {
            gameLoop.framesSinceInit = 0;
            return;
        }

        if (paused) {
            gameLoop.framesSinceInit = 0;
            return;
        }

        Gdx.gl20.glEnable(Gdx.gl20.GL_BLEND);
        Gl.blendSrcAlphaOneMinusAlpha();
        Gdx.gl20.glEnable(Gdx.gl20.GL_SCISSOR_TEST);

        gameLoop.onFrame();
    }

    @Override
    public void pause() {
        paused = true;

        if (gameLoop.scene != null) {
            gameLoop.scene.pause();
        }

        MusicManager.INSTANCE.pause();
        Sample.INSTANCE.pause();
        Sample.INSTANCE.reset();

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

        MusicManager.INSTANCE.mute();
    }

    @Override
    public boolean keyDown(int keycode) {
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        gameLoop.motionEvents.add( new PointerEvent(screenX, screenY, pointer, button, PointerEvent.Type.TOUCH_DOWN));
        return true;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        gameLoop.motionEvents.add( new PointerEvent(screenX, screenY, pointer, button, PointerEvent.Type.TOUCH_UP));
        return true;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        gameLoop.motionEvents.add( new PointerEvent(screenX, screenY, pointer, 0, PointerEvent.Type.TOUCH_DRAGGED));
        return true;
    }

    @Override
    public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        return false;
    }


    public static boolean deleteFile(String path) {
        FileHandle file = Gdx.files.local(path);
        if (file.exists()) {
            file.delete();
            return true;
        }
        return false;
    }

    public final void runOnUiThread(Runnable action) {
            action.run();
    }

    static public void requestInternetPermission(InterstitialPoint returnTo) {
        returnTo.returnToWork(true);
    }

    public InputStream openFileInput(String bonesFile) {
        try {
            return Gdx.files.local(bonesFile).read();
        } catch (Exception e) {
            return null;
        }
    }
}
