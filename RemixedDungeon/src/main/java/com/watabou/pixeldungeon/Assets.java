/*
 * Pixel Dungeon
 * Copyright (C) 2012-2014  Oleg Dolya
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package com.watabou.pixeldungeon;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.storage.Preferences;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.utils.Bundle;

import org.json.JSONException;

public class Assets {

    private static final String KEY_BANNERS = "banners";
    private static final String KEY_TOOLBAR = "toolbar";
    private static final String KEY_STATUS  = "status";
    private static final String KEY_CHROME  = "chrome";

    public static final String ARCS_BG   = "ui/arcs1.png";
    public static final String ARCS_FG   = "ui/arcs2.png";
    public static final String DASHBOARD = "ui/dashboard.png";

    public static final String BADGES = "ui/badges.png";
    public static final String AMULET = "amulet.png";

    public static final String ICONS  = "ui/icons.png";
    public static final String HP_BAR = "ui/hp_bar.png";
    public static final String SP_BAR = "ui/sp_bar.png";
    public static final String XP_BAR = "ui/exp_bar.png";

    public static final String AVATARS = "ui/avatars.png";
    public static final String PET     = "pet.png";

    public static final String SURFACE = "ui/surface.png";

    public static final String FIREBALL = "ui/fireball.png";
    public static final String SPECKS   = "effects/specks.png";
    public static final String EFFECTS  = "effects/effects.png";

    public static final String RAT       = "rat.png";
    public static final String GNOLL     = "gnoll.png";
    public static final String GOO       = "goo.png";
    public static final String SKELETON  = "skeleton.png";
    public static final String TENGU     = "tengu.png";
    public static final String SHEEP     = "sheep.png";
    public static final String KEEPER    = "shopkeeper.png";
    public static final String DM300     = "dm300.png";
    public static final String MONK      = "monk.png";
    public static final String STATUE    = "statue.png";
    public static final String PIRANHA   = "piranha.png";
    public static final String ROTTING   = "rotting_fist.png";
    public static final String YOG       = "yog.png";
    public static final String LARVA     = "larva.png";
    public static final String GHOST     = "ghost.png";
    public static final String MAKER     = "wandmaker.png";
    public static final String TROLL     = "blacksmith.png";
    public static final String IMP       = "demon.png";

    public static final String ITEMS  = "items.png";
    public static final String PLANTS = "plants.png";

    public static final String TILES_SPIDER_NEST_X         = "tiles_spiders_nest_x.png";
    public static final String TILES_SPIDER_NEST_XYZ       = "tilesets/tiles_spider_caves_xyz.png";
    public static final String TILES_SEWERS                = "tiles0.png";
    public static final String TILES_SEWERS_X              = "tiles0_x.png";
    public static final String TILES_SEWERS_XYZ            = "tilesets/tiles_sewers_xyz.png";
    public static final String TILES_PRISON                = "tiles1.png";
    public static final String TILES_PRISON_X              = "tiles1_x.png";
    public static final String TILES_PRISON_XYZ            = "tilesets/tiles_prison_xyz.png";
    public static final String TILES_PRISON_BOSS           = "tiles1_boss.png";
    public static final String TILES_CAVES                 = "tiles2.png";
    public static final String TILES_CAVES_X               = "tiles2_x.png";
    public static final String TILES_CAVES_XYZ             = "tilesets/tiles_caves_xyz.png";
    public static final String TILES_CITY                  = "tiles3.png";
    public static final String TILES_CITY_XYZ              = "tilesets/tiles_metropolis_xyz.png";
    public static final String TILES_HALLS                 = "tiles4.png";
    public static final String TILES_HALLS_XYZ             = "tilesets/tiles_halls_xyz.png";
    public static final String TILES_SHADOW_LORD           = "tiles_shadow_lord.png";
    public static final String TILES_GUTS                  = "tiles_guts_x.png";
    public static final String TILES_GUTS_XYZ              = "tilesets/tiles_guts_xyz.png";
    public static final String TILES_NECRO                 = "tiles_necropolis.png";
    public static final String TILES_NECRO_XYZ             = "tilesets/tiles_necropolis_xyz.png";
    public static final String TILES_ICE_CAVES_X           = "tiles_ice_caves_x.png";
    public static final String TILES_ICE_CAVES_XYZ         = "tilesets/tiles_ice_caves_xyz.png";
    public static final String TILES_GENERIC_TOWN_INTERIOR = "tiles_standalone_interior.png";

    public static final String WATER_SEWERS    = "water0.png";
    public static final String WATER_PRISON    = "water1.png";
    public static final String WATER_CAVES     = "water2.png";
    public static final String WATER_CITY      = "water3.png";
    public static final String WATER_HALLS     = "water4.png";
    public static final String WATER_SPIDERS   = "water_spider_nest.png";
    public static final String WATER_GUTS      = "water_guts.png";
    public static final String WATER_NECRO     = "water_necropolis.png";
    public static final String WATER_ICE_CAVES = "water_ice_caves.png";

    public static final String BUFFS_SMALL = "ui/buffs.png";
    public static final String BUFFS_LARGE = "ui/large_buffs.png";

    public static final String SPELL_ICONS = "spell_icons.png";

    public static final String FONTS1X  = "ui/font1x.png";
    public static final String FONTS25X = "ui/font25x.png";

    public static final String THEME = "theme";
    public static final String TUNE  = "game";
    public static final String HAPPY = "surface";

    public static final String SND_CLICK = "snd_click";
    public static final String SND_BADGE = "snd_badge";
    public static final String SND_GOLD  = "snd_gold";

    public static final String SND_OPEN      = "snd_door_open";
    public static final String SND_UNLOCK    = "snd_unlock";
    public static final String SND_ITEM      = "snd_item";
    public static final String SND_DEWDROP   = "snd_dewdrop";
    public static final String SND_HIT       = "snd_hit";
    public static final String SND_MISS      = "snd_miss";
    public static final String SND_STEP      = "snd_step";
    public static final String SND_WATER     = "snd_water";
    public static final String SND_DESCEND   = "snd_descend";
    public static final String SND_EAT       = "snd_eat";
    public static final String SND_READ      = "snd_read";
    public static final String SND_LULLABY   = "snd_lullaby";
    public static final String SND_DRINK     = "snd_drink";
    public static final String SND_SHATTER   = "snd_shatter";
    public static final String SND_ZAP       = "snd_zap";
    public static final String SND_LIGHTNING = "snd_lightning";
    public static final String SND_LEVELUP   = "snd_levelup";
    public static final String SND_DEATH     = "snd_death";
    public static final String SND_CHALLENGE = "snd_challenge";
    public static final String SND_CURSED    = "snd_cursed";
    public static final String SND_TRAP      = "snd_trap";
    public static final String SND_EVOKE     = "snd_evoke";
    public static final String SND_TOMB      = "snd_tomb";
    public static final String SND_ALERT     = "snd_alert";
    public static final String SND_MELD      = "snd_meld";
    public static final String SND_BOSS      = "snd_boss";
    public static final String SND_BLAST     = "snd_blast";
    public static final String SND_PLANT     = "snd_plant";
    public static final String SND_RAY       = "snd_ray";
    public static final String SND_BEACON    = "snd_beacon";
    public static final String SND_TELEPORT  = "snd_teleport";
    public static final String SND_CHARMS    = "snd_charms";
    public static final String SND_MASTERY   = "snd_mastery";
    public static final String SND_PUFF      = "snd_puff";
    public static final String SND_ROCKS     = "snd_rocks";
    public static final String SND_BURNING   = "snd_burning";
    public static final String SND_FALLING   = "snd_falling";
    public static final String SND_GHOST     = "snd_ghost";
    public static final String SND_SECRET    = "snd_secret";
    public static final String SND_BONES     = "snd_bones";
    public static final String SND_MIMIC     = "snd_mimic";

    public static final String SND_DOMINANCE   = "snd_dominance";
    public static final String SND_ROTTEN_DROP = "snd_rotten_drop";
    public static final String SND_CRYSTAL     = "snd_crystal";

    public static final String SND_BITE      = "snd_bite";            // by sonidotv, from freesound.org
    public static final String SND_EXPLOSION = "snd_explosion"; // by kantouth, from freesound.org

    public static final String UI_ICONS_12 = "ui/ui_icons12x12.png";
    public static final String UI_ICONS_6  = "ui/ui_icons6x6.png";


    private static int chromeType  = 0;
    private static int statusType  = 0;
    private static int toolbarType = 0;
    private static int bannersType = 0;

    static {
        Bundle premiumSettings;
        try {
            premiumSettings = new Bundle(Preferences.INSTANCE.getString(Preferences.KEY_PREMIUM_SETTINGS, Utils.EMPTY_STRING));
            chromeType = premiumSettings.getInt(KEY_CHROME);
            statusType = premiumSettings.getInt(KEY_STATUS);
            toolbarType = premiumSettings.getInt(KEY_TOOLBAR);
            bannersType = premiumSettings.getInt(KEY_BANNERS);
        } catch (JSONException e) {
        }
    }

    public static String getBanners() {
        switch (bannersType) {
            default:
                return "ui/" + StringsManager.getVar(R.string.Assets_Prefix) + "banners.png";
            case 1:
                return "ui/" + StringsManager.getVar(R.string.Assets_Prefix) + "banners_supporter_1.png";
            case 2:
                return "ui/" + StringsManager.getVar(R.string.Assets_Prefix) + "banners_supporter_2.png";
            case 3:
                return "ui/" + StringsManager.getVar(R.string.Assets_Prefix) + "banners_supporter_3.png";
            case 4:
                return "ui/" + StringsManager.getVar(R.string.Assets_Prefix) + "banners_supporter_4.png";
        }
    }


    public static String getChrome() {
        switch (chromeType) {
            default:
                return "ui/chrome.png";
            case 1:
                return "ui/chrome_supporter_1.png";
            case 2:
                return "ui/chrome_supporter_2.png";
            case 3:
                return "ui/chrome_supporter_3.png";
            case 4:
                return "ui/chrome_marble.png";
            case 5:
                return "ui/chrome_supporter_4.png";
        }
    }

    public static String getStatus() {
        switch (statusType) {
            default:
                return "ui/status_pane.png";
            case 1:
                return "ui/status_pane_supporter_1.png";
            case 2:
                return "ui/status_pane_supporter_2.png";
            case 3:
                return "ui/status_pane_supporter_3.png";
            case 4:
                return "ui/status_pane_supporter_4.png";
        }
    }

    public static void use(String assetKind, int index) {
        if (assetKind.equals(KEY_CHROME)) {
            chromeType = index;
        }
        if (assetKind.equals(KEY_STATUS)) {
            statusType = index;
        }

        if (assetKind.equals(KEY_TOOLBAR)) {
            toolbarType = index;
        }

        if (assetKind.equals(KEY_BANNERS)) {
            bannersType = index;
        }

        Bundle premiumSettings = new Bundle();
        premiumSettings.put(KEY_CHROME, chromeType);
        premiumSettings.put(KEY_STATUS, statusType);
        premiumSettings.put(KEY_TOOLBAR, toolbarType);
        premiumSettings.put(KEY_BANNERS, bannersType);

        Preferences.INSTANCE.put(Preferences.KEY_PREMIUM_SETTINGS, premiumSettings.serialize());
    }

    public static String getTitle() {
        switch (bannersType) {
            default:
                return "ui/title.png";
            case 1:
                return "ui/title_supporter_1.png";
            case 2:
                return "ui/title_supporter_2.png";
            case 3:
                return "ui/title_supporter_3.png";
            case 4:
                return "ui/title_supporter_4.png";
        }
    }
}
