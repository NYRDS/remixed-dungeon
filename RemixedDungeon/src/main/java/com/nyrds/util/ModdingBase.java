package com.nyrds.util;

import com.nyrds.LuaInterface;
import com.nyrds.pixeldungeon.game.GameLoop;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

public class ModdingBase {
    public static final String REMIXED = "Remixed";
    protected static final Map<String, String> resourcesRemap = new HashMap<>();
    protected static final Set<String> dlcSet = new HashSet<>();

    @NotNull
    static String mActiveMod = REMIXED;
    public static final String NO_FILE = "___no_file";
    public static final Set<String> sizeAgnosticFiles = new HashSet<>();

    public static final String MAZE = "Maze";
    public static final String CONUNDRUM = "Conundrum";
    public static final String REMIXED_ADDITIONS = "Remixed Additions";
    public static final String REMIXED_RPG = "Remixed RPG";
    public static final String THE_EPIC_DUNGEON = "The epic dungeon";
    public static final String HI_FI_DLC = "HiFi DLC";
    public static final String GACHI_RPD = "Gachi RPD; Ascension";

    private static final Set<String> trustedMods = new HashSet<>();

    // Official mods pinned to their first engine-compatible release: older
    // distributions use engine APIs removed by the lua-migration rounds and
    // must not activate. Bump alongside the mod repos when a breaking round ships.
    private static final Map<String, Integer> minModVersion = new HashMap<>();

    static {
        trustedMods.add(MAZE);
        trustedMods.add(CONUNDRUM);

        trustedMods.add(REMIXED_ADDITIONS);
        trustedMods.add(REMIXED_RPG);
        trustedMods.add(THE_EPIC_DUNGEON);
        trustedMods.add(GACHI_RPD);

        dlcSet.add(REMIXED_ADDITIONS);
        dlcSet.add(REMIXED_RPG);
        dlcSet.add(THE_EPIC_DUNGEON);

        dlcSet.add(HI_FI_DLC);
        dlcSet.add(REMIXED);

        minModVersion.put(REMIXED_ADDITIONS, 20);
        minModVersion.put(REMIXED_RPG, 29);
        minModVersion.put(THE_EPIC_DUNGEON, 49);

        resourcesRemap.put("spellsIcons/elemental(new).png", "spellsIcons/elemental_all.png");

        sizeAgnosticFiles.add("ui/title.png");
        sizeAgnosticFiles.add("amulet.png");
        sizeAgnosticFiles.add("ui/arcs1.png");
        sizeAgnosticFiles.add("ui/arcs2.png");
    }

    public static int minModVersion(@NotNull String mod) {
        Integer min = minModVersion.get(mod);
        return min != null ? min : 0;
    }

    public static boolean modVersionSupported(@NotNull String mod, int installedVersion) {
        return installedVersion >= minModVersion(mod);
    }

    public static boolean activeModSupported() {
        if (!inMod()) {
            return true;
        }
        JSONObject version = JsonHelper.tryReadJsonFromAssets("version.json");
        return modVersionSupported(mActiveMod, version.optInt("version"));
    }

    public static int activeModVersion() {
        if (mActiveMod.equals(ModdingBase.REMIXED)) {
            return GameLoop.versionCode;
        }

        JSONObject version = JsonHelper.tryReadJsonFromAssets("version.json");
        return version.optInt("version");
    }

    public static String activeMod() {
        return mActiveMod;
    }

    public static boolean inMod() {
        return !mActiveMod.equals(REMIXED);
    }

    @LuaInterface
    public static boolean inRemixed() {
        return dlcSet.contains(mActiveMod);
    }

    static boolean trustedMod() {
        return trustedMods.contains(mActiveMod);
    }

    @LuaInterface
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
}
