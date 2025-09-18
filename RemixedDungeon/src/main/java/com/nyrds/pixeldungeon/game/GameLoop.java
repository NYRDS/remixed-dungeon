package com.nyrds.pixeldungeon.game;

import android.view.KeyEvent;

import com.nyrds.LuaInterface;
import com.nyrds.lua.LuaEngine;
import com.nyrds.platform.ConcurrencyProvider;
import com.nyrds.platform.EventCollector;
import com.nyrds.platform.PlatformAtomicInteger;
import com.nyrds.platform.audio.MusicManager;
import com.nyrds.platform.audio.Sample;
import com.nyrds.platform.game.Game;
import com.nyrds.platform.gfx.SystemText;
import com.nyrds.platform.gl.Gl;
import com.nyrds.platform.gl.NoosaScript;
import com.nyrds.platform.input.Keys;
import com.nyrds.platform.input.PointerEvent;
import com.nyrds.platform.input.Touchscreen;
import com.nyrds.platform.util.TrackedRuntimeException;
import com.nyrds.util.ModdingMode;
import com.nyrds.util.ReportingExecutor;
import com.watabou.gltextures.TextureCache;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Gizmo;
import com.watabou.noosa.Scene;
import com.watabou.pixeldungeon.scenes.InterlevelScene;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.utils.SystemTime;

import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

import java.util.Queue;
import java.util.concurrent.Future;

import lombok.SneakyThrows;
import lombok.val;

public class GameLoop {

    private static final ConcurrencyProvider provider = new ConcurrencyProvider();
    public static PlatformAtomicInteger loadingOrSaving;
    public static final Object stepLock = provider.createLock();

    public static final double[] MOVE_TIMEOUTS = new double[]{250, 500, 1000, 2000, 5000, 10000, 30000, 60000, Double.POSITIVE_INFINITY};

    public static String version = Utils.EMPTY_STRING;
    public static int versionCode = 0;

    public static int width;
    public static int height;

    @SuppressWarnings("unused")
    public static volatile boolean softPaused = false;

    private final ReportingExecutor stepExecutor;
    private final ReportingExecutor executor;
    public ReportingExecutor soundExecutor;

    private final Queue<Runnable> uiTasks;

    private Class<? extends Scene> sceneClass;
    protected static int difficulty = Integer.MAX_VALUE;

    private static float timeScale = 1f;
    public static float elapsed = 0f;

    public int framesSinceInit;

    private static GameLoop instance;

    public Scene scene;
    protected boolean requestedReset = true;

    private long now;
    private long step;

    public Runnable doOnResume;

    public final Queue<PointerEvent> motionEvents;
    public final Queue<KeyEvent> keysEvents;


    public GameLoop(Class<? extends Scene> c) {
        super();
        instance = this;
        sceneClass = c;

        loadingOrSaving = provider.createAtomicInteger(0);
        stepExecutor = provider.createReportingExecutor();
        executor = provider.createReportingExecutor();
        soundExecutor = provider.createReportingExecutor();
        uiTasks = provider.createConcurrentLinkedQueue();
        motionEvents = provider.createConcurrentLinkedQueue();
        keysEvents = provider.createConcurrentLinkedQueue();
    }

    static public GameLoop instance() {
        return instance;
    }

    static public void pushUiTask(Runnable task) {
        instance().uiTasks.add(task);
    }

    static public Future<?> stepExecute(Runnable task) {
        return instance().stepExecutor.submit(task);
    }

    static public void execute(Runnable task) {
        instance().executor.execute(task);
    }

    public static void setNeedSceneRestart() {
        if (!(instance().scene instanceof InterlevelScene)) {
            instance().requestedReset = true;
        }
    }

    public static float getDifficultyFactor() {
        switch (getDifficulty()) {
            case 0:
                return 1f;
            case 1:
            case 2:
                return 1.5f;
            case 3:
                return 2;
            default:
                return 1;
        }
    }

    @LuaInterface
    public static int getDifficulty() {
        return difficulty;
    }

    public static void addToScene(Gizmo gizmo) {
        GameLoop.pushUiTask(() -> {
            Scene scene = scene();
            if (scene != null) {
                scene.add(gizmo);
            }
        });
    }

    @LuaInterface
    public static Scene scene() {
        return instance().scene;
    }

