package com.nyrds.platform.game;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.media.AudioManager;
import android.net.Uri;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.PermissionChecker;

import com.nyrds.pixeldungeon.game.GameLoop;
import com.nyrds.pixeldungeon.game.GamePreferences;
import com.nyrds.pixeldungeon.support.Ads;
import com.nyrds.pixeldungeon.support.Iap;
import com.nyrds.pixeldungeon.support.PlayGames;
import com.nyrds.platform.EventCollector;
import com.nyrds.platform.app.RemixedDungeonApp;
import com.nyrds.platform.audio.Music;
import com.nyrds.platform.audio.Sample;
import com.nyrds.platform.gfx.SystemText;
import com.nyrds.platform.input.Keys;
import com.nyrds.util.ReportingExecutor;
import com.watabou.glscripts.Script;
import com.watabou.gltextures.TextureCache;
import com.watabou.noosa.InterstitialPoint;
import com.watabou.noosa.Scene;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.utils.Random;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Executor;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class Game {
    private static Game instance;

    public Iap iap;


    private static volatile boolean paused = true;

    protected GameLoop gameLoop;

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
        return layout;
    }

    public void openUrl(String prompt, String address) {
    }

}
