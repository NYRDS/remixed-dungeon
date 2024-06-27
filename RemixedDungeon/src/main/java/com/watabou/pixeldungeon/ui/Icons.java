/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
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
package com.watabou.pixeldungeon.ui;

import com.nyrds.platform.EventCollector;
import com.watabou.noosa.Image;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.actors.hero.HeroClass;

public enum Icons {

    SKULL,
    BUSY,
    COMPASS,
    PREFS,
    WARNING,
    TARGET,
    NYRDIE,
    WARRIOR,
    MAGE,
    ROGUE,
    ELF,
    CLOSE,
    DEPTH,
    SLEEP,
    ALERT,
    SUPPORT,
    SUPPORTED,
    BACKPACK,
    SEED_POUCH,
    SCROLL_HOLDER,
    WAND_HOLSTER,
    POTIONS_BELT,
    KEYRING,
    CHECKED,
    UNCHECKED,
    EXIT,
    CHALLENGE_OFF,
    CHALLENGE_ON,
    RESUME,
    CHEST_SILVER,
    CHEST_GOLD,
    CHEST_RUBY,
    HUNTRESS,
    MODDING_MODE,
    QUIVER,
    MIND_CONTROL,
    GRAPHS,
    NECROMANCER,
    CHEST_ROYAL,
    PLAY_GAMES,
    BTN_SYNC_IN,
    BTN_SYNC_OUT,
    BTN_SYNC_REFRESH,
    BTN_TARGET,
    BTN_QUESTION,
    GNOLL,
    VK,
    FB,
    DISCORD,
    PLUS,
    MINUS,
    FACILITATIONS_ON,
    FACILITATIONS_OFF;



    public Image get() {
        return get(this);
    }

