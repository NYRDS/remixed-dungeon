package com.nyrds.platform.game;

/**
 * HTML version of InstallMod
 */
public class InstallMod extends RemixedDungeon {
    public void installMod() {
        // In HTML version, mod installation is not supported
        System.out.println("Mod installation not supported in HTML version");
    }
    
    public static boolean isModInstalled(String modId) {
        // In HTML version, mod installation is not supported
        return false;
    }
}