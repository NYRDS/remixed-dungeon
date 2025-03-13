package com.nyrds.platform.game;

import android.view.KeyEvent;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.nyrds.pixeldungeon.game.GameLoop;
import com.nyrds.pixeldungeon.game.GamePreferences;
import com.nyrds.pixeldungeon.ml.BuildConfig;
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
import com.watabou.noosa.Camera;
import com.watabou.noosa.InterstitialPoint;
import com.watabou.noosa.Scene;
import com.watabou.pixeldungeon.scenes.GameScene;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.Deflater;

import lombok.Getter;

public class Game implements ApplicationListener, InputProcessor {
    private static Game instance;

    @Getter
    private static volatile boolean paused = true;
    public static boolean softPaused;

    protected GameLoop gameLoop;
    public Iap iap = new Iap();
    public PlayGames playGames = new PlayGames();

    private final Map<Integer, Long> keyDownTimes = new HashMap<>();
    private static final long AUTO_FIRE_INTERVAL = 250;

    public Game(Class<? extends Scene> c) {
        super();
        instance = this;
        gameLoop = new GameLoop(c);

        GameLoop.version = BuildConfig.VERSION_NAME;
        GameLoop.versionCode = BuildConfig.VERSION_CODE;
    }

    public void doRestart() {
        RemixedDungeonApp.restartApp();
    }

    public static void shutdown() {
        GameLoop.pushUiTask(() -> {
            instance.pause();
            instance.dispose();
            System.exit(0);
        });
    }

    public static void toast(final String text, final Object... args) { }

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
        Gdx.net.openURI(address);
    }

    public static void sendEmail(String emailUri, String subject) {
        Gdx.net.openURI("mailto:" + emailUri + "?subject=" + subject);
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
        GameLoop.width = width;
        GameLoop.height = height;
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

        // Check for auto-fire events
        long currentTime = System.currentTimeMillis();
        for (Map.Entry<Integer, Long> entry : keyDownTimes.entrySet()) {
            int keycode = entry.getKey();
            long lastFireTime = entry.getValue();
            if (currentTime - lastFireTime >= AUTO_FIRE_INTERVAL) {
                GameLoop.instance().keysEvents.add(new KeyEvent(keycode, KeyEvent.ACTION_DOWN));
                keyDownTimes.put(keycode, currentTime); // Update the last fire time
            }
        }

        if (BuildConfig.DEBUG && Gdx.input.isButtonPressed(Input.Buttons.RIGHT)) {
            takeScreenshot();
        }
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

    private int w_width = 800, w_height = 450;
    public void toggleFullscreen() {
        pause();
        Graphics graphics = Gdx.graphics;

        if (graphics.isFullscreen()) {
            graphics.setWindowedMode(w_width, w_height);
        } else {
            w_width = graphics.getWidth();
            w_height = graphics.getHeight();

            Graphics.DisplayMode displayMode = graphics.getDisplayMode();
            graphics.setFullscreenMode(displayMode);
        }
        resume();
    }

    @Override
    public boolean keyDown(int keycode) {
        //PUtil.slog("key", "keyDown: " +  keycode + " " + Input.Keys.toString(keycode));
        GameLoop.instance().keysEvents.add(new KeyEvent(keycode, KeyEvent.ACTION_DOWN));
        keyDownTimes.put(keycode, System.currentTimeMillis()); // Record the time when the key was pressed

        if (keycode == Input.Keys.F11) {
            toggleFullscreen();
        }

        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        GameLoop.instance().keysEvents.add(new KeyEvent(keycode, KeyEvent.ACTION_UP));
        keyDownTimes.remove(keycode); // Remove the key from the map when it's released
        return true;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        gameLoop.motionEvents.add(new PointerEvent(screenX, screenY, pointer, button, PointerEvent.Type.TOUCH_DOWN));
        return true;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        gameLoop.motionEvents.add(new PointerEvent(screenX, screenY, pointer, button, PointerEvent.Type.TOUCH_UP));
        return true;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        gameLoop.motionEvents.add(new PointerEvent(screenX, screenY, pointer, 0, PointerEvent.Type.TOUCH_DRAGGED));
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
        if (GameLoop.scene() instanceof GameScene) {
            GamePreferences.zoom(GamePreferences.zoom() - amountY / 10);
            Camera.main.zoom((float) (GameScene.defaultZoom + GamePreferences.zoom()));
        }
        return true;
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

    public static void takeScreenshot() {
        int width = Gdx.graphics.getWidth();
        int height = Gdx.graphics.getHeight();

        Pixmap pixmap = Pixmap.createFromFrameBuffer(0, 0, width, height);

        long timestamp = System.currentTimeMillis();

        PixmapIO.writePNG(Gdx.files.local("screenshot" + timestamp + ".png"), pixmap, Deflater.DEFAULT_COMPRESSION, true);

        pixmap.dispose();
    }

    public static void updateFpsLimit() {
        int[] limit = {30, 60, 120};
        Gdx.graphics.setForegroundFPS(limit[GamePreferences.fps_limit()]);
    }
}