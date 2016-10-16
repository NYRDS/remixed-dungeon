package com.nyrds.android.util;

import android.support.annotation.NonNull;

import com.watabou.pixeldungeon.PixelDungeon;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by mike on 16.10.2016.
 */

public class Mods {


	static public Map<String, ModDesc> buildModsList() {
		Map<String, ModDesc> modsList = new HashMap<>();
		try {
			Map<String, ModDesc> installedMods = getInstalledModsList();
			Map<String, ModDesc> availableMods = getAvailableModsList();

			for (Map.Entry<String, ModDesc> entry : installedMods.entrySet()) {
				String name = entry.getKey();
				ModDesc localDesc = entry.getValue();

				ModDesc netDesc = availableMods.get(name);
				if (netDesc != null) {

					if (netDesc.version > localDesc.version) {
						localDesc.needUpdate = true;
						localDesc.url = availableMods.get(name).url;
					}
				}
				modsList.put(name, entry.getValue());
			}

			for (Map.Entry<String, ModDesc> entry : availableMods.entrySet()) {

				String name = entry.getKey();

				if (modsList.containsKey(name)) {
					continue;
				}

				ModDesc netDesc = entry.getValue();
				netDesc.needUpdate = true;
				modsList.put(name, entry.getValue());
			}

		} catch (JSONException e) {
			throw new TrackedRuntimeException(e);
		}
		return modsList;
	}

	@NonNull
	static private Map<String, ModDesc> getInstalledModsList() throws JSONException {
		Map<String, ModDesc> installedMods = new HashMap<>();

		File[] extList = FileSystem.listExternalStorage();

		for (File file : extList) {
			if (file.isDirectory()) {
				ModDesc desc = new ModDesc();
				desc.name = file.getName();

				JSONObject versionInfo = JsonHelper.tryReadJsonFromAssets(file.getAbsolutePath() + "/version.json");
				if (versionInfo.has("version")) {
					desc.version = versionInfo.getInt("version");
				}
				installedMods.put(desc.name, desc);
			}
		}

		return installedMods;
	}


	private static void updateAvailableModsList(String prefix, Map<String, ModDesc> availableMods) throws JSONException {
		JSONObject mods_common = JsonHelper.readJsonFromFile(FileSystem.getExternalStorageFile("mods_" + prefix + ".json"));

		JSONArray mods = mods_common.getJSONArray("known_mods");

		for (int i = 0; i < mods.length(); ++i) {
			ModDesc desc = new ModDesc();
			JSONObject jsonDesc = mods.getJSONObject(i);
			desc.name = jsonDesc.getString("name");
			desc.version = jsonDesc.getInt("version");
			desc.url = jsonDesc.getString("url");

			availableMods.put(desc.name, desc);
		}
	}

	private static Map<String, ModDesc> getAvailableModsList() throws JSONException {
		Map<String, ModDesc> availableMods = new HashMap<>();

		updateAvailableModsList("common", availableMods);
		updateAvailableModsList(PixelDungeon.uiLanguage(), availableMods);
		return availableMods;
	}

	static public class ModDesc {
		public String  url;
		public String  name;
		public int     version;
		public boolean needUpdate;
	}
}
