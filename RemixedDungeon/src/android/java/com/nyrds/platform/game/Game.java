package com.nyrds.platform.game;

import android.Manifest;
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
import android.view.ViewParent;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.PermissionChecker;

import com.nyrds.pixeldungeon.game.GameLoop;
import com.nyrds.pixeldungeon.game.GamePreferences;
import com.nyrds.pixeldungeon.support.PlayGames;
import com.nyrds.platform.EventCollector;
import com.nyrds.platform.app.RemixedDungeonApp;
import com.nyrds.platform.audio.MusicManager;
import com.nyrds.platform.audio.Sample;
import com.nyrds.platform.gfx.SystemText;
import com.nyrds.platform.input.Keys;
import com.nyrds.platform.input.PointerEvent;
import com.nyrds.platform.support.Ads;
import com.nyrds.platform.support.Iap;
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

import lombok.Getter;

@SuppressLint("Registered")
public class Game extends Activity implements GLSurfaceView.Renderer, View.OnTouchListener, ActivityCompat.OnRequestPermissionsResultCallback {

    public static final int REQUEST_CODE_OPEN_DOCUMENT_TREE_MOD_DIR_INSTALL = 37372;
    public static final int REQUEST_CODE_OPEN_DOCUMENT_TREE_MOD_DIR_EXPORT = 37373;

    public static boolean softPaused;
    @SuppressLint("StaticFieldLeak")
    private static Game instance;

    public PlayGames playGames;
    public Iap iap;
    public Executor serviceExecutor = new ReportingExecutor();


    private GLSurfaceView view;
    @Getter
    private LinearLayout layout;

    @Getter
    private static volatile boolean paused = false;

    protected GameLoop gameLoop;

    public Game(Class<? extends Scene> c) {
        super();
        instance = this;
        gameLoop = new GameLoop(c);
    }

    static public void runOnMainThread(Runnable runnable) {
        GameLoop.pushUiTask( () -> {
            instance().runOnUiThread(runnable);
        });
    }

    static public void requestInternetPermission(InterstitialPoint returnTo) {
        String[] requiredPermissions = {Manifest.permission.INTERNET};
        instance().doPermissionsRequest(returnTo, requiredPermissions);
    }

    public static boolean smallResScreen() {
        return false;
    }

