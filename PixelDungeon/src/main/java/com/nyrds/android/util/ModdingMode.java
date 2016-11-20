package com.nyrds.android.util;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class ModdingMode {
	public static final String REMIXED = "Remixed";
	
	static private String mActiveMod = REMIXED;
	static private Context mContext;

	static private boolean mTextRenderingMode = false;

	public static void selectMod(String mod) {
		File modPath = FileSystem.getExternalStorageFile(mod);
		if((modPath.exists() && modPath.isDirectory()) || mod.equals(ModdingMode.REMIXED)) {
			mActiveMod = mod;
		}
	}

	public static String activeMod() {
		return mActiveMod;
	}

	public static boolean isAssetExist(String resName) {
		InputStream str;
		try {
			str = mContext.getAssets().open(resName);
			str.close();
			return true;
		} catch (IOException e) {
			return false;
		}
	}
	
	public static boolean inMod() {
		return !mActiveMod.equals(REMIXED);
	}
	
	public static boolean isResourceExistInMod(String resName) {
		if(!mActiveMod.equals(REMIXED)){
			return FileSystem.getExternalStorageFile(mActiveMod + "/" + resName).exists();
		}
		return false;
	}
	
	public static boolean isResourceExist(String resName) {
		if(isResourceExistInMod(resName)) {
			return true;
		} else {
			return isAssetExist(resName);
		}
	}
	
	public static File getFile(String resName) {
		if(!mActiveMod.equals(REMIXED)){
			return FileSystem.getExternalStorageFile(mActiveMod + "/" + resName);
		}
		return null;
	}
	
	public static InputStream getInputStream(String resName) {
		try {
			if (!mActiveMod.equals(REMIXED) && isModdingAllowed(resName)) {
				File file = FileSystem.getExternalStorageFile(mActiveMod + "/" + resName);
				if (file.exists()) {
					return new FileInputStream(file);
				}
			}
			return mContext.getAssets().open(resName);
		} catch (IOException e) {
			throw new TrackedRuntimeException(e);
		}
	}

	private static boolean isModdingAllowed(String resName){
		return !(resName.contains("accessories") || resName.contains("banners"));
	}

	public static void setContext(Context context) {
		mContext = context;
	}

	public static void setClassicTextRenderingMode(boolean val) {
		mTextRenderingMode = val;
	}

	public static boolean getClassicTextRenderingMode() {
		return mTextRenderingMode;
	}

	public static boolean isHalloweenEvent(){

		Calendar now = new GregorianCalendar();
		Calendar halloween = new GregorianCalendar();
		halloween.set(Calendar.MONTH,Calendar.OCTOBER);
		halloween.set(Calendar.DAY_OF_MONTH,31);

		long milisPerDay = (1000 * 60 * 60 * 24);

		long nowMilis =  now.getTimeInMillis() /milisPerDay;
		long hallMilis = halloween.getTimeInMillis() /milisPerDay;

		long daysDiff;



		if(nowMilis > hallMilis) {
			daysDiff = (nowMilis - hallMilis);
		} else {
			daysDiff = (hallMilis - nowMilis );
		}

		return daysDiff < 14;
	}
}
