package com.nyrds.pixeldungeon.game;

import android.view.KeyEvent;

import com.nyrds.LuaInterface;
import com.nyrds.platform.EventCollector;
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

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import lombok.SneakyThrows;

public class GameLoop {

    public static final AtomicInteger loadingOrSaving = new AtomicInteger();
    public static final Object stepLock = new Object();


    public static final double[] MOVE_TIMEOUTS = new double[]{250, 500, 1000, 2000, 5000, 10000, 30000, 60000, Double.POSITIVE_INFINITY };

    public static String version = Utils.EMPTY_STRING;
    public static int versionCode = 0;

    // Actual size of the screen
    public static int width;
    public static int height;

    @SuppressWarnings("unused")
    public static volatile boolean softPaused = false;

    private final ReportingExecutor stepExecutor = new ReportingExecutor();
    private final ReportingExecutor executor = new ReportingExecutor();
    public ReportingExecutor soundExecutor = new ReportingExecutor();

    private final ConcurrentLinkedQueue<Runnable> uiTasks = new ConcurrentLinkedQueue<>();

    // New scene class
    private Class<? extends Scene> sceneClass;
    protected static int difficulty = Integer.MAX_VALUE;

    private static float timeScale = 1f;
    public static float elapsed = 0f;

    public int framesSinceInit;

    private static GameLoop instance;

    // Current scene
    public Scene scene;
    // true if scene switch is requested
    protected boolean requestedReset = true;

    // Current time in milliseconds
    private long now;
    // Milliseconds passed since previous update
    private long step;


    public Runnable doOnResume;

    // Accumulated touch events
    public final ConcurrentLinkedQueue<PointerEvent> motionEvents = new ConcurrentLinkedQueue<>();

    // Accumulated key events
    public final ConcurrentLinkedQueue<KeyEvent> keysEvents = new ConcurrentLinkedQueue<>();


    public GameLoop(Class<? extends Scene> c) {
        super();
        instance = this;
        sceneClass = c;
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
        GameLoop.pushUiTask(()->
            {
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
            GameLoop.pushUiTask( () -> {
                        doOnResume.run();
                        doOnResume = null;
                    }
            );
        }
    }


    public void onFrame() {
        SystemTime.tick();
        long rightNow = SystemTime.now();
        step = Math.min((now == 0 ? 0 : rightNow - now),250);
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
            stepExecutor.execute(() -> {
                update();
            });
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
                Touchscreen.processEvent(motionEvents.poll());
            }

            while (!keysEvents.isEmpty()) {
                Keys.processEvent(keysEvents.poll());
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
            EventCollector.setSessionData("pre_scene",scene.getClass().getSimpleName());
            scene.destroy();
        }
        scene = requestedScene;
        scene.create();
        EventCollector.setSessionData("scene",scene.getClass().getSimpleName());

        elapsed = 0f;
        timeScale = 1f;

        Game.syncAdsState();
    }

    public static void setDifficulty(int difficulty) {
        GameLoop.difficulty = difficulty;
    }
}