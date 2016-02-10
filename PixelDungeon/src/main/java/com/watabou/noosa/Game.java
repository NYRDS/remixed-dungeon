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
import android.content.res.Configuration;
import android.content.res.Resources.NotFoundException;
import android.media.AudioManager;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.os.Vibrator;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.PermissionChecker;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import com.nyrds.android.util.FileSystem;
import com.nyrds.android.util.ModdingMode;
import com.nyrds.pixeldungeon.ml.R;
import com.watabou.glscripts.Script;
import com.watabou.gltextures.TextureCache;
import com.watabou.input.Keys;
import com.watabou.input.Touchscreen;
import com.watabou.noosa.audio.Music;
import com.watabou.noosa.audio.Sample;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.utils.SystemTime;

import org.acra.ACRA;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class Game extends Activity implements GLSurfaceView.Renderer, View.OnTouchListener, ActivityCompat.OnRequestPermissionsResultCallback {

	private static Game instance;
	private static Context context;

	// Actual size of the screen
	private static int width;
	private static int height;

	public static String version;
	public static int versionCode;

	// Current scene
	protected Scene scene;
	// true if scene switch is requested
	protected boolean requestedReset = true;
	protected static boolean needSceneRestart = false;

	// New scene class
	protected Class<? extends Scene> sceneClass;

	// Current time in milliseconds
	protected long now;
	// Milliseconds passed since previous update
	protected long step;

	public static float timeScale = 1f;
	public static float elapsed = 0f;

	protected GLSurfaceView view;
	protected LinearLayout layout;

	public static volatile boolean paused = true;
	protected static int difficulty;

	// Accumulated touch events
	protected final ArrayList<MotionEvent> motionEvents = new ArrayList<>();

	// Accumulated key events
	protected final ArrayList<KeyEvent> keysEvents = new ArrayList<>();

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
		}	else {
			ret = stat.getAvailableBytes();
		}
		ACRA.getErrorReporter().putCustomData("FreeInternalMemorySize", Long.toString(ret));
		return ret;
	}

	public void useLocale(String lang) {
		ACRA.getErrorReporter().putCustomData("Locale", lang);

		Locale locale;
		if (lang.equals("pt_BR")) {
			locale = new Locale("pt", "BR");
		} else {
			locale = new Locale(lang);
		}

		Configuration config = getBaseContext().getResources().getConfiguration();
		config.locale = locale;
		getBaseContext().getResources().updateConfiguration(config,
				getBaseContext().getResources().getDisplayMetrics());

		String modStrings = String.format("strings_%s.json", lang);

		if(ModdingMode.isResourceExistInMod(modStrings)) {
			parseStrings(modStrings);
		} else if (ModdingMode.isResourceExistInMod("strings_en.json")) {
			parseStrings("strings_en.json");
		}
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

	public  static void shutdown() {
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

	@SuppressLint("UseSparseArrays")
	private Map<Integer, String> stringMap = new HashMap<>();
	@SuppressLint("UseSparseArrays")
	private Map<Integer, String[]> stringsMap = new HashMap<>();
	private Map<String, Integer> keyToInt;

	private void addMappingForClass(Class<?> clazz) {
		for (Field f : clazz.getDeclaredFields()) {
			int key;
			try {
				key = f.getInt(null);
			} catch (IllegalAccessException | IllegalArgumentException e) {
				throw new RuntimeException(e);
			}
			String name = f.getName();

			keyToInt.put(name, key);
		}
	}

	private void initTextMapping() {
		long mapStart = System.nanoTime();

		keyToInt = new HashMap<>();

		addMappingForClass( R.string.class);
		addMappingForClass( R.array.class);

		long mapEnd = System.nanoTime();
		GLog.toFile("map creating time %f", (mapEnd - mapStart) / 1000000f);
	}

	private void parseStrings(String resource) {
		File jsonFile = ModdingMode.getFile(resource);
		if (jsonFile == null) {
			return;
		}

		if(!jsonFile.exists()) {
			return;
		}

		if (keyToInt == null) {
			initTextMapping();
		}

		String line = "";

		try {
			InputStream fis = new FileInputStream(jsonFile);
			InputStreamReader isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
			BufferedReader br = new BufferedReader(isr);

			while ((line = br.readLine()) != null) {
				JSONArray entry = new JSONArray(line);

				String keyString = entry.getString(0);
				Integer key = keyToInt.get(keyString);
				if(key == null){
					toast("unknown key: [%s] in [%s] ignored ", keyString, resource);
				}

				if (entry.length() == 2) {

					String value = entry.getString(1);
					stringMap.put(key, value);
				}

				if (entry.length() > 2) {
					String[] values = new String[entry.length() - 1];
					for (int i = 1; i < entry.length(); i++) {
						values[i - 1] = entry.getString(i);
					}
					stringsMap.put(key, values);
				}
			}
			br.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (JSONException e) {
			toast("malformed json: [%s] in [%s] ignored ", line, resource);
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		context = getApplicationContext();

		FileSystem.setContext(context);
		ModdingMode.setContext(context);

		try {
			version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
			version = "???";
		}
		try {
			versionCode = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
		} catch (NameNotFoundException e) {
			versionCode = 0;
		}

		setVolumeControlStream(AudioManager.STREAM_MUSIC);

		view = new GLSurfaceView(this);
		view.setEGLContextClientVersion(2);
		// Hope this allow game work on broader devices list
		// view.setEGLConfigChooser( false );
		view.setRenderer(this);
		view.setOnTouchListener(this);

		if (android.os.Build.VERSION.SDK_INT >= 9) {
			layout = new LinearLayout(this);
			layout.setOrientation(LinearLayout.VERTICAL);
			layout.addView(view);

			setContentView(layout);
		} else {
			setContentView(view);
		}
	}

	@Override
	public void onResume() {
		super.onResume();

		now = 0;
		view.onResume();

		Music.INSTANCE.resume();
		Sample.INSTANCE.resume();

		if(doOnResume!=null) {
			doOnResume.run();
			doOnResume = null;
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		paused = true;

		if (scene != null) {
			scene.pause();
		}

		view.onPause();
		Script.reset();

		Music.INSTANCE.pause();
		Sample.INSTANCE.pause();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		Music.INSTANCE.mute();
		Sample.INSTANCE.reset();

		if (scene != null) {
			scene.destroy();
			scene = null;
		}
	}

	@SuppressLint({ "Recycle", "ClickableViewAccessibility" })
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

			return false;
		}

		synchronized (keysEvents) {
			keysEvents.add(event);
		}
		return true;
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {

		if (keyCode == Keys.VOLUME_DOWN || keyCode == Keys.VOLUME_UP) {

			return false;
		}

		synchronized (keysEvents) {
			keysEvents.add(event);
		}
		return true;
	}

	@Override
	public void onDrawFrame(GL10 gl) {

		if (instance() == null || width() == 0 || height() == 0 || paused) {
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

		Game.width(width);
		Game.height(height);
	}

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		GLES20.glEnable(GL10.GL_BLEND);
		GLES20.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);

		GLES20.glEnable(GL10.GL_SCISSOR_TEST);

		paused = false;

		SystemText.invalidate();
		TextureCache.reload();

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

	protected void step() {

		if (requestedReset) {
			requestedReset = false;
			try {
				switchScene(sceneClass.newInstance());
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		update();
	}

	protected void draw() {
		scene.draw();
	}

	protected void switchScene(Scene requestedScene) {

		SystemText.invalidate();
		Camera.reset();

		if (scene != null) {
			scene.destroy();
		}
		scene = requestedScene;
		scene.create();

		Game.elapsed = 0f;
		Game.timeScale = 1f;
	}

	protected void update() {
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

		if (instance()!= null && instance().stringMap !=null && instance().stringMap.containsKey(id)) {
			return instance().stringMap.get(id);
		}

		try {
			return context.getResources().getString(id);
		} catch (NotFoundException notFound) {
			GLog.w("resource not found: %s", notFound.getMessage());
		}
		return "";
	}

	public static String[] getVars(int id) {

		if (id != R.string.easyModeAdUnitId && id != R.string.saveLoadAdUnitId
				&& id != R.string.easyModeSmallScreenAdUnitId && id != R.string.iapKey && id != R.string.testDevice) {
			if (instance()!=null && instance().stringsMap.containsKey(id)) {
				return instance().stringsMap.get(id);
			}
		}
		return context.getResources().getStringArray(id);
	}

	public synchronized static Game instance() {
		return instance;
	}

	public synchronized static Game instance(Game instance) {
		Game.instance = instance;
		return instance;
	}

	public static void donate(int level) {
	}

	public String getPriceString(int level) {
		return null;
	}

	public void initIap() {
	}

	public boolean iapReady() {
		return false;
	}

	public static int width() {
		return width;
	}

	public static void width(int width) {
		Game.width = width;
	}

	public static int height() {
		return height;
	}

	public static void height(int height) {
		Game.height = height;
	}

	public static synchronized void executeInGlThread(Runnable task) {
		instance().view.queueEvent(task);
	}

	public void removeEasyModeBanner() {
	}

	public void initSaveAndLoadIntersitial() {
	}

	public void displayEasyModeBanner() {
	}

	private InterstitialPoint permissionsPoint;
	public void doPermissionsRequest(InterstitialPoint returnTo,String[] permissions) {
		boolean havePermissions = true;
		for(String permission:permissions) {
			int checkResult = ActivityCompat.checkSelfPermission(this, permission);
			if (checkResult != PermissionChecker.PERMISSION_GRANTED) {
					havePermissions = false;
					break;
			}
		}
		if(!havePermissions) {
			int code = 0;
			permissionsPoint = returnTo;
			ActivityCompat.requestPermissions(this, permissions, code);
		}	else {
			returnTo.returnToWork(true);
		}
	}

	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		boolean res = true;

		if(permissions.length == 0) {
			res = false;
		}

		for(int grant:grantResults) {
			if(grant!= PackageManager.PERMISSION_GRANTED) {
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
}
