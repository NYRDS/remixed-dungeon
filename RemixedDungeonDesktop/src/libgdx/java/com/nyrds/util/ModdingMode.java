package com.nyrds.util;

import com.nyrds.LuaInterface;
import com.nyrds.pixeldungeon.game.GameLoop;
import com.nyrds.platform.EventCollector;
import com.nyrds.platform.game.RemixedDungeon;
import com.nyrds.platform.gfx.BitmapData;
import com.nyrds.platform.storage.FileSystem;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;

import org.apache.commons.io.input.BOMInputStream;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.SequenceInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.SneakyThrows;


public class ModdingMode {
    public static final String REMIXED = "Remixed";
    public static final String NO_FILE = "___no_file";

    private static final Set<String> trustedMods = new HashSet<>();
    private static final Set<String> dlcSet = new HashSet<>();

    public static boolean useRetroHeroSprites = false;

    private static final Set<String> pathsChecked = new HashSet<>();
    private static final Map<String, Boolean> assetsExistenceCache = new HashMap<>();

    private static final Map<String, String> resourcesRemap = new HashMap<>();

    private static final Map<String, String> rewardVideoIds = new HashMap<>();
    private static final Map<String, String> interstitialIds = new HashMap<>();

    public static final Set<String> sizeAgnosticFiles = new HashSet<>();

    public static final String MAZE = "Maze";

    public static final String CONUNDRUM = "Conundrum";

    public static final String REMIXED_ADDITIONS = "Remixed Additions";

    public static final String REMIXED_RPG = "Remixed RPG";

    public static final String THE_EPIC_DUNGEON = "The epic dungeon";

    public static final String HI_FI_DLC = "HiFi DLC";

    static {
        trustedMods.add(MAZE);
        trustedMods.add(CONUNDRUM);

        trustedMods.add(REMIXED_ADDITIONS);
        trustedMods.add(REMIXED_RPG);
        trustedMods.add(THE_EPIC_DUNGEON);

        dlcSet.add(REMIXED_ADDITIONS);
        dlcSet.add(REMIXED_RPG);
        dlcSet.add(THE_EPIC_DUNGEON);

        dlcSet.add(HI_FI_DLC);
        dlcSet.add(REMIXED);

        resourcesRemap.put("spellsIcons/elemental(new).png", "spellsIcons/elemental_all.png");

        sizeAgnosticFiles.add("ui/title.png");
        sizeAgnosticFiles.add("amulet.png");
        sizeAgnosticFiles.add("ui/arcs1.png");
        sizeAgnosticFiles.add("ui/arcs2.png");
    }

    @NotNull
    static private String mActiveMod = REMIXED;

    static private boolean mTextRenderingMode = false;

    public static void selectMod(String mod) {
        try {
            useRetroHeroSprites = false;

            assetsExistenceCache.clear();

            File modPath = FileSystem.getExternalStorageFile(mod);
            if ((modPath.exists() && modPath.isDirectory()) || mod.equals(ModdingMode.REMIXED)) {
                mActiveMod = mod;
            }

            if (!mod.equals(ModdingMode.REMIXED)) {
                useRetroHeroSprites = !isResourceExistInMod("hero_modern");
            }
        } catch (Exception e) {
            EventCollector.logException(e);
            mActiveMod = ModdingMode.REMIXED;
        } finally {
            FileSystem.invalidateCache();
        }
    }

    public static int activeModVersion() {
        if (mActiveMod.equals(ModdingMode.REMIXED)) {
            return GameLoop.versionCode;
        }

        JSONObject version = JsonHelper.tryReadJsonFromAssets("version.json");
        return version.optInt("version");
    }

    public static String activeMod() {
        return mActiveMod;
    }

    public static String getSoundById(String id) {

        String candidate = id + ".ogg";

        if (ModdingMode.isResourceExistInMod(candidate)) {
            return candidate;
        }

        candidate = id + ".mp3";

        if (ModdingMode.isResourceExistInMod(candidate)) {
            return candidate;
        }

        candidate = id + ".ogg";

        if (ModdingMode.isAssetExist(candidate)) {
            return candidate;
        }

        candidate = id + ".mp3";
        if (ModdingMode.isAssetExist(candidate)) {
            return candidate;
        }

        if (id.contains(".mp3")) {
            return getSoundById(id.replace(".mp3", ""));
        }

        if (id.contains(".ogg")) {
            return getSoundById(id.replace(".ogg", ""));
        }

        return Utils.EMPTY_STRING;
    }

    public static boolean isSoundExists(String id) {
        String resourceId = "sound/" + id;
        String foundId = getSoundById(resourceId);
        GLog.debug("sound: %s -> %s", id, foundId);
        return !foundId.isEmpty();
    }

    public static boolean isAssetExist(String resName) {
        Boolean isExist = assetsExistenceCache.get(resName);

        if (isExist != null) {
            return isExist;
        }

        boolean res = FileSystem.getInternalStorageFileHandleBase(resName).exists();
        assetsExistenceCache.put(resName, res);
        return res;
    }

    public static boolean inMod() {
        return !mActiveMod.equals(REMIXED);
    }

    @LuaInterface
    public static boolean inRemixed() {
        return dlcSet.contains(mActiveMod);
    }

    public static boolean isResourceExists(String resName) {
        return FileSystem.exists(resName);
    }

    public static boolean isResourceExistInMod(String resName) {
        return FileSystem.exists(resName);
    }

