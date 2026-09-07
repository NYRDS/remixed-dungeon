package com.nyrds.platform.game;

import android.view.KeyEvent;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.github.xpenatan.gdx.backends.teavm.TeaFiles;
import com.nyrds.pixeldungeon.game.GameLoop;
import com.nyrds.pixeldungeon.ml.BuildConfig;
import com.nyrds.platform.audio.WebAudio;
import com.nyrds.platform.storage.PersistedFileStorage;
import com.nyrds.pixeldungeon.support.PlayGames;
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
import com.watabou.pixeldungeon.utils.GLog;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;

public class Game implements ApplicationListener, InputProcessor {
    private static Game instance;

    @Getter
    private static volatile boolean paused = true;
    public static boolean softPaused;

    protected GameLoop gameLoop;
    public Iap iap = new Iap();
    public PlayGames playGames = new PlayGames();
    public PlayGames playGamesAdapter = playGames; // For compatibility with main source code that expects PlayGamesAdapter
    
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
        // Restarting is not supported in HTML
    }

    public static void shutdown() {
        GameLoop.pushUiTask(() -> {
            instance.pause();
            instance.dispose();
            // We can't actually exit in HTML, just pause
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
        // Vibration is not supported in HTML
    }

    public synchronized static Game instance() {
        return instance;
    }

    public static void openUrl(String prompt, String address) {
        Gdx.net.openURI(address);
    }

    static public void copyToClipboard(String label, String text) {
        // Clipboard not directly available in HTML without JSNI
        Gdx.app.log("Game", "copyToClipboard: " + label);
    }

    public static void sendEmail(String emailUri, String subject) {
        Gdx.net.openURI("mailto:" + emailUri + "?subject=" + subject);
    }

    static public void openPlayStore() {
    }

    @Override
    public void create() {
        installPlatformBackingStores();

        SystemText.invalidate();
        TextureCache.clear();
        Gdx.input.setInputProcessor(this);

        resume();
    }

    // TeaApplication.initGdx() wires Gdx.files to a memory-only local storage
    // (saves would die with the page) and leaves Gdx.audio null (sound is
    // silent). Swap in the persisted local storage and the WebAudio factory
    // before anything reads saves or plays audio.
    private void installPlatformBackingStores() {
        if (Gdx.files instanceof TeaFiles) {
            ((TeaFiles) Gdx.files).localStorage = new PersistedFileStorage();
        }
        if (Gdx.audio == null) {
            Gdx.audio = new WebAudio();
        }
    }

    @Override
    public void resize(int width, int height) {
        GameLoop.width = width;
        GameLoop.height = height;
        GameLoop.setNeedSceneRestart();
    }

    @Override
    public void render() {
        touchJsHeartbeat();

        if (instance() == null || GameLoop.width == 0 || GameLoop.height == 0) {
            gameLoop.framesSinceInit = 0;
            return;
        }

        if (paused) {
            // TeaApplication pauses us on visibilitychange "hidden", but the
            // matching "visible" resume can be missed (fired while booting,
            // before initState reaches APP_LOOP, or dropped by the embedded
            // pane). Self-heal: once the document is visible again, resume.
            if (isDocumentVisible()) {
                GLog.debug("paused but document visible - resuming");
                resume();
            } else {
                gameLoop.framesSinceInit = 0;
                return;
            }
        }

        Gdx.gl20.glEnable(GL20.GL_BLEND);
        Gl.blendSrcAlphaOneMinusAlpha();
        Gdx.gl20.glEnable(GL20.GL_SCISSOR_TEST);

        try {
            gameLoop.onFrame();
        } catch (Throwable t) {
            // TeaVM keeps the original JS Error (with a readable stack) on the
            // throwable - surface it, since getStackTrace() is empty on web
            Throwable cur = t;
            while (cur.getCause() != null && cur.getCause() != cur) {
                cur = cur.getCause();
            }
            dumpJsException(cur);
            throw t;
        }

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
            // Screenshot functionality not supported in HTML
        }

        renderDoneMarker();
    }

    // browser debug: set after render() fully returns; distinguishes "hung
    // inside onFrame" from "returned but loop not rescheduled"
    @org.teavm.jso.JSBody(script =
            "var s = window.__gameState || (window.__gameState = {}); s.renderDone = Date.now();")
    private static native void renderDoneMarker();

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

    public void toggleFullscreen() {
        // Fullscreen toggle is handled by the browser in HTML
    }

    @Override
    public boolean keyDown(int keycode) {
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
            com.nyrds.pixeldungeon.game.GamePreferences.zoom(
                com.nyrds.pixeldungeon.game.GamePreferences.zoom() - amountY / 10);
            Camera.main.zoom((float) (
                com.watabou.pixeldungeon.scenes.GameScene.defaultZoom + 
                com.nyrds.pixeldungeon.game.GamePreferences.zoom()));
        }
        return true;
    }

    public static boolean deleteFile(String path) {
        // File deletion is limited in HTML
        try {
            FileHandle file = Gdx.files.local(path);
            if (file.exists()) {
                return file.delete();
            }
        } catch (Exception e) {
            // File operations are limited in HTML
        }
        return false;
    }

    public static void runOnUiThread(Runnable action) {
        Gdx.app.postRunnable(action);
    }

    public InputStream openFileInput(String bonesFile) {
        try {
            return Gdx.files.local(bonesFile).read();
        } catch (Exception e) {
            return null;
        }
    }

    public static void updateFpsLimit() {
        // FPS limit is handled by the browser in HTML
    }
    
    static public void requestInternetPermission(InterstitialPoint returnTo) {
        // In HTML, we assume permission is always granted
        if (returnTo != null) {
            returnTo.returnToWork(true);
        }
    }

    // caveman: watchdog support - TeaVM has no Thread.getAllStackTraces()
    public static void dumpThreadStacks() {
        GLog.toFile("WATCHDOG: thread stacks not available on web");
    }

    @org.teavm.jso.JSBody(params = "t", script =
            "var je = t && t['$jsException'];"
            + "console.error('CAUSE-JSSTACK: ' + (je && je.stack ? je.stack : (t && t.stack)));")
    private static native void dumpJsException(Throwable t);

    // browser debug heartbeat: window.__gameState.frame = ms timestamp of the
    // last TeaApplication render() entry; frames = total render() count
    @org.teavm.jso.JSBody(script =
            "var s = window.__gameState || (window.__gameState = {}); s.frames = (s.frames || 0) + 1; s.frame = Date.now();")
    private static native void touchJsHeartbeat();

    @org.teavm.jso.JSBody(script = "return document.visibilityState === 'visible';")
    private static native boolean isDocumentVisible();
}