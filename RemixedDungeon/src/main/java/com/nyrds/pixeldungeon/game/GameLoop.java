package com.nyrds.pixeldungeon.game;

import android.view.KeyEvent;
import android.view.MotionEvent;

import com.nyrds.LuaInterface;
import com.nyrds.platform.EventCollector;
import com.nyrds.platform.audio.Music;
import com.nyrds.platform.audio.Sample;
import com.nyrds.platform.game.Game;
import com.nyrds.platform.gfx.SystemText;
import com.nyrds.platform.gl.Gl;
import com.nyrds.platform.gl.NoosaScript;
import com.nyrds.platform.input.Keys;
import com.nyrds.platform.input.Touchscreen;
import com.nyrds.platform.util.TrackedRuntimeException;
import com.nyrds.util.ModdingMode;
import com.nyrds.util.ReportingExecutor;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Gizmo;
import com.watabou.noosa.Scene;
import com.watabou.pixeldungeon.scenes.InterlevelScene;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.utils.SystemTime;

import org.luaj.vm2.LuaError;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;

import lombok.SneakyThrows;

public class GameLoop {

    public static final AtomicInteger loadingOrSaving = new AtomicInteger();
    public static final double[] MOVE_TIMEOUTS = new double[]{250, 500, 1000, 2000, 5000, 10000, 30000, 60000, Double.POSITIVE_INFINITY };

    public static String version = Utils.EMPTY_STRING;
    public static int versionCode = 0;

    // Actual size of the screen
    public static int width;
    public static int height;

    public static volatile boolean softPaused = false;

    private final Executor executor = new ReportingExecutor();
    public Executor serviceExecutor = new ReportingExecutor();

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
    public final ConcurrentLinkedQueue<MotionEvent> motionEvents = new ConcurrentLinkedQueue<>();

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
        Scene scene = scene();
        if(scene!=null) {
            scene.add(gizmo);
        }
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

    public static boolean smallResScreen() {
        return width() <= 320 && height() <= 320;
    }

    public static int width() {
        return width;
    }

    public static void width(int width) {
        GameLoop.width = width;
    }

    public static int height() {
        return height;
    }

    public static void height(int height) {
        GameLoop.height = height;
    }

    public static boolean isAlpha() {
        return version.contains("alpha") || version.contains("in_dev");
    }

    public static boolean isDev() {
        return version.contains("in_dev");
    }

    public static void switchNoFade(Class<? extends PixelScene> c) {
        PixelScene.noFade = true;
        switchScene(c);
    }

    public void shutdown() {
        if (instance().scene != null) {
            instance().scene.pause();
            instance().scene.destroy();
        }
    }

    public void onResume() {
        now = 0;

        SystemTime.tick();
        SystemTime.updateLastActionTime();

        Music.INSTANCE.resume();
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


        if (framesSinceInit>2) {
            Runnable task;
            while ((task = uiTasks.poll()) != null) {
                task.run();
            }

            if (!softPaused) {
                try {
                    step();
                } catch (LuaError e) {
                    throw ModdingMode.modException(e);
                } catch (Exception e) {
                    throw new TrackedRuntimeException(e);
                }
            }

            NoosaScript.get().resetCamera();

            Gl.clear();

            if (scene != null) {
                scene.draw();
            }
        }
    }

    @SneakyThrows
    public void step() {

        if(loadingOrSaving.get() > 0) {
            return;
        }

        if (requestedReset) {
            requestedReset = false;
            switchScene(sceneClass.newInstance());
            return;
        }

        elapsed = timeScale * step * 0.001f;

        while(!motionEvents.isEmpty()) {
            Touchscreen.processEvent(motionEvents.poll());
        }

        while(!keysEvents.isEmpty()) {
            Keys.processEvent(keysEvents.poll());
        }

        scene.update();
        Camera.updateAll();
    }

    private void switchScene(Scene requestedScene) {

        SystemText.invalidate();
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