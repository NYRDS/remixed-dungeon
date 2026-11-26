
package com.watabou.pixeldungeon.actors.blobs;

import com.nyrds.LuaInterface;
import com.nyrds.pixeldungeon.items.Treasury;
import com.nyrds.pixeldungeon.items.common.ItemFactory;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.effects.BlobEmitter;
import com.watabou.pixeldungeon.effects.Speck;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.potions.Potion;
import com.watabou.pixeldungeon.items.rings.Ring;
import com.watabou.pixeldungeon.items.scrolls.Scroll;
import com.watabou.pixeldungeon.items.scrolls.ScrollOfUpgrade;
import com.watabou.pixeldungeon.items.scrolls.ScrollOfWeaponUpgrade;
import com.watabou.pixeldungeon.items.wands.Wand;
import com.watabou.pixeldungeon.items.weapon.Weapon.Enchantment;
import com.watabou.pixeldungeon.items.weapon.melee.MeleeWeapon;
import com.watabou.pixeldungeon.plants.Seed;

@LuaInterface
public class WaterOfTransmutation extends WellWater {

    @Override
    protected Item affectItem(Item item) {
        Item transmutedItem = null;

        if (item.getEntityKind().equals(ItemFactory.SPELL_BOOK)) {
            transmutedItem = ItemFactory.itemByName(ItemFactory.SPELL_BOOK);
        } else if (item instanceof MeleeWeapon) {
            transmutedItem = changeWeapon((MeleeWeapon) item);
        } else if (item instanceof Scroll) {
            transmutedItem = changeScroll(item);
        } else if (item instanceof Potion) {
            transmutedItem = changePotion(item);
        } else if (item instanceof Ring) {
            transmutedItem = changeRing(item);
        } else if (item instanceof Wand) {
            transmutedItem = changeWand(item);
        } else if (item instanceof Seed) {
            transmutedItem = changeSeed(item);
        } else {
            // caveman: not transmutable - reject, well keeps its water
            // (regression: returning the item itself dried the well permanently)
            return null;
        }

        clear();
        return transmutedItem;
    }

    @Override
    public void use(BlobEmitter emitter) {
        super.use(emitter);
        emitter.start(Speck.factory(Speck.CHANGE), 0.2f, 0);
    }

    private static final String[][] WEAPON_TRANSMUTE_PAIRS = {
            {"Knuckles", "Dagger"},
            {"Spear", "Quarterstaff"},
            {"Sword", "Mace"},
            {"Longsword", "BattleAxe"},
            {"Glaive", "WarHammer"},
            {"Claymore", "Halberd"},
            {"SacrificialSword", "GoldenSword"},
    };

    private MeleeWeapon changeWeapon(MeleeWeapon w) {
        String kind = w.getEntityKind();
        String pairKind = null;
        for (String[] pair : WEAPON_TRANSMUTE_PAIRS) {
            if (kind.equals(pair[0])) {
                pairKind = pair[1];
                break;
            }
            if (kind.equals(pair[1])) {
                pairKind = pair[0];
                break;
            }
        }

        MeleeWeapon n = pairKind != null ? (MeleeWeapon) ItemFactory.itemByName(pairKind) : null;

        if (n != null) {

            int level = w.level();
            if (level > 0) {
                n.upgrade(level);
            } else if (level < 0) {
                n.degrade(-level);
            }

            if (w.isEnchanted()) {
                n.enchant(Enchantment.random());
            }

            n.setLevelKnown(w.isLevelKnown());
            n.setCursedKnown(w.isCursedKnown());
            n.setCursed(w.isCursed());

            return n;
        } else {
            return null;
        }
    }

    private Item changeRing(Item r) {
        Item n;
        do {
            n = Treasury.getLevelTreasury().random(Treasury.Category.RING);
        } while (n.getEntityKind().equals(r.getEntityKind()));

        n.level(0);

        int level = r.level();
        if (level > 0) {
            n.upgrade(level);
        } else if (level < 0) {
            n.degrade(-level);
        }

        n.setLevelKnown(r.isLevelKnown());
        n.setCursedKnown(r.isCursedKnown());
        n.setCursed(r.isCursed());

        return n;
    }

    private Item changeWand(Item w) {

        Item n;
        do {
            n = Treasury.getLevelTreasury().random(Treasury.Category.WAND);
        } while (n.getEntityKind().equals(w.getEntityKind()));

        n.level(0);
        n.upgrade(w.level());

        n.setLevelKnown(w.isLevelKnown());
        n.setCursedKnown(w.isCursedKnown());
        n.setCursed(w.isCursed());

        return n;
    }

    private Item changeSeed(Item s) {

        Item n;

        do {
            n = Treasury.getLevelTreasury().random(Treasury.Category.SEED);
        } while (n.getEntityKind().equals(s.getEntityKind()));

        return n;
    }

    private Item changeScroll(Item s) {
        if (ScrollOfUpgrade.KIND.equals(s.getEntityKind())) {

            return new ScrollOfWeaponUpgrade();

        } else if (ScrollOfWeaponUpgrade.KIND.equals(s.getEntityKind())) {

            return new ScrollOfUpgrade();

        } else {

            Item n;
            do {
                n = Treasury.getLevelTreasury().random(Treasury.Category.SCROLL);
            } while (n.getEntityKind().equals(s.getEntityKind()));
            return n;
        }
    }

    private Item changePotion(Item p) {
        if (p instanceof Potion && Potion.STRENGTH.equals(((Potion) p).getEntityKind())) {
            return ItemFactory.itemByName(Potion.MIGHT);
        } else {
            Item n;
            do {
                n = Treasury.getLevelTreasury().random(Treasury.Category.POTION);
            } while (n.getEntityKind().equals(p.getEntityKind()));
            return n;
        }
    }

    @Override
    public String tileDesc() {
        return StringsManager.getVar(R.string.WaterOfTransmutation_Info);
    }
}
