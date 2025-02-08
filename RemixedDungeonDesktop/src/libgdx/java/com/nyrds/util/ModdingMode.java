package com.nyrds.util;

import com.nyrds.platform.EventCollector;
import com.nyrds.platform.game.RemixedDungeon;
import com.nyrds.platform.gfx.BitmapData;
import com.nyrds.platform.storage.FileSystem;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;

import org.apache.commons.io.input.BOMInputStream;
import org.jetbrains.annotations.NotNull;

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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.SneakyThrows;


public class ModdingMode extends ModdingBase {

    public static boolean useRetroHeroSprites = false;

    private static final Set<String> pathsChecked = new HashSet<>();
    private static final Map<String, Boolean> assetsExistenceCache = new HashMap<>();

    static private boolean mTextRenderingMode = false;

    public static void selectMod(String mod) {
        try {
            useRetroHeroSprites = false;

            assetsExistenceCache.clear();

            File modPath = FileSystem.getExternalStorageFile(mod);
            if ((modPath.exists() && modPath.isDirectory()) || mod.equals(ModdingBase.REMIXED)) {
                mActiveMod = mod;
            }

            if (!mod.equals(ModdingBase.REMIXED)) {
                useRetroHeroSprites = !isResourceExistInMod("hero_modern");
            }
        } catch (Exception e) {
            EventCollector.logException(e);
            mActiveMod = ModdingBase.REMIXED;
        } finally {
            FileSystem.invalidateCache();
        }
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

        list.replaceAll(s -> s.replaceFirst(path + "/", ""));

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

    public static void setClassicTextRenderingMode(boolean val) {
        mTextRenderingMode = val;
    }

    public static boolean getClassicTextRenderingMode() {
        return mTextRenderingMode;
    }

    public static RuntimeException modException(Exception e) {
        return new ModError(mActiveMod, e);
    }

    public static RuntimeException modException(String s, Exception e) {
        return new ModError(mActiveMod + ":" + s, e);
    }

    @SneakyThrows
    public static @NotNull BitmapData getBitmapData(String src) {

        BitmapData modAsset = BitmapData.decodeStream(getInputStream(src));

        if (modAsset.bmp == null) {
            throw new ModError("Bad bitmap: " + src);
        }

        if (sizeAgnosticFiles.contains(src)) {
            return modAsset;
        }

        if (isAssetExist(src)) {
            BitmapData baseAsset = BitmapData.decodeStream(getInputStreamBuiltIn(src));

            if (baseAsset.bmp == null) {
                throw new ModError("Bad builtin bitmap: " + src);
            }

            if (modAsset.getHeight() * modAsset.getWidth() < baseAsset.getWidth() * baseAsset.getHeight()) {
                RemixedDungeon.toast("%s image in %s smaller than in Remixed, using base version", src, activeMod());
                return baseAsset;
            }
        }

        return modAsset;

    }
}
