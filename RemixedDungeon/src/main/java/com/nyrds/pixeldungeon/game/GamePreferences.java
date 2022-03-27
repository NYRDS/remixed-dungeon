package com.nyrds.pixeldungeon.game;

import android.annotation.SuppressLint;

import com.nyrds.pixeldungeon.support.AdsUtils;
import com.nyrds.platform.EventCollector;
import com.nyrds.platform.audio.Music;
import com.nyrds.platform.audio.Sample;
import com.nyrds.platform.game.Game;
import com.nyrds.platform.game.RemixedDungeon;
import com.nyrds.platform.gfx.SystemText;
import com.nyrds.platform.storage.Preferences;
import com.nyrds.platform.util.StringsManager;
import com.nyrds.util.ModdingMode;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Badges;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.ui.ModsButton;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.pixeldungeon.windows.elements.Tool;

import java.util.Locale;

public class GamePreferences {
    public static void zoom(double value) {
        Preferences.INSTANCE.put(Preferences.KEY_ZOOM, value);
    }

    public static double zoom() {
        return Preferences.INSTANCE.getDouble(Preferences.KEY_ZOOM, 0);
    }

    public static void music(boolean value) {
        Music.INSTANCE.enable(value);
        Preferences.INSTANCE.put(Preferences.KEY_MUSIC, value);
    }

    public static boolean music() {
        return Preferences.INSTANCE.getBoolean(Preferences.KEY_MUSIC, true);
    }

    public static void soundFx(boolean value) {
        Sample.INSTANCE.enable(value);
        Preferences.INSTANCE.put(Preferences.KEY_SOUND_FX, value);
    }

    public static boolean soundFx() {
        return Preferences.INSTANCE.getBoolean(Preferences.KEY_SOUND_FX, true);
    }

    public static void brightness(boolean value) {
        Preferences.INSTANCE.put(Preferences.KEY_BRIGHTNESS, value);
        if (GameLoop.scene() instanceof GameScene) {
            ((GameScene) GameLoop.scene()).brightness(value);
        }
    }

    public static boolean brightness() {
        return Preferences.INSTANCE.getBoolean(Preferences.KEY_BRIGHTNESS,
                false);
    }

    private static void donated(int value) {
        Preferences.INSTANCE.put(Preferences.KEY_DONATED, value);
    }

    public static int donated() {
        return Preferences.INSTANCE.getInt(Preferences.KEY_DONATED, 0);
    }

    public static void lastClass(int value) {
        Preferences.INSTANCE.put(Preferences.KEY_LAST_CLASS, value);
    }

    public static int lastClass() {
        return Preferences.INSTANCE.getInt(Preferences.KEY_LAST_CLASS, 0);
    }

    public static void challenges(int value) {
        Preferences.INSTANCE.put(Preferences.KEY_CHALLENGES, value);
    }

    public static int challenges() {
        return Preferences.INSTANCE.getInt(Preferences.KEY_CHALLENGES, 0);
    }

    public static void intro(boolean value) {
        Preferences.INSTANCE.put(Preferences.KEY_INTRO, value);
    }

    public static boolean intro() {
        return Preferences.INSTANCE.getBoolean(Preferences.KEY_INTRO, true);
    }

    public static String uiLanguage() {
        String deviceLocale = Locale.getDefault().getLanguage();
        EventCollector.setSessionData("device_locale", deviceLocale);
        return Preferences.INSTANCE.getString(Preferences.KEY_LOCALE,
                deviceLocale);
    }

    public static void uiLanguage(String lang) {
        Preferences.INSTANCE.put(Preferences.KEY_LOCALE, lang);

        setSelectedLanguage();
        GameLoop.resetScene();
    }

    public static void version(int value)  {
        Preferences.INSTANCE.put( Preferences.KEY_VERSION, value );
    }

    public static int version() {
        return Preferences.INSTANCE.getInt( Preferences.KEY_VERSION, 0 );
    }

    public static void versionString(String value)  {
        Preferences.INSTANCE.put( Preferences.KEY_VERSION_STRING, value );
    }

    public static String versionString() {
        return Preferences.INSTANCE.getString( Preferences.KEY_VERSION_STRING, Utils.UNKNOWN);
    }

    public static void fontScale(int value) {
        Preferences.INSTANCE.put(Preferences.KEY_FONT_SCALE, value);
        SystemText.updateFontScale();
    }

    public static int fontScale() {
        return Preferences.INSTANCE.getInt(Preferences.KEY_FONT_SCALE, 0);
    }

    public static boolean classicFont() {
        boolean val = Preferences.INSTANCE.getBoolean(Preferences.KEY_CLASSIC_FONT, false);
        ModdingMode.setClassicTextRenderingMode(val);
        return val;
    }

