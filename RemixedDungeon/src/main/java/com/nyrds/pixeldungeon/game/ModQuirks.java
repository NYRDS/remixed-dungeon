package com.nyrds.pixeldungeon.game;

public class ModQuirks {

    public static boolean mobLeveling;
    public static boolean only2dTiles;
    public static boolean only3dTiles;

    public static void reset() {
        mobLeveling =true;
        only2dTiles = false;
        only3dTiles = false;
    }
}
