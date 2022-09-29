package com.nyrds.pixeldungeon.mechanics;

/**
 * Created by mike on 17.01.2018.
 * This file is part of Remixed Pixel Dungeon.
 */

public class CommonActions {
    public static final String AC_READ = "Scroll_ACRead";
    public static final String NPC_TALK = "NPC_Talk";
    public static final String NPC_TRADE = "NPC_Trade";
    public static final String AC_EAT = "Food_ACEat";
    public static final String MAC_STEAL = "CharAction_Steal";
    public static final String MAC_TAUNT = "CharAction_Taunt";
    public static final String MAC_PUSH = "CharAction_Push";
    public static final String MAC_ORDER = "CharAction_Order";
    public static final String MAC_HIT = "CharAction_Hit";
    public static final String AC_EQUIP = "EquipableItem_ACEquip";
    public static final String AC_UNEQUIP = "EquipableItem_ACUnequip";
    public static final String AC_DRINK = "Potion_ACDrink";

    public static boolean hideBagOnAction(String action) {
        if (AC_EQUIP.equals(action)) {
            return false;
        }

        if (AC_UNEQUIP.equals(action)) {
            return false;
        }

        return true;
    }
}
