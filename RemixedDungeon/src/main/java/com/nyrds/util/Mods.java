package com.nyrds.util;

import com.nyrds.pixeldungeon.game.GamePreferences;
import com.nyrds.pixeldungeon.utils.ModDesc;
import com.nyrds.platform.EventCollector;
import com.nyrds.platform.storage.FileSystem;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import lombok.SneakyThrows;

/**
 * Created by mike on 16.10.2016.
 */

public class Mods {

    public static final String MODS_COMMON_JSON = "mods_common.json";

    static public @NotNull Map<String, ModDesc> buildModsList() {
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

                if (netDesc.isCompatible()) {
                    modsList.put(name, netDesc);
                }
            }

        } catch (Exception e) {
            EventCollector.logException(e);
        }

        ModDesc Remixed = new ModDesc();
        Remixed.installDir = Remixed.name = ModdingMode.REMIXED;
        Remixed.needUpdate = false;
        Remixed.installed = true;
        modsList.put(ModdingMode.REMIXED, Remixed);

        return modsList;
    }

    @NotNull
    static private Map<String, ModDesc> getInstalledModsList() throws JSONException {
        Map<String, ModDesc> installedMods = new HashMap<>();

        File[] extList = FileSystem.listExternalStorage();

        for (File file : extList) {
            if (file.isDirectory()) {
                ModDesc desc = new ModDesc();
                desc.installDir = desc.name = file.getName();

                File fileToTry = new File(file.getAbsolutePath() + "/version.json");

                JSONObject versionInfo = JsonHelper.readJsonFromFile(fileToTry);
                if (versionInfo.has("version")) {

                    ModDesc.fromJson(desc, versionInfo);

                    desc.installed = true;
                    installedMods.put(desc.installDir, desc);
                }
            }
        }

        return installedMods;
    }

    @SneakyThrows
    public static ModDesc getModDesc(String name, String lang) {
        JSONObject mods_common = JsonHelper.readJsonFromFile(FileSystem.getExternalStorageFile(MODS_COMMON_JSON));

        ModDesc ret = new ModDesc();

        if (name.equals("Remixed")) {
            ret.name = "Remixed";
            ret.description = "Remixed Dungeon without any mods";
            ret.author = "NYRDS";
            ret.url = "https://wiki.nyrds.net";
            return ret;
        }

        if (!mods_common.has("info")) {
            return ret;
        }

        JSONObject info = mods_common.getJSONObject("info");

        if (!info.has(name)) {
            return ret;
        }

        JSONObject desc = info.getJSONObject(name);

        JSONObject modDesc = null;
        if (desc.has(lang)) {
            modDesc = desc.getJSONObject(lang);
        }

        if (modDesc == null) {
            if (desc.has("en")) {
                modDesc = desc.getJSONObject("en");
            } else {
                return ret;
            }
        }

        ret.name = modDesc.optString("Mod_Name", name);
        ret.description = modDesc.optString("Mod_Description", "Unknown");
        ret.author = modDesc.optString("Mod_Author", "Unknown");
        ret.url = modDesc.optString("Mod_Link", "");
        return ret;
    }

    private static void updateAvailableModsList(String prefix, Map<String, ModDesc> availableMods) throws JSONException {
        JSONObject mods_common = JsonHelper.readJsonFromFile(FileSystem.getExternalStorageFile(MODS_COMMON_JSON));

        if (!mods_common.has("known_mods")) {
            return;
        }

        JSONObject prefixes = mods_common.getJSONObject("known_mods");

        if (!prefixes.has(prefix)) {
            return;
        }

        JSONArray mods = prefixes.getJSONArray(prefix);

        for (int i = 0; i < mods.length(); ++i) {
            ModDesc desc = new ModDesc();
            JSONObject jsonDesc = mods.getJSONObject(i);
            desc.installDir = desc.name = jsonDesc.getString("name");
            desc.version = jsonDesc.getInt("version");
            desc.url = jsonDesc.getString("url");
            desc.rpdVersion = jsonDesc.optInt("rpdVersion");

            availableMods.put(desc.installDir, desc);
        }
    }

    private static @NotNull Map<String, ModDesc> getAvailableModsList() throws JSONException {
        Map<String, ModDesc> availableMods = new HashMap<>();

        updateAvailableModsList("common", availableMods);
        updateAvailableModsList(GamePreferences.uiLanguage(), availableMods);
        return availableMods;
    }

}
