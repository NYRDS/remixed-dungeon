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

package com.watabou.pixeldungeon.actors.hero;

import android.support.annotation.NonNull;

import com.nyrds.android.util.JsonHelper;
import com.nyrds.android.util.TrackedRuntimeException;
import com.nyrds.retrodungeon.items.common.ItemFactory;
import com.nyrds.retrodungeon.items.common.UnknownItem;
import com.nyrds.retrodungeon.items.common.armor.NecromancerArmor;
import com.nyrds.retrodungeon.mechanics.ablities.Abilities;
import com.nyrds.retrodungeon.mechanics.ablities.Ordinary;
import com.nyrds.retrodungeon.ml.BuildConfig;
import com.nyrds.retrodungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.Badges;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.KindOfWeapon;
import com.watabou.pixeldungeon.items.TomeOfMastery;
import com.watabou.pixeldungeon.items.armor.Armor;
import com.watabou.pixeldungeon.items.armor.ClassArmor;
import com.watabou.pixeldungeon.items.armor.ElfArmor;
import com.watabou.pixeldungeon.items.armor.HuntressArmor;
import com.watabou.pixeldungeon.items.armor.MageArmor;
import com.watabou.pixeldungeon.items.armor.RogueArmor;
import com.watabou.pixeldungeon.items.armor.WarriorArmor;
import com.watabou.pixeldungeon.items.rings.Artifact;
import com.watabou.pixeldungeon.ui.QuickSlot;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.utils.Bundle;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public enum HeroClass {

    WARRIOR(Game.getVar(R.string.HeroClass_War), WarriorArmor.class, Ordinary.instance),
    MAGE(Game.getVar(R.string.HeroClass_Mag), MageArmor.class, Ordinary.instance),
    ROGUE(Game.getVar(R.string.HeroClass_Rog), RogueArmor.class, Ordinary.instance),
    HUNTRESS(Game.getVar(R.string.HeroClass_Hun), HuntressArmor.class, Ordinary.instance),
    ELF(Game.getVar(R.string.HeroClass_Elf), ElfArmor.class, Ordinary.instance),
    NECROMANCER(Game.getVar(R.string.HeroClass_Necromancer), NecromancerArmor.class, Ordinary.instance);

    private final Class<? extends ClassArmor> armorClass;

    private String    title;
    private Abilities abilities;
    static private JSONObject initHeroes = JsonHelper.readJsonFromAsset(BuildConfig.DEBUG ? "hero/initHeroesDebug.json" : "hero/initHeroes.json");

    private boolean isSpellUser;
    private String  magicAffinity;

    private static final String[] WAR_PERKS         = Game
            .getVars(R.array.HeroClass_WarPerks);
    private static final String[] MAG_PERKS         = Game
            .getVars(R.array.HeroClass_MagPerks);
    private static final String[] ROG_PERKS         = Game
            .getVars(R.array.HeroClass_RogPerks);
    private static final String[] HUN_PERKS         = Game
            .getVars(R.array.HeroClass_HunPerks);
    private static final String[] ELF_PERKS         = Game
            .getVars(R.array.HeroClass_ElfPerks);
    private static final String[] NECROMANCER_PERKS = Game
            .getVars(R.array.HeroClass_NecromancerPerks);

    HeroClass(String title, Class<? extends ClassArmor> armorClass, Abilities abilities) {
        this.title = title;
        this.armorClass = armorClass;
        this.abilities = abilities;
    }

    public boolean allowed() {
        return initHeroes.has(name());
    }

    public void initHero(Hero hero) {
        hero.heroClass = this;
        initCommon(hero);
        initForClass(hero, hero.heroClass.name());

        hero.setGender(getGender());

        if (Badges.isUnlocked(masteryBadge()) && hero.getDifficulty() < 3) {
            {
                if (hero.heroClass != HeroClass.NECROMANCER) {
                    new TomeOfMastery().collect(hero);
                }
            }
        }
        hero.updateAwareness();
    }

    private static void initForClass(Hero hero, String className) {
        if (initHeroes.has(className)) {
            try {
                JSONObject classDesc = initHeroes.getJSONObject(className);
                if (classDesc.has("armor")) {
                    Armor armor = (Armor) ItemFactory.createItemFromDesc(classDesc.getJSONObject("armor"));
                    hero.belongings.armor = armor;
                }

                if (classDesc.has("weapon")) {
                    KindOfWeapon weapon = (KindOfWeapon) ItemFactory.createItemFromDesc(classDesc.getJSONObject("weapon"));
                    hero.belongings.weapon = weapon;
                }

                if (classDesc.has("ring1")) {
                    hero.belongings.ring1 = (Artifact) ItemFactory.createItemFromDesc(classDesc.getJSONObject("ring1"));
                    hero.belongings.ring1.activate(hero);
                }

                if (classDesc.has("ring2")) {
                    hero.belongings.ring2 = (Artifact) ItemFactory.createItemFromDesc(classDesc.getJSONObject("ring2"));
                    hero.belongings.ring2.activate(hero);
                }

                if (classDesc.has("items")) {
                    JSONArray items = classDesc.getJSONArray("items");
                    for (int i = 0; i < items.length(); ++i) {
                        hero.collect(ItemFactory.createItemFromDesc(items.getJSONObject(i)));
                    }
                }

                if (classDesc.has("quickslot")) {
                    int slot = 0;
                    JSONArray quickslots = classDesc.getJSONArray("quickslot");
                    for (int i = 0; i < quickslots.length(); ++i) {
                        Item item = ItemFactory.createItemFromDesc(quickslots.getJSONObject(i));

                        if (hero.belongings.getItem(item.getClass()) != null) {
                            QuickSlot.selectItem(hero.belongings.getItem(item.getClass()), slot);
                            slot++;
                        }
                    }
                }

                if (classDesc.has("knownItems")) {
                    JSONArray knownItems = classDesc.getJSONArray("knownItems");
                    for (int i = 0; i < knownItems.length(); ++i) {
                        Item item = ItemFactory.createItemFromDesc(knownItems.getJSONObject(i));
                        if (item instanceof UnknownItem) {
                            ((UnknownItem) item).setKnown();
                        }
                    }
                }

                hero.STR(classDesc.optInt("str", hero.STR()));
                hero.hp(hero.ht(classDesc.optInt("hp", hero.ht())));
                hero.heroClass.isSpellUser(classDesc.optBoolean("isSpellUser", false));
                hero.heroClass.setMagicAffinity(classDesc.optString("magicAffinity", "Common"));
                hero.setMaxSoulPoints(classDesc.optInt("maxSp", 10));
                hero.setSoulPoints(classDesc.optInt("startingSp", 0));

            } catch (JSONException e) {
                throw new TrackedRuntimeException(e);
            } catch (InstantiationException e) {
                throw new TrackedRuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new TrackedRuntimeException(e);
            }
        }
    }

    private static void initCommon(Hero hero) {
        QuickSlot.cleanStorage();
        initForClass(hero, "common");
        if (hero.getDifficulty() < 3) {
            initForClass(hero, "non_expert");
        }
    }

    public Badges.Badge masteryBadge() {
        switch (this) {
            case WARRIOR:
                return Badges.Badge.MASTERY_WARRIOR;
            case MAGE:
                return Badges.Badge.MASTERY_MAGE;
            case ROGUE:
                return Badges.Badge.MASTERY_ROGUE;
            case HUNTRESS:
                return Badges.Badge.MASTERY_HUNTRESS;
            case ELF:
                return Badges.Badge.MASTERY_ELF;
            case NECROMANCER:
                return Badges.Badge.MASTERY_NECROMANCER;
        }
        return null;
    }


    public String title() {
        return title;
    }

    @NonNull
    public String[] perks() {

        switch (this) {
            case WARRIOR:
                return WAR_PERKS;
            case MAGE:
                return MAG_PERKS;
            case ROGUE:
            default:
                return ROG_PERKS;
            case HUNTRESS:
                return HUN_PERKS;
            case ELF:
                return ELF_PERKS;
            case NECROMANCER:
                return NECROMANCER_PERKS;
        }
    }

    public int getGender() {
        switch (this) {
            case WARRIOR:
            case MAGE:
            case ROGUE:
            case ELF:
            case NECROMANCER:
                return Utils.MASCULINE;
            case HUNTRESS:
                return Utils.FEMININE;
        }
        return Utils.NEUTER;
    }

    private static final String CLASS          = "class";
    private static final String SPELL_AFFINITY = "affinity";

    public void storeInBundle(Bundle bundle) {
        bundle.put(CLASS, toString());
        bundle.put(SPELL_AFFINITY, getMagicAffinity());
    }

    public static HeroClass restoreFromBundle(Bundle bundle) {
        String value = bundle.getString(CLASS);
        HeroClass ret = value.length() > 0 ? valueOf(value) : ROGUE;
        ret.setMagicAffinity(bundle.getString(SPELL_AFFINITY));
        return ret;
    }

    public ClassArmor classArmor() {
        try {
            return armorClass.newInstance();
        } catch (Exception e) {
            throw new TrackedRuntimeException(e);
        }
    }

    Abilities getAbilities() {
        return abilities;
    }

    public boolean isSpellUser() {
        return isSpellUser;
    }

    public void isSpellUser(boolean b) {
        isSpellUser = b;
    }

    public String getMagicAffinity() {
        return magicAffinity;
    }

    public void setMagicAffinity(String affinity) {
        magicAffinity = affinity;
    }
}