    public static void switchScene(Class<? extends Scene> c) {
        instance().sceneClass = c;
        instance().requestedReset = true;
    }

    @LuaInterface
    public static void resetScene() {
        switchScene(instance().sceneClass);
    }

    static public void runOnMainThread(Runnable runnable) {
        pushUiTask(() -> {
            Game.instance().runOnUiThread(runnable);
        });
    }

    public void onResume() {
        now = 0;

        SystemTime.tick();
        SystemTime.updateLastActionTime();

        SystemText.invalidate();
        TextureCache.clear();

        MusicManager.INSTANCE.enable(GamePreferences.music());
        MusicManager.INSTANCE.resume();

        Sample.INSTANCE.enable(GamePreferences.soundFx());
        Sample.INSTANCE.resume();

        if (doOnResume != null) {
            GameLoop.pushUiTask(() -> {
                doOnResume.run();
                doOnResume = null;
            });
        }
    }

    public void onFrame() {
        SystemTime.tick();
        long rightNow = SystemTime.now();
        step = Math.min((now == 0 ? 0 : rightNow - now), 250);
        now = rightNow;

        framesSinceInit++;

        synchronized (stepLock) {
            if (framesSinceInit > 2) {
                Runnable task;
                while ((task = uiTasks.poll()) != null) {
                    task.run();
                }

                if (!Game.softPaused && loadingOrSaving.get() == 0) {
                    try {
                        if (requestedReset) {
                            requestedReset = false;
                            switchScene(sceneClass.newInstance());
                            return;
                        }
                    } catch (LuaError e) {
                        throw ModdingMode.modException(e);
                    } catch (Exception e) {
                        throw new TrackedRuntimeException(e);
                    }
                }
            }
        }

        if (framesSinceInit > 2 && !Game.softPaused && loadingOrSaving.get() == 0) {
            stepExecutor.execute(this::update);
        }

        NoosaScript.get().resetCamera();
        Gl.clear();
        Gl.flush();

        synchronized (stepLock) {
            if (scene != null) {
                scene.draw();
            }
        }
    }

    @SneakyThrows
    public void update() {
        synchronized (stepLock) {

            Keys.processEvent(new KeyEvent(Keys.Key.BEGIN_OF_FRAME, KeyEvent.ACTION_DOWN));

            while (!motionEvents.isEmpty()) {
                val event = motionEvents.poll();
                if (event != null) {
                    Touchscreen.processEvent(event);
                }
            }

            while (!keysEvents.isEmpty()) {
                val event = keysEvents.poll();
                if (event != null) {
                    Keys.processEvent(event);
                }
            }

            Keys.processEvent(new KeyEvent(Keys.Key.END_OF_FRAME, KeyEvent.ACTION_DOWN));

            elapsed = timeScale * step * 0.001f;
            if (scene != null) {
                scene.update();
            }
            Camera.updateAll();
        }
    }

    private void switchScene(Scene requestedScene) {
        SystemText.invalidate();
        TextureCache.clear();
        Camera.reset();

        if (scene != null) {
            EventCollector.setSessionData("pre_scene", scene.getClass().getSimpleName());
            scene.destroy();
        }
        scene = requestedScene;
        scene.create();
        EventCollector.setSessionData("scene", scene.getClass().getSimpleName());

        elapsed = 0f;
        timeScale = 1f;

        Game.syncAdsState();
    }

    public static void setDifficulty(int difficulty) {
        GameLoop.difficulty = difficulty;
    }

    @LuaInterface
    public static void callByGlobalId(int callbackId, Object... params) {
        // Get the global callbacks table from Lua
        LuaValue callbacks = LuaEngine.getGlobals().get("ItemSelectionCallbacks");
        
        // If the callbacks table exists and has the callback ID
        if (callbacks != null && callbacks.istable()) {
            LuaValue callback = callbacks.get(callbackId);
            if (callback != null && callback.isfunction()) {
                // Convert Java objects to Lua values
                LuaValue[] luaParams = new LuaValue[params.length];
                for (int i = 0; i < params.length; i++) {
                    luaParams[i] = CoerceJavaToLua.coerce(params[i]);
                }
                
                // Call the Lua callback function
                callback.invoke(luaParams);
                
                // Remove the callback from the table to prevent memory leaks
                callbacks.set(callbackId, LuaValue.NIL);
            }
        }
    }
}