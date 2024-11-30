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

import com.nyrds.pixeldungeon.items.common.ItemFactory;
import com.nyrds.pixeldungeon.items.common.UnknownItem;
import com.nyrds.pixeldungeon.items.common.armor.NecromancerArmor;
import com.nyrds.pixeldungeon.mechanics.NamedEntityKind;
import com.nyrds.pixeldungeon.mechanics.spells.Spell;
import com.nyrds.pixeldungeon.mechanics.spells.SpellFactory;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.mobs.common.MobFactory;
import com.nyrds.platform.util.StringsManager;
import com.nyrds.util.JsonHelper;
import com.nyrds.util.ModdingMode;
import com.nyrds.util.Util;
import com.watabou.noosa.Image;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Badges;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.Facilitations;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.CharModifier;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.TomeOfMastery;
import com.watabou.pixeldungeon.items.armor.ClassArmor;
import com.watabou.pixeldungeon.items.armor.ElfArmor;
import com.watabou.pixeldungeon.items.armor.GnollArmor;
import com.watabou.pixeldungeon.items.armor.HuntressArmor;
import com.watabou.pixeldungeon.items.armor.MageArmor;
import com.watabou.pixeldungeon.items.armor.RogueArmor;
import com.watabou.pixeldungeon.items.armor.WarriorArmor;
import com.watabou.pixeldungeon.sprites.CharSprite;
import com.watabou.pixeldungeon.ui.BuffIndicator;
import com.watabou.pixeldungeon.ui.QuickSlot;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.utils.Bundle;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import lombok.Getter;
import lombok.SneakyThrows;
import lombok.val;


public enum HeroClass implements CharModifier {

    NONE(null, ClassArmor.class),
    WARRIOR(R.string.HeroClass_War, WarriorArmor.class),
    MAGE(R.string.HeroClass_Mag, MageArmor.class),
    ROGUE(R.string.HeroClass_Rog, RogueArmor.class),
    HUNTRESS(R.string.HeroClass_Hun, HuntressArmor.class),
    ELF(R.string.HeroClass_Elf, ElfArmor.class),
    NECROMANCER(R.string.HeroClass_Necromancer, NecromancerArmor.class),
    GNOLL(R.string.HeroClass_Gnoll, GnollArmor.class);

    private static final String FORBIDDEN_ACTIONS = "forbiddenActions";
    private static final String FRIENDLY_MOBS = "friendlyMobs";

    private static final String COMMON = "common";
    private static final String NON_EXPERT = "non_expert";
    public static final String QUICKSLOT = "quickslot";
    public static final String KNOWN_ITEMS = "knownItems";
    public static final String ALLIES = "allies";
    public static final String KIND = "kind";
    public static final String SPELL = "spell";

    private final Class<? extends ClassArmor> armorClass;

    @Getter
    private final Set<String> forbiddenActions = new HashSet<>();
    private final Set<String> friendlyMobs = new HashSet<>();
    private final Set<String> immunities = new HashSet<>();
    private final Set<String> resistances = new HashSet<>();


    private final Integer titleId;
    static public final JSONObject initHeroes = JsonHelper.readJsonFromAsset(Util.isDebug() && !ModdingMode.inMod() ? "hero/initHeroesDebug.json" : "hero/initHeroes.json");

    private String magicAffinity = Utils.EMPTY_STRING;

    HeroClass(Integer titleId, Class<? extends ClassArmor> armorClass) {
        this.titleId = titleId;
        this.armorClass = armorClass;
    }

    public boolean allowed() {
        return initHeroes.has(name());
    }

    public void initHero(Hero hero) {
        hero.setHeroClass(this);
        initCommon(hero);
        initForClass(hero, hero.getHeroClass().name());

        if (Badges.isUnlocked(masteryBadge()) && hero.getDifficulty() < 3) {
            {
                var tomeOfMastery = new TomeOfMastery();
                if (tomeOfMastery.givesMasteryTo(hero)) {
                    tomeOfMastery.collect(hero);
                }
            }
        }
        hero.updateAwareness();
    }

    @SneakyThrows
    private void initForClass(Hero hero, String className) {
        if (initHeroes.has(className)) {
            try {
                JSONObject classDesc = initHeroes.getJSONObject(className);

                hero.getBelongings().setupFromJson(classDesc);

                if(classDesc.has(ALLIES)) {
                    val allies = classDesc.getJSONArray(ALLIES);
                    for (int i = 0; i < allies.length(); ++i) {
                        val desc = allies.getJSONObject(i);
                        if (desc.has(KIND)) {
                            var mob = MobFactory.mobByName(desc.getString(KIND));
                            mob.fromJson(desc);
                            mob.makePet(hero);
                            hero.initialAlies.add(mob);
                        }
                    }
                }

                if (classDesc.has(QUICKSLOT)) {
                    int slot = 0;
                    JSONArray quickslots = classDesc.getJSONArray(QUICKSLOT);
                    for (int i = 0; i < quickslots.length(); ++i) {

                        val desc = quickslots.getJSONObject(i);
                        if (desc.has(KIND)) {
                            Item item = ItemFactory.createItemFromDesc(desc);
                            if (item.valid()) {
                                item = hero.getItem(item.getEntityKind());
                                if (item.valid()) {
                                    QuickSlot.selectItem(item, slot);
                                    slot++;
                                }
                            }
                        }

                        if (desc.has(SPELL)) {
                            String spellKind = desc.getString(SPELL);
                            if (SpellFactory.hasSpellForName(spellKind)) {
                                Spell spell = SpellFactory.getSpellByName(spellKind);
                                QuickSlot.selectItem(spell.itemForSlot(), slot);
                                slot++;
                            }
                        }
                    }
                }

                if (classDesc.has(KNOWN_ITEMS)) {
                    JSONArray knownItems = classDesc.getJSONArray(KNOWN_ITEMS);
                    for (int i = 0; i < knownItems.length(); ++i) {
                        Item item = ItemFactory.createItemFromDesc(knownItems.getJSONObject(i));
                        if (item instanceof UnknownItem) {
                            ((UnknownItem) item).setKnown();
                        }
                    }
                }

                JsonHelper.readStringSet(classDesc, FORBIDDEN_ACTIONS, forbiddenActions);
                JsonHelper.readStringSet(classDesc, FRIENDLY_MOBS, friendlyMobs);
                JsonHelper.readStringSet(classDesc, Char.IMMUNITIES, immunities);
                JsonHelper.readStringSet(classDesc, Char.RESISTANCES, resistances);

                hero.STR(classDesc.optInt("str", hero.STR()));

                if (Dungeon.isFacilitated(Facilitations.SUPER_STRENGTH)) {
                    hero.STR(hero.STR() + 4);
                }

                hero.lvl(classDesc.optInt("lvl", hero.lvl()));

                hero.hp(hero.ht(classDesc.optInt("hp", hero.ht())));
                hero.getHeroClass().setMagicAffinity(classDesc.optString("magicAffinity", magicAffinity));
                hero.setMaxSkillPoints(classDesc.optInt("maxSp", hero.getSkillPointsMax()));
                hero.setSkillLevel(classDesc.optInt("sl", hero.skillLevel()));
                hero.setSkillPoints(classDesc.optInt("sp", classDesc.optInt("startingSp", hero.getSkillPoints())));

            } catch (Exception e) {
                throw ModdingMode.modException("bad InitHero.json", e);
            }
        }
    }

    private void initCommon(Hero hero) {
        QuickSlot.cleanStorage();
        initForClass(hero, COMMON);
        if (hero.getDifficulty() < 3) {
            initForClass(hero, NON_EXPERT);
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
            case GNOLL:
                return Badges.Badge.MASTERY_GNOLL;
        }
        return null;
    }


    public String title() {
        return StringsManager.getVar(titleId);
    }

    @NotNull
    public String[] perks() {

        switch (this) {
            case WARRIOR:
                return StringsManager.getVars(R.array.HeroClass_WarPerks);
            case MAGE:
                return StringsManager.getVars(R.array.HeroClass_MagPerks);
            case ROGUE:
            default:
                return StringsManager.getVars(R.array.HeroClass_RogPerks);
            case HUNTRESS:
                return StringsManager.getVars(R.array.HeroClass_HunPerks);
            case ELF:
                return StringsManager.getVars(R.array.HeroClass_ElfPerks);
            case NECROMANCER:
                return StringsManager.getVars(R.array.HeroClass_NecromancerPerks);
            case GNOLL:
                return StringsManager.getVars(R.array.HeroClass_GnollPerks);
        }
    }

    public String getDescription() {
        StringBuilder desc = new StringBuilder();
        for (val item : perks()) {
            desc.append("# ");
            desc.append(item);
            desc.append("\n\n");
        }

        return desc.toString();
    }

    public int getGender() {
        switch (this) {
            default:
                return Utils.MASCULINE;
            case HUNTRESS:
                return Utils.FEMININE;
        }
    }

    private static final String CLASS = "class";
    private static final String SPELL_AFFINITY = "affinity";

    public void storeInBundle(Bundle bundle) {
        bundle.put(CLASS, toString());
        bundle.put(SPELL_AFFINITY, getMagicAffinity());
        bundle.put(FORBIDDEN_ACTIONS, forbiddenActions.toArray(new String[0]));
        bundle.put(FRIENDLY_MOBS, friendlyMobs.toArray(new String[0]));
        bundle.put(this + Char.IMMUNITIES, immunities.toArray(new String[0]));
        bundle.put(this + Char.RESISTANCES, resistances.toArray(new String[0]));
    }

    public static HeroClass restoreFromBundle(Bundle bundle) {
        String value = bundle.getString(CLASS);
        HeroClass ret = !value.isEmpty() ? valueOf(value) : ROGUE;

        ret.setMagicAffinity(bundle.getString(SPELL_AFFINITY));

        Collections.addAll(ret.forbiddenActions, bundle.getStringArray(FORBIDDEN_ACTIONS));
        Collections.addAll(ret.friendlyMobs, bundle.getStringArray(FRIENDLY_MOBS));
        Collections.addAll(ret.immunities, bundle.getStringArray(value + Char.IMMUNITIES));
        Collections.addAll(ret.resistances, bundle.getStringArray(value + Char.RESISTANCES));

        return ret;
    }

    @SneakyThrows
    public ClassArmor classArmor() {
        return armorClass.newInstance();
    }

    public String getMagicAffinity() {
        return magicAffinity;
    }

    public void setMagicAffinity(String affinity) {
        magicAffinity = affinity;
    }

    public String tag() {
        if (this == HUNTRESS) {
            return "ranger";
        }

        return name().toLowerCase(Locale.ROOT);
    }

    public boolean forbidden(String action) {
        return forbiddenActions.contains(action);
    }

    public boolean friendlyTo(String mobClass) {
        return friendlyMobs.contains(mobClass);
    }

    @Override
    public int drBonus() {
        return 0;
    }

    @Override
    public int stealthBonus() {
        return 0;
    }

    @Override
    public float speedMultiplier() {
        return 1;
    }

    @Override
    public int defenceProc(Char defender, Char enemy, int damage) {
        return damage;
    }

    @Override
    public int attackProc(Char attacker, Char defender, int damage) {
        return damage;
    }

    @Override
    public int charGotDamage(int damage, NamedEntityKind src, Char target) {
        return damage;
    }

    @Override
    public int regenerationBonus() {
        return 0;
    }

    @Override
    public int manaRegenerationBonus() {
        return 0;
    }

    @Override
    public void charAct() {

    }

    @Override
    public void spellCasted(Char caster, Spell spell) {

    }

    @Override
    public int dewBonus() {
        switch (this) {
            case HUNTRESS:
            case ELF:
                return 1;
            default:
                return 0;
        }
    }

    @Override
    public Set<String> resistances() {
        return resistances;
    }

    @Override
    public Set<String> immunities() {
        return immunities;
    }

    @Override
    public CharSprite.State charSpriteStatus() {
        return CharSprite.State.NONE;
    }

    @Override
    public int defenceSkillBonus() {
        return 0;
    }

    @Override
    public int attackSkillBonus() {
        return 0;
    }

    @Override
    public int icon() {
        return BuffIndicator.NONE;
    }

    @Override
    public String desc() {
        return name();
    }

    public int classIndex() {
        return ordinal() - 1;
    }

    @Override
    public String textureSmall() {
        return Assets.BUFFS_SMALL;
    }

    @Override
    public String textureLarge() {
        return Assets.BUFFS_LARGE;
    }

    @Override
    public Image smallIcon() {
        return null;
    }

    @Override
    public float hasteLevel() {
        if (this == HeroClass.ELF) {
            return 1;
        }
        return 0;
    }
}
