package com.nyrds.android.util;

import android.support.annotation.NonNull;

import com.nyrds.retrodungeon.ml.EventCollector;
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

	public static final String MODS_COMMON_JSON = "mods_common.json";

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
						localDesc.url = netDesc.url;
					}
				}
				modsList.put(name, localDesc);
			}

			for (Map.Entry<String, ModDesc> entry : availableMods.entrySet()) {

				String name = entry.getKey();

				if (modsList.containsKey(name)) {
					continue;
				}

				ModDesc netDesc = entry.getValue();
				netDesc.needUpdate = true;

				int rpdVersion = PixelDungeon.version();
				if(rpdVersion>10000) {
					rpdVersion -= 10000;
				}
				if(netDesc.rpdVersion <= rpdVersion) {
					modsList.put(name, netDesc);
				}
			}

		} catch (JSONException e) {
			EventCollector.logException(e);
		}

		Mods.ModDesc Remixed = new Mods.ModDesc();
		Remixed.name = ModdingMode.REMIXED;
		Remixed.needUpdate = false;
		Remixed.installed = true;
		modsList.put(ModdingMode.REMIXED, Remixed);

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

				JSONObject versionInfo = JsonHelper.readJsonFromFile(new File(file.getAbsolutePath() + "/version.json"));
				if (versionInfo.has("version")) {
					desc.version = versionInfo.getInt("version");

					desc.installed = true;
					installedMods.put(desc.name, desc);
				}
			}
		}

		return installedMods;
	}


	private static void updateAvailableModsList(String prefix, Map<String, ModDesc> availableMods) throws JSONException {
		JSONObject mods_common = JsonHelper.readJsonFromFile(FileSystem.getExternalStorageFile(MODS_COMMON_JSON));

		if(!mods_common.has("known_mods")) {
			return;
		}

		JSONObject prefixes = mods_common.getJSONObject("known_mods");

		if(!prefixes.has(prefix)) {
			return;
		}

		JSONArray mods = prefixes.getJSONArray(prefix);

		for (int i = 0; i < mods.length(); ++i) {
			ModDesc desc = new ModDesc();
			JSONObject jsonDesc = mods.getJSONObject(i);
			desc.name = jsonDesc.getString("name");
			desc.version = jsonDesc.getInt("version");
			desc.url = jsonDesc.getString("url");
			desc.rpdVersion = jsonDesc.optInt("rpdVersion");

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
		public int     rpdVersion;
		public boolean needUpdate = false;
		public boolean installed  = false;
	}
}
