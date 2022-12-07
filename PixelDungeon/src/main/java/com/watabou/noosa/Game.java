/*
 * Copyright (C) 2012-2014  Oleg Dolya
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.watabou.noosa;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.media.AudioManager;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.PermissionChecker;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import com.nyrds.android.util.FileSystem;
import com.nyrds.android.util.ModdingMode;
import com.nyrds.android.util.TrackedRuntimeException;
import com.nyrds.android.util.Util;
import com.nyrds.retrodungeon.ml.BuildConfig;
import com.nyrds.retrodungeon.ml.EventCollector;
import com.nyrds.retrodungeon.ml.R;
import com.watabou.glscripts.Script;
import com.watabou.gltextures.TextureCache;
import com.watabou.input.Keys;
import com.watabou.input.Touchscreen;
import com.watabou.noosa.audio.Music;
import com.watabou.noosa.audio.Sample;
import com.watabou.pixeldungeon.ui.Window;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.pixeldungeon.windows.WndMessage;
import com.watabou.utils.SystemTime;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class Game extends Activity implements GLSurfaceView.Renderer, View.OnTouchListener, ActivityCompat.OnRequestPermissionsResultCallback {

	private static Game instance;

	// Actual size of the screen
	private static int width;
	private static int height;

	public static String version;
	public static int versionCode;

	// Current scene
	protected Scene scene;
	// true if scene switch is requested
	protected        boolean requestedReset   = true;
	protected static boolean needSceneRestart = false;

	// New scene class
	private Class<? extends Scene> sceneClass;

	// Current time in milliseconds
	private   long now;
	// Milliseconds passed since previous update
	private   long step;

	private static float timeScale = 1f;
	public static  float elapsed   = 0f;

	private GLSurfaceView view;
	private LinearLayout  layout;

	public static volatile boolean paused = true;
	protected static int difficulty;

	// Accumulated touch events
	private final ArrayList<MotionEvent> motionEvents = new ArrayList<>();

	// Accumulated key events
	private final ArrayList<KeyEvent> keysEvents = new ArrayList<>();

	public Executor executor = Executors.newSingleThreadExecutor();

	private Runnable doOnResume;

	public Game(Class<? extends Scene> c) {
		super();
		instance(this);
		sceneClass = c;
	}

	@SuppressLint("NewApi")
	@SuppressWarnings("deprecation")
		public static long getAvailableInternalMemorySize() {
			File path = Environment.getDataDirectory();
			StatFs stat = new StatFs(path.getPath());
			long ret;
			if (android.os.Build.VERSION.SDK_INT < 18) {
				long blockSize = stat.getBlockSize();
				long availableBlocks = stat.getAvailableBlocks();
				ret = availableBlocks * blockSize;
			} else {
				ret = stat.getAvailableBytes();
			}
			EventCollector.collectSessionData("FreeInternalMemorySize", Long.toString(ret));
			return ret;
		}

	public static void setNeedSceneRestart(boolean needSceneRestart) {
		Game.needSceneRestart = needSceneRestart;
	}

	public float getDifficultyFactor() {
		switch (getDifficulty()) {
			case 0:
				return 1;
			case 1:
			case 2:
				return 1.25f;
			case 3:
				return 2;
			default:
				return 1;
		}
	}

	public static int getDifficulty() {
		return difficulty;
	}

	public void useLocale(String lang) {
		EventCollector.collectSessionData("Locale", lang);

		Locale locale;
		if (lang.equals("pt_BR")) {
			locale = new Locale("pt", "BR");
		} else {
			locale = new Locale(lang);
		}

		StringsManager.useLocale(locale, lang);
	}

	public void doRestart() {
		Intent i = instance().getBaseContext().getPackageManager()
				.getLaunchIntentForPackage(getBaseContext().getPackageName());
		i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

		int piId = 123456;
		PendingIntent pi = PendingIntent.getActivity(getBaseContext(), piId, i, PendingIntent.FLAG_CANCEL_CURRENT);
		AlarmManager mgr = (AlarmManager) getBaseContext().getSystemService(ContextWrapper.ALARM_SERVICE);
		mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, pi);
		shutdown();
	}

	public static void shutdown() {
		paused = true;
		if (instance().scene != null) {
			instance().scene.pause();
		}

		System.exit(0);
	}

	public static void toast(final String text, final Object... args) {
		instance().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				String toastText = text;
				Context context = instance().getApplicationContext();

				if (args.length > 0) {
					toastText = Utils.format(text, args);
				}

				android.widget.Toast toast = android.widget.Toast.makeText(context, toastText,
						android.widget.Toast.LENGTH_LONG);
				toast.show();
			}
		});

	}

	public static boolean isAlpha() {
		return version.contains("alpha");
	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Context context = getApplicationContext();
		StringsManager.setContext(context);

		EventCollector.init(this);

		if(!BuildConfig.DEBUG) {
			EventCollector.collectSessionData("apk signature",Util.getSignature(this));
		}

		FileSystem.setContext(context);
		ModdingMode.setContext(context);

		try {
			version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
			versionCode = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
		} catch (NameNotFoundException e) {
			version = "???";
			versionCode = 0;
		}

		setVolumeControlStream(AudioManager.STREAM_MUSIC);

		view = new GLSurfaceView(this);
		view.setEGLContextClientVersion(2);


		// Hope this allow game work on broader devices list
		// view.setEGLConfigChooser( false );
		view.setRenderer(this);
		view.setOnTouchListener(this);

		layout = new LinearLayout(this);
		getLayout().setOrientation(LinearLayout.VERTICAL);
		getLayout().addView(view);

		setContentView(getLayout());
	}

	@Override
	public void onResume() {
		super.onResume();

		now = 0;

		SystemTime.tick();
		SystemTime.updateLastActionTime();

		view.onResume();

		Music.INSTANCE.resume();
		Sample.INSTANCE.resume();

		if (doOnResume != null) {
			doOnResume.run();
			doOnResume = null;
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		paused = true;

		if (scene != null) {
			executeInGlThread(new Runnable() {
				@Override
				public void run() {
					scene.pause();
				}
			});

			Music.INSTANCE.pause();
			Sample.INSTANCE.pause();
		}

		view.onPause();
		Script.reset();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		executeInGlThread(new Runnable() {
			@Override
			public void run() {
				Music.INSTANCE.mute();
				Sample.INSTANCE.reset();

				if (scene != null) {
					scene.destroy();
					scene = null;
				}
			}
		});

	}

	@SuppressLint({"Recycle", "ClickableViewAccessibility"})
	@Override
	public boolean onTouch(View view, MotionEvent event) {
		synchronized (motionEvents) {
			motionEvents.add(MotionEvent.obtain(event));
		}
		return true;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode == Keys.VOLUME_DOWN || keyCode == Keys.VOLUME_UP) {
			return super.onKeyUp(keyCode, event);
		}

		synchronized (keysEvents) {
			keysEvents.add(event);
		}
		return true;
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {

		if (keyCode == Keys.VOLUME_DOWN || keyCode == Keys.VOLUME_UP) {
			return super.onKeyUp(keyCode, event);
		}

		synchronized (keysEvents) {
			keysEvents.add(event);
		}
		return true;
	}

	@Override
	public void onDrawFrame(GL10 gl) {
		if (instance() == null || width() == 0 || height() == 0) {
			return;
		}

		if(paused) {
			GLES20.glScissor(0, 0, width(), height());
			GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
			return;
		}

		SystemTime.tick();
		long rightNow = SystemTime.now();
		step = (now == 0 ? 0 : rightNow - now);
		now = rightNow;

		step();

		NoosaScript.get().resetCamera();
		GLES20.glScissor(0, 0, width(), height());
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
		
		draw();
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		GLES20.glViewport(0, 0, width, height);

		//GLog.i("viewport: %d %d",width, height );

		Game.width(width);
		Game.height(height);

		setNeedSceneRestart(true);
	}

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		GLES20.glEnable(GL10.GL_BLEND);
		GLES20.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);

		GLES20.glEnable(GL10.GL_SCISSOR_TEST);

		paused = false;

		SystemText.invalidate();
		TextureCache.reload();

		if(scene!=null) {
			scene.resume();
		}
	}

	public static boolean isPaused() {
		return paused;
	}

	public static void resetScene() {
		switchScene(instance().sceneClass);
	}

	public static void switchScene(Class<? extends Scene> c) {
		instance().sceneClass = c;
		instance().requestedReset = true;
	}

	public static Scene scene() {
		return instance().scene;
	}

	private void step() {

		if (requestedReset) {
			requestedReset = false;
			try {
				switchScene(sceneClass.newInstance());
				return;
			} catch (Exception e) {
				throw new TrackedRuntimeException(e);
			}
		}

		update();
	}

	private void draw() {
		scene.draw();
	}

	private void switchScene(Scene requestedScene) {

		SystemText.invalidate();
		Camera.reset();

		if (scene != null) {
			scene.destroy();
		}
		scene = requestedScene;
		scene.create();

		EventCollector.logScene(scene.getClass().getCanonicalName());

		Game.elapsed = 0f;
		Game.timeScale = 1f;
	}

	private void update() {
		Game.elapsed = Game.timeScale * step * 0.001f;

		synchronized (motionEvents) {
			Touchscreen.processTouchEvents(motionEvents);
			motionEvents.clear();
		}
		synchronized (keysEvents) {
			Keys.processTouchEvents(keysEvents);
			keysEvents.clear();
		}

		scene.update();
		Camera.updateAll();
	}

	public static void vibrate(int milliseconds) {
		((Vibrator) instance().getSystemService(VIBRATOR_SERVICE)).vibrate(milliseconds);
	}

	public static String getVar(int id) {
		return StringsManager.getVar(id);
	}

	public static String[] getVars(int id) {
		return StringsManager.getVars(id);
	}

	public synchronized static Game instance() {
		return instance;
	}

	private synchronized static void instance(Game instance) {
		Game.instance = instance;
	}

	public static boolean smallResScreen() {
		return width() <= 320 && height() <= 320;
	}

	public static int width() {
		return width;
	}

	private static void width(int width) {
		Game.width = width;
	}

	public static int height() {
		return height;
	}

	private static void height(int height) {
		Game.height = height;
	}

	public static synchronized void executeInGlThread(Runnable task) {
		instance().view.queueEvent(task);
	}

	public void initEventCollector() {
	}

	private InterstitialPoint permissionsPoint;

	public void doPermissionsRequest(@NonNull InterstitialPoint returnTo, String[] permissions) {
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

	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
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
		doOnResume = new Runnable() {
			@Override
			public void run() {
				permissionsPoint.returnToWork(result);
			}
		};

	}

	public LinearLayout getLayout() {
		return layout;
	}

	public boolean checkOwnSignature() {
		Log.i("Game",Utils.format("own signature %s", Util.getSignature(this)));
		return Util.getSignature(this).equals(getVar(R.string.ownSignature));
	}

	public void setSelectedLanguage() {
	}

	static Window currentWindow;

	public static void showWindow(final String msg) {
		hideWindow();
		Game.executeInGlThread(new Runnable() {
			@Override
			public void run() {
				currentWindow = new WndMessage(msg);
				Game.scene().add(currentWindow);
			}
		});
	}

	public static void hideWindow() {
		Game.executeInGlThread(new Runnable() {
			@Override
			public void run() {
				if(currentWindow!=null) {
					currentWindow.hide();
					currentWindow = null;
				}
			}
		});
	}
}