    public static void classicFont(boolean value) {
        ModdingMode.setClassicTextRenderingMode(value);
        Preferences.INSTANCE.put(Preferences.KEY_CLASSIC_FONT, value);
    }

    public static void activeMod(String mod) {
        Preferences.INSTANCE.put(Preferences.KEY_ACTIVE_MOD, mod);
        ModdingMode.selectMod(activeMod());

        setSelectedLanguage();

        EventCollector.setSessionData("RPD_active_mod", ModdingMode.activeMod());
        EventCollector.setSessionData("active_mod_version", Integer.toString(ModdingMode.activeModVersion()));
        ModsButton.modUpdated();
    }

    public static String activeMod() {
        return Preferences.INSTANCE.getString(Preferences.KEY_ACTIVE_MOD, ModdingMode.REMIXED);
    }

    public static boolean realtime() {
            return Preferences.INSTANCE.getBoolean(Preferences.KEY_REALTIME, false);
    }

    public static void realtime(boolean value) {
        Preferences.INSTANCE.put(Preferences.KEY_REALTIME, value);
    }

    // *** IMMERSIVE MODE ****
    @SuppressLint("NewApi")
    public static void immerse(boolean value) {
        Preferences.INSTANCE.put(Preferences.KEY_IMMERSIVE, value);

        Game.runOnMainThread(() -> {
            RemixedDungeon.updateImmersiveMode();
            GameLoop.setNeedSceneRestart();
        });
    }

    public static boolean immersed() {
        return Preferences.INSTANCE
                .getBoolean(Preferences.KEY_IMMERSIVE, true);
    }

    /*
     * <---Purchases
     */
    static public void setDonationLevel(int level) {

        if(level > 0) {
            AdsUtils.removeEasyModeBanner();
        }

        if (level < donated()) {
            return;
        }

        if (donated() == 0 && level != 0) {
            GameLoop.pushUiTask(() -> {
                Sample.INSTANCE.play(Assets.SND_GOLD);
                Badges.validateSupporter();
            });
        }
        donated(level);
    }

    public static void setDifficulty(int _difficulty) {
        GameLoop.setDifficulty(_difficulty);
        Game.syncAdsState();
    }

    //--- Move timeouts
    public static int moveTimeout() {
        return Preferences.INSTANCE.getInt(Preferences.KEY_MOVE_TIMEOUT, Integer.MAX_VALUE);
    }

    public static void moveTimeout(int value) {
        Preferences.INSTANCE.put(Preferences.KEY_MOVE_TIMEOUT,value);
    }

    public static int limitTimeoutIndex(int value) {
        return 	Math.max(Math.min(value, GameLoop.MOVE_TIMEOUTS.length-1),0);
    }

    public static double getMoveTimeout() {
        return GameLoop.MOVE_TIMEOUTS[limitTimeoutIndex(moveTimeout())];
    }

    public static int quickSlots() {
        return Preferences.INSTANCE.getInt(Preferences.KEY_QUICKSLOTS, -1);
}

    public static void quickSlots(int slots) {
        Preferences.INSTANCE.put(Preferences.KEY_QUICKSLOTS, slots);
        if (GameLoop.scene() instanceof GameScene) {
            ((GameScene) GameLoop.scene()).updateToolbar(false);
        }
    }

    public static String toolStyle() {
        return Preferences.INSTANCE.getString(Preferences.KEY_TOOL_STYLE, Tool.Size.Std.name());
    }

    public static void toolStyle(String style) {
        Preferences.INSTANCE.put(Preferences.KEY_TOOL_STYLE, style);
        if (GameLoop.scene() instanceof GameScene) {
            ((GameScene) GameLoop.scene()).updateToolbar(true);
        }
    }

    public static Boolean handedness() {
        return Preferences.INSTANCE.getBoolean(Preferences.KEY_HANDEDNESS, false);
    }

    public static void handedness(Boolean left) {
        Preferences.INSTANCE.put(Preferences.KEY_HANDEDNESS, left);
        if (GameLoop.scene() instanceof GameScene) {
            ((GameScene) GameLoop.scene()).updateToolbar(false);
        }
    }

    static public void useLocale(String lang) {
        EventCollector.setSessionData("Locale", lang);

        Locale locale = new Locale(lang);

        if (lang.equals("pt_BR")) {
            locale = new Locale("pt", "BR");
        }

        if (lang.equals("zh_CN")) {
            locale = new Locale("zh", "CN");
        }

        if (lang.equals("zh_TW")) {
            locale = new Locale("zh", "TW");
        }

        StringsManager.useLocale(locale, lang);
    }

    public static void setSelectedLanguage() {
        useLocale(uiLanguage());
    }
}