    public void doRestart() {
        Intent i = getBaseContext()
                .getPackageManager()
                .getLaunchIntentForPackage(getBaseContext().getPackageName())
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

        int piId = Random.Int(Integer.MAX_VALUE);

        PendingIntent pi = PendingIntent.getActivity(getBaseContext(), piId, i, PendingIntent.FLAG_CANCEL_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        AlarmManager mgr = (AlarmManager) getBaseContext().getSystemService(ContextWrapper.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, pi);
        shutdown();
    }

    //TODO investigate how it behaves
    public static void shutdown() {
        GameLoop.pushUiTask(() -> {
            instance.pause();
            instance().finish();
            System.exit(0);
        });
    }

    public static void toast(final String text, final Object... args) {
        String toastText = text;

        if (args.length > 0) {
            toastText = Utils.format(text, args);
        }

        GLog.toFile("%s ",toastText);

        String finalToastText = toastText;

        runOnMainThread(() -> {

            Toast toast = Toast.makeText(RemixedDungeonApp.getContext(), finalToastText,
                    Toast.LENGTH_SHORT);
            toast.show();
        });

    }

    @Override
    protected void onSaveInstanceState(@NotNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        iap.onNewIntent(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this;

        iap = new Iap();
        if (savedInstanceState == null) {
            iap.onNewIntent(getIntent());
        }

        try {
            GameLoop.version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            GameLoop.versionCode = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
        } catch (NameNotFoundException ignored) {
        }

        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        view = new GLSurfaceView(this);
        view.setEGLContextClientVersion(2);

        // Hope this allow game work on broader devices list
        // view.setEGLConfigChooser( false );
        view.setRenderer(this);
        view.setOnTouchListener(this);
    }

    public static void syncAdsState() {

        if(GamePreferences.donated() > 0) {
            Ads.removeEasyModeBanner();
            return;
        }

        if (GameLoop.getDifficulty() == 0) {
            Ads.displayEasyModeBanner();
        }

        if (GameLoop.getDifficulty() >= 2) {
            Ads.removeEasyModeBanner();
        }

    }

    @Override
    public void onResume() {
        super.onResume();

        ViewParent parent = view.getParent();
        if (parent != null) {
            layout.removeView(view);
        }

        layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(view);
        setContentView(layout);

        instance = this;

        view.onResume();

        gameLoop.onResume();
    }

    public void pause() {
        paused = true;

        synchronized (GameLoop.stepLock) {
            if (gameLoop.scene != null) { // view.onPause will wait for gl thread, so it safe to access scene here
                gameLoop.scene.pause();
            }
        }

        MusicManager.INSTANCE.pause();
        Sample.INSTANCE.pause();

        Script.reset();
    }

    @Override
    public void onPause() {
        super.onPause();
        view.onPause();
        pause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        MusicManager.INSTANCE.mute();
        Sample.INSTANCE.reset();
    }

    @SuppressLint({"Recycle", "ClickableViewAccessibility"})
    @Override
    public boolean onTouch(View view, MotionEvent event) {
        gameLoop.motionEvents.add(new PointerEvent(MotionEvent.obtain(event)));
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == Keys.VOLUME_DOWN || keyCode == Keys.VOLUME_UP) {
            return super.onKeyDown(keyCode, event);
        }

        gameLoop.keysEvents.add(event);
        return true;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {

        if (keyCode == Keys.VOLUME_DOWN || keyCode == Keys.VOLUME_UP) {
            return super.onKeyUp(keyCode, event);
        }

        synchronized (gameLoop.keysEvents) {
            gameLoop.keysEvents.add(event);
        }
        return true;
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        if (instance() == null || GameLoop.width == 0 || GameLoop.height == 0) {
            gameLoop.framesSinceInit = 0;
            return;
        }

        if (paused) {
            gameLoop.framesSinceInit = 0;
            return;
        }

        if(!isFinishing()) {
            gameLoop.onFrame();
        }
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);

        //GLog.i("viewport: %d %d",width, height );

        GameLoop.width = width;
        GameLoop.height = height;

        GameLoop.setNeedSceneRestart();
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glEnable(GL10.GL_BLEND);
        GLES20.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);

        GLES20.glEnable(GL10.GL_SCISSOR_TEST);


        SystemText.invalidate();
        TextureCache.clear();

        paused = false;

        if (gameLoop.scene != null) {
            gameLoop.scene.resume();
        }
    }


    public static void vibrate(int milliseconds) {
        ((Vibrator) instance().getSystemService(VIBRATOR_SERVICE)).vibrate(milliseconds);
    }

    public synchronized static Game instance() {
        return instance;
    }

    private InterstitialPoint permissionsPoint = new Utils.SpuriousReturn();

    public void doPermissionsRequest(@NotNull InterstitialPoint returnTo, String[] permissions) {
        boolean havePermissions = true;
        for (String permission : permissions) {
            int checkResult = ActivityCompat.checkSelfPermission(this, permission);
            if (checkResult != PermissionChecker.PERMISSION_GRANTED) {
                havePermissions = false;
                break;
            }
        }
        if (!havePermissions) {
            int code = 0;
            permissionsPoint = returnTo;
            ActivityCompat.requestPermissions(this, permissions, code);
        } else {
            returnTo.returnToWork(true);
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NotNull String @NotNull [] permissions, int @NotNull [] grantResults) {
        boolean res = true;

        if (permissions.length == 0) {
            res = false;
        }

        for (int grant : grantResults) {
            if (grant != PackageManager.PERMISSION_GRANTED) {
                res = false;
                break;
            }
        }

        final boolean result = res;
        gameLoop.doOnResume = () -> {
            if(permissionsPoint == null) {
                EventCollector.logException("permissionsPoint was not set");
                return;
            }
            permissionsPoint.returnToWork(result);
        };

    }

    static public void openUrl(String prompt, String address) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(address));
        Game.instance().startActivity(Intent.createChooser(intent, prompt));
    }

    static public void sendEmail(String emailUri, String subject) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("message/rfc822");
        intent.putExtra(Intent.EXTRA_EMAIL, new String[] { emailUri });
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);

        Game.instance().startActivity(Intent.createChooser(intent, emailUri));
    }


    public static void openPlayStore() {
        final String appPackageName = instance().getPackageName();
        try {
            instance().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
        } catch (android.content.ActivityNotFoundException anfe) {
            instance().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
        }
    }

    public static void updateFpsLimit() {

    }

}
