package com.nyrds.android.util;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

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
			if (!mActiveMod.equals(REMIXED)) {
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

	public static void setContext(Context context) {
		mContext = context;
	}

	public static void setClassicTextRenderingMode(boolean val) {
		mTextRenderingMode = val;
	}

	public static boolean getClassicTextRenderingMode() {
		return mTextRenderingMode;
	}
}
