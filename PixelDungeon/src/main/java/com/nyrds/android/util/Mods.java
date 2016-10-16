package com.nyrds.android.util;

import android.support.annotation.NonNull;

import com.watabou.pixeldungeon.PixelDungeon;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by mike on 16.10.2016.
 */

public class Mods {

	@NonNull
	static public ArrayList<ModDesc> buildsModsList() {
		ArrayList<ModDesc> installedMods = new ArrayList<>();

		File[] extList = FileSystem.listExternalStorage();

		for (File file : extList) {
			if (file.isDirectory()) {
				ModDesc desc = new ModDesc();
				desc.name = file.getName();

				JSONObject versionInfo = JsonHelper.tryReadJsonFromAssets(file.getAbsolutePath()+"/version.json");
				if(versionInfo.has("version")) {
					try {
						desc.version = versionInfo.getInt("version");
					} catch (JSONException e) {
						new TrackedRuntimeException(e);
					}
				}

				installedMods.add(desc);
			}
		}

		return installedMods;
	}

	ArrayList<ModDesc> getAvailableModsList() throws JSONException {
		ArrayList<ModDesc> availableMods = new ArrayList<>();

		JSONObject mods_common = JsonHelper.readJsonFromFile(FileSystem.getExternalStorageFile("mods_common.json"));
		JSONObject mods_locale = JsonHelper.readJsonFromFile(FileSystem.getExternalStorageFile("mods_"+ PixelDungeon.uiLanguage()+".json"));


		JSONArray mods = mods_common.getJSONArray("known_mods");

		for(int i = 0;i<mods.length();++i) {
			ModDesc desc = new ModDesc();
			JSONObject jsonDesc = mods.getJSONObject(i);
			desc.name = jsonDesc.getString("name");
			desc.version = jsonDesc.getInt("version");
			desc.url = jsonDesc.getString("url");
		}

		return availableMods;
	}

	static public class ModDesc {
		public String  url;
		public String  name;
		public int     version;
		public boolean needUpdate;
	}
}
