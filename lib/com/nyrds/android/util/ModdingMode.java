package com.nyrds.android.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;

public class ModdingMode {
	static private boolean mMode = false;
	static private String mActiveMod = null;
	static private Context mContext;

	static private boolean mTextRenderingMode = false;

	public static boolean mode() {
		return mMode;
	}

	public static void mode(boolean mMode) {
		ModdingMode.mMode = mMode;
	}

	public static void selectMod(String mod) {
		mActiveMod = mod;
	}

	public static final String activeMod() {
		return mActiveMod;
	}

	public static File getFile(String resName) {
		if(mActiveMod!=null){
			return FileSystem.getExternalStorageFile(mActiveMod + "/" + resName);
		}
		return null;
	}
	
	public static InputStream getInputStream(String resName) {
		try {
			if (mActiveMod!=null) {
				File file = FileSystem.getExternalStorageFile(mActiveMod + "/" + resName);
				if (file.exists()) {
					return new FileInputStream(file);
				}
			}
			return mContext.getAssets().open(resName);
		} catch (IOException e) {
			throw new RuntimeException(e);
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