    @NotNull
    public static List<String> listResources(String path, FilenameFilter filter) {
        pathsChecked.clear();

        var list = _listResources(path, filter);

        for (int i = 0; i < list.size(); ++i) {
            list.set(i, list.get(i).replaceFirst(path + "/", ""));
        }

        return list;
    }

    @SneakyThrows
    @NotNull
    private static List<String> _listResources(String path, FilenameFilter filter) {
        if (pathsChecked.contains(path)) {
            return new ArrayList<>();
        }

        pathsChecked.add(path);

        Set<String> resList = new HashSet<>();

        String[] fullList = FileSystem.listResources(path);

        collectResources(path, filter, resList, fullList);

        return Arrays.asList(resList.toArray(new String[0]));
    }

    private static void collectResources(String path, FilenameFilter filter, Set<String> resList, String[] fullList) {
        if (fullList == null) {
            return;
        }
        for (String resource : fullList) {
            if (filter.accept(null, resource)) {
                resList.add(path + "/" + resource);
            } else {
                resList.addAll(_listResources(path + "/" + resource, filter));
            }
        }
    }

    public static boolean isResourceExist(String resName) {
        if (isResourceExistInMod(resName)) {
            return true;
        } else {
            return isAssetExist(resName);
        }
    }

    public static String getResource(String resName) {

        StringBuilder resource = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(getInputStream(resName)))) {
            String line = reader.readLine();

            while (line != null) {
                resource.append(line);
                line = reader.readLine();
            }
        } catch (IOException | SecurityException e) {
            EventCollector.logException(e, resName);
        }

        return resource.toString();
    }

    public static @NotNull InputStream getInputStreamBuiltIn(String resName) {
        try {
            if (resourcesRemap.containsKey(resName)) {
                resName = resourcesRemap.get(resName);
            }

            return new FileInputStream(FileSystem.getInternalStorageFile(resName));
        } catch (IOException | SecurityException e) {
            throw new ModError("Missing file: " + resName + " in Remixed", e);
        }
    }

    public static @NotNull InputStream getInputMergedInputStream(String resName) {
        InputStream modStream = null;
        try {
            modStream = new BOMInputStream(new FileInputStream(FileSystem.getInternalStorageFile(resName)));
        } catch (Exception e) {
            // ignore
        }

        try {
            InputStream builtInStream = new BOMInputStream(new FileInputStream(FileSystem.getInternalStorageFileBase(resName)));
            if(modStream == null) {
                return builtInStream;
            }
            return new SequenceInputStream(builtInStream, modStream);
        } catch (IOException | SecurityException | ModError e) {
            throw new ModError("Missing file: " + resName, e);
        }

    }

    public static @NotNull InputStream getInputStream(String resName) {
        try {
            if (isModdingAllowed(resName)) {
                return new FileInputStream(FileSystem.getInternalStorageFile(resName));
            }
            return new FileInputStream(FileSystem.getInternalStorageFileBase(resName));
        } catch (IOException | SecurityException | ModError e) {
            throw new ModError("Missing file: " + resName, e);
        }
    }

    private static boolean isModdingAllowed(@NotNull String resName) {

        if (resName.startsWith("scripts/services")) {
            return false;
        }

        return trustedMod() || !(resName.contains("accessories") || resName.contains("banners"));
    }

    private static boolean trustedMod() {
        return trustedMods.contains(mActiveMod);
    }

    public static void setClassicTextRenderingMode(boolean val) {
        mTextRenderingMode = val;
    }

    public static boolean getClassicTextRenderingMode() {
        return mTextRenderingMode;
    }

    public static boolean isHalloweenEvent() {

        Calendar now = new GregorianCalendar();
        Calendar halloween = new GregorianCalendar();
        halloween.set(Calendar.MONTH, Calendar.OCTOBER);
        halloween.set(Calendar.DAY_OF_MONTH, 31);

        long milisPerDay = (1000 * 60 * 60 * 24);

        long nowMilis = now.getTimeInMillis() / milisPerDay;
        long hallMilis = halloween.getTimeInMillis() / milisPerDay;

        long daysDiff;

        if (nowMilis > hallMilis) {
            daysDiff = (nowMilis - hallMilis);
        } else {
            daysDiff = (hallMilis - nowMilis);
        }

        return daysDiff < 14;
    }

    public static RuntimeException modException(Exception e) {
        return new ModError(mActiveMod, e);
    }

    public static RuntimeException modException(String s, Exception e) {
        return new ModError(mActiveMod + ":" + s, e);
    }

    @SneakyThrows
    public static @NotNull BitmapData getBitmapData(String src) {

        String resName = (String) src;

        BitmapData modAsset = BitmapData.decodeStream(getInputStream(resName));

        if (modAsset.bmp == null) {
            throw new ModError("Bad bitmap: " + resName);
        }

        if (sizeAgnosticFiles.contains(resName)) {
            return modAsset;
        }

        if (isAssetExist(resName)) {
            BitmapData baseAsset = BitmapData.decodeStream(getInputStreamBuiltIn(resName));

            if (baseAsset.bmp == null) {
                throw new ModError("Bad builtin bitmap: " + resName);
            }

            if (modAsset.getHeight() * modAsset.getWidth() < baseAsset.getWidth() * baseAsset.getHeight()) {
                RemixedDungeon.toast("%s image in %s smaller than in Remixed, using base version", resName, activeMod());
                return baseAsset;
            }
        }

        return modAsset;

    }
}