    public static Image get(Icons type) {
        Image icon = new Image(Assets.ICONS);
        Image icon2 = new Image(Assets.ICONS2);
        switch (type) {
            case SKULL:
                icon.frame(icon.texture.uvRect(0, 0, 8, 8));
                break;
            case BUSY:
                icon.frame(icon.texture.uvRect(8, 0, 16, 8));
                break;
            case COMPASS:
                icon.frame(icon.texture.uvRect(0, 8, 7, 13));
                break;
            case PREFS:
                icon.frame(icon.texture.uvRect(30, 0, 46, 16));
                break;
            case WARNING:
                icon.frame(icon.texture.uvRect(46, 0, 58, 12));
                break;
            case TARGET:
                icon.frame(icon.texture.uvRect(0, 13, 16, 29));
                break;
            case NYRDIE:
                icon.frame(icon.texture.uvRect(30, 16, 45, 26));
                break;
            case WARRIOR:
                icon.frame(icon.texture.uvRect(0, 29, 16, 45));
                break;
            case MAGE:
                icon.frame(icon.texture.uvRect(16, 29, 32, 45));
                break;
            case ROGUE:
                icon.frame(icon.texture.uvRect(32, 29, 48, 45));
                break;
            case ELF:
                icon.frame(icon.texture.uvRect(48, 29, 64, 45));
                break;
            case CLOSE:
                icon.frame(icon.texture.uvRect(1, 46, 12, 57));
                break;
            case DEPTH:
                icon.frame(icon.texture.uvRect(45, 12, 54, 20));
                break;
            case SLEEP:
                icon.frame(icon.texture.uvRect(13, 45, 22, 53));
                break;
            case ALERT:
                icon.frame(icon.texture.uvRect(22, 45, 30, 53));
                break;
            case SUPPORT:
                icon.frame(icon.texture.uvRect(78, 74, 94, 88));
                break;
            case SUPPORTED:
                icon.frame(icon.texture.uvRect(46, 45, 62, 61));
                break;
            case BACKPACK:
                icon.frame(icon.texture.uvRect(58, 0, 68, 10));
                break;
            case SCROLL_HOLDER:
                icon.frame(icon.texture.uvRect(68, 0, 78, 10));
                break;
            case SEED_POUCH:
                icon.frame(icon.texture.uvRect(78, 0, 88, 10));
                break;
            case WAND_HOLSTER:
                icon.frame(icon.texture.uvRect(88, 0, 98, 10));
                break;
            case POTIONS_BELT:
                icon.frame(icon.texture.uvRect(98, 0, 108, 10));
                break;
            case KEYRING:
                icon.frame(icon.texture.uvRect(64, 29, 74, 39));
                break;
            case CHECKED:
                icon.frame(icon.texture.uvRect(54, 12, 66, 24));
                break;
            case UNCHECKED:
                icon.frame(icon.texture.uvRect(66, 12, 78, 24));
                break;
            case EXIT:
                icon.frame(icon.texture.uvRect(108, 0, 124, 16));
                break;
            case CHALLENGE_OFF:
                icon.frame(icon.texture.uvRect(78, 16, 102, 40));
                break;
            case CHALLENGE_ON:
                icon.frame(icon.texture.uvRect(102, 16, 126, 40));
                break;
            case FACILITATIONS_OFF:
                icon2.frame(icon2.texture.uvRect(78, 16, 102, 40));
                return icon2;

            case FACILITATIONS_ON:
                icon2.frame(icon2.texture.uvRect(102, 16, 126, 40));
                return icon2;
            case RESUME:
                icon.frame(icon.texture.uvRect(114, 0, 126, 11));
                break;
            case CHEST_SILVER:
                icon.frame(icon.texture.uvRect(63, 46, 77, 59));
                break;
            case CHEST_GOLD:
                icon.frame(icon.texture.uvRect(79, 46, 93, 59));
                break;
            case CHEST_RUBY:
                icon.frame(icon.texture.uvRect(95, 46, 109, 59));
                break;
            case HUNTRESS:
                icon.frame(icon.texture.uvRect(110, 45, 126, 61));
                break;
            case MODDING_MODE:
                icon.frame(icon.texture.uvRect(0, 58, 12, 70));
                break;
            case QUIVER:
                icon.frame(icon.texture.uvRect(13, 54, 23, 64));
                break;
            case MIND_CONTROL:
                icon.frame(icon.texture.uvRect(13, 69, 27, 78));
                break;
            case GRAPHS:
                icon.frame(icon.texture.uvRect(13, 97, 25, 109));
                break;
            case NECROMANCER:
                icon.frame(icon.texture.uvRect(110, 62, 126, 78));
                break;
            case CHEST_ROYAL:
                icon.frame(icon.texture.uvRect(95, 61, 109, 74));
                break;
            case PLAY_GAMES:
                icon.frame(icon.texture.uvRect(79, 61, 93, 74));
                break;
            case BTN_SYNC_IN:
                icon.frame(icon.texture.uvRect(1, 85, 12, 96));
                break;
            case BTN_SYNC_OUT:
                icon.frame(icon.texture.uvRect(14, 85, 25, 96));
                break;
            case BTN_SYNC_REFRESH:
                icon.frame(icon.texture.uvRect(1, 97, 12, 108));
                break;
            case BTN_TARGET:
                icon.frame(icon.texture.uvRect(52, 62, 68, 77));
                break;
            case BTN_QUESTION:
                icon.frame(icon.texture.uvRect(52, 78, 68, 93));
                break;
            case GNOLL:
                icon.frame(icon.texture.uvRect(110, 78, 126, 94));
                break;
            case VK:
                icon.frame(icon.texture.uvRect(62, 94, 78, 110));
                break;
            case FB:
                icon.frame(icon.texture.uvRect(79, 94, 95, 110));
                break;
            case DISCORD:
                icon.frame(icon.texture.uvRect(45, 94, 61, 110));
                break;
            case PLUS:
                icon.frame(icon.texture.uvRect(14, 114, 22, 122));
                break;
            case MINUS:
                icon.frame(icon.texture.uvRect(14, 109, 22, 113));
                break;
        }
        return icon;
    }

    public static Image get(HeroClass cl) {
        try {
            return get(valueOf(cl.name()));
        } catch (IllegalArgumentException err) {
            EventCollector.logException(err);
            return get(NYRDIE);
        }
    }
}
