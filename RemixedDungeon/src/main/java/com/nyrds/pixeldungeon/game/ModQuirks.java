package com.nyrds.pixeldungeon.game;

public class ModQuirks {

    public static boolean mobLeveling;
    public static boolean only2dTiles;
    public static boolean only3dTiles;
    public static boolean noMovingArcs;

    public static float defaultCarcassChance = 0;

    public static void reset() {
        mobLeveling =true;
        only2dTiles = false;
        only3dTiles = false;
        defaultCarcassChance = 0;
        noMovingArcs = false;
    }
}
