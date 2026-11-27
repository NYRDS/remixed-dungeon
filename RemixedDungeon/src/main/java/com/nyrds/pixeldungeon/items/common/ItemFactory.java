package com.nyrds.pixeldungeon.items.common;

import com.nyrds.LuaInterface;
import com.nyrds.pixeldungeon.items.Carcass;
import com.nyrds.pixeldungeon.items.CustomItem;
import com.nyrds.pixeldungeon.items.Treasury;
import com.nyrds.pixeldungeon.mobs.common.MobFactory;
import com.nyrds.pixeldungeon.utils.ItemsList;
import com.nyrds.platform.EventCollector;
import com.nyrds.platform.game.Game;
import com.nyrds.platform.util.TrackedRuntimeException;
import com.nyrds.util.JsonHelper;
import com.nyrds.util.ModdingMode;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.items.Gold;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.armor.Armor;
import com.watabou.pixeldungeon.items.armor.ClassArmor;
import com.watabou.pixeldungeon.items.bags.Bag;
import com.watabou.pixeldungeon.items.food.Food;
import com.watabou.pixeldungeon.items.keys.Key;
import com.watabou.pixeldungeon.items.potions.Potion;
import com.watabou.pixeldungeon.items.rings.Ring;
import com.watabou.pixeldungeon.items.scrolls.BlankScroll;
import com.watabou.pixeldungeon.items.scrolls.Scroll;
import com.watabou.pixeldungeon.items.scrolls.ScrollOfIdentify;
import com.watabou.pixeldungeon.items.scrolls.ScrollOfUpgrade;
import com.watabou.pixeldungeon.items.scrolls.ScrollOfWeaponUpgrade;
import com.watabou.pixeldungeon.items.wands.Wand;
import com.watabou.pixeldungeon.items.weapon.melee.KindOfBow;
import com.watabou.pixeldungeon.items.weapon.melee.MeleeWeapon;
import com.watabou.pixeldungeon.items.weapon.missiles.Arrow;
import com.watabou.pixeldungeon.items.weapon.missiles.Dart;
import com.watabou.pixeldungeon.items.weapon.missiles.MissileWeapon;
import com.watabou.pixeldungeon.plants.Seed;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.utils.Bundle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONException;
import org.json.JSONObject;


public class ItemFactory {

    public static final String GOLD = "Gold";
    public static final String AMULET = "Amulet";
    public static final String CANDY_OF_DEATH = "CandyOfDeath";
    public static final String SPELL_BOOK = "SpellBook";
    public static final String CHAOS_SHIELD = "ChaosShield";
    public static final String TENGU_LIVER = "TenguLiver";
    public static final String RAW_FISH = "RawFish";
    public static final String REMIXED_PICKAXE = "RemixedPickaxe";

    static private final Map<String, Class<? extends Item>> mItemsList = new HashMap<>();
    static private final Map<Class<? extends Item>, String> mNamesList = new HashMap<>();
    // data kinds served by a kind-parameterized framework class (Food, ...):
    // kind string -> constructor taking the kind. Checked before mItemsList so
    // a framework kind can never fall into the CustomItem tail.
    static private final Map<String, Function<String, Item>> mKindFactories = new HashMap<>();

    static {
        initItemsMap();

        for(String itemFile: ModdingMode.listResources("scripts/items", (dir, name) -> name.endsWith(".lua"))) {
            String kind = itemFile.replace(".lua", Utils.EMPTY_STRING);
            if (mKindFactories.containsKey(kind)) {
                continue; // framework kinds carry their scripts/items/<Kind>.lua for Food, not as CustomItems
            }
            mItemsList.put(kind, CustomItem.class);
        }
    }

    private static void registerItemClass(Class<? extends Item> itemClass) {
        registerItemClassByName(itemClass, itemClass.getSimpleName());
    }

    private static void registerItemClassByName(Class<? extends Item> itemClass, String name) {
        mItemsList.put(name, itemClass);
        mNamesList.put(itemClass, name);
    }

    /**
     * Registers a data kind served by a kind-parameterized framework class
     * (e.g. Food): the factory receives the kind string and returns the
     * configured instance. Per-kind data/behavior lives in
     * scripts/items/<Kind>.lua.
     */
    private static void registerItemKind(@NotNull String kind, @NotNull Function<String, Item> factory) {
        mKindFactories.put(kind, factory);
    }

    private static void initItemsMap() {

        registerItemClass(Gold.class);

        // data keys and bags: Key/Bag kind ctors, images from scripts
        registerItemClass(Key.class);
        registerItemKind("IronKey", Key::new);
        registerItemKind("GoldenKey", Key::new);
        registerItemKind("SkeletonKey", Key::new);
        registerItemClass(Bag.class);
        registerItemKind("Keyring", Bag::new);
        registerItemKind("PotionBelt", Bag::new);
        registerItemKind("Quiver", Bag::new);
        registerItemKind("ScrollHolder", Bag::new);
        registerItemKind("SeedPouch", Bag::new);
        registerItemKind("WandHolster", Bag::new);

        // the framework classes themselves resolve as kinds (name lookup
        // during Item field init runs before the kind ctor arg lands; bare
        // instances restore as their canonical kind: ration/healing/identify)
        registerItemClass(Food.class);
        registerItemClass(Potion.class);
        registerItemClass(Scroll.class);

        // data foods: one Food framework + scripts/items/<Kind>.lua each
        registerItemKind(Food.RATION, Food::new);
        registerItemKind(Food.OVERPRICED_RATION, Food::new);
        registerItemKind(Food.PASTY, Food::new);
        registerItemKind(Food.PSEUDO_PASTY, Food::new);
        registerItemKind(Food.MYSTERY_MEAT, Food::new);
        registerItemKind(Food.CHARGRILLED_MEAT, Food::new);
        registerItemKind(Food.FROZEN_CARPACCIO, Food::new);
        registerItemKind(Food.ROTTEN_MEAT, Food::new);
        registerItemKind(Food.ROTTEN_PASTY, Food::new);
        registerItemKind(Food.ROTTEN_RATION, Food::new);
        registerItemKind(Food.CANDY_OF_DEATH, Food::new);

        // data scrolls: one Scroll framework + scripts/items/<Kind>.lua each
        // (Identify/Upgrade/WeaponUpgrade keep thin java classes - inventory
        // selection UI; BlankScroll too)
        registerItemClass(BlankScroll.class);
        registerItemClass(ScrollOfWeaponUpgrade.class);
        registerItemClass(ScrollOfIdentify.class);
        registerItemClass(ScrollOfUpgrade.class);
        registerItemKind(Scroll.MAGIC_MAPPING, Scroll::new);
        registerItemKind(Scroll.RECHARGING, Scroll::new);
        registerItemKind(Scroll.REMOVE_CURSE, Scroll::new);
        registerItemKind(Scroll.TELEPORTATION, Scroll::new);
        registerItemKind(Scroll.CHALLENGE, Scroll::new);
        registerItemKind(Scroll.TERROR, Scroll::new);
        registerItemKind(Scroll.LULLABY, Scroll::new);
        registerItemKind(Scroll.PSIONIC_BLAST, Scroll::new);
        registerItemKind(Scroll.MIRROR_IMAGE, Scroll::new);
        registerItemKind(Scroll.DOMINATION, Scroll::new);
        registerItemKind(Scroll.SUMMONING, Scroll::new);
        registerItemKind(Scroll.CURSE, Scroll::new);

        // data potions: one Potion framework + scripts/items/<Kind>.lua each
        registerItemKind(Potion.HEALING, Potion::new);
        registerItemKind(Potion.EXPERIENCE, Potion::new);
        registerItemKind(Potion.TOXIC_GAS, Potion::new);
        registerItemKind(Potion.LIQUID_FLAME, Potion::new);
        registerItemKind(Potion.STRENGTH, Potion::new);
        registerItemKind(Potion.PARALYTIC_GAS, Potion::new);
        registerItemKind(Potion.LEVITATION, Potion::new);
        registerItemKind(Potion.MIND_VISION, Potion::new);
        registerItemKind(Potion.PURITY, Potion::new);
        registerItemKind(Potion.INVISIBILITY, Potion::new);
        registerItemKind(Potion.MIGHT, Potion::new);
        registerItemKind(Potion.FROST, Potion::new);
        registerItemKind(Potion.MANA, Potion::new);
        // data rings: one Ring framework + scripts/items/<Kind>.lua each
        // (RingOfFrost/RingOfStoneWalking included, always-known kinds)
        registerItemClass(Ring.class);
        registerItemKind("RingOfMending", Ring::new);
        registerItemKind("RingOfDetection", Ring::new);
        registerItemKind("RingOfShadows", Ring::new);
        registerItemKind("RingOfPower", Ring::new);
        registerItemKind("RingOfHerbalism", Ring::new);
        registerItemKind("RingOfAccuracy", Ring::new);
        registerItemKind("RingOfEvasion", Ring::new);
        registerItemKind("RingOfSatiety", Ring::new);
        registerItemKind("RingOfHaste", Ring::new);
        registerItemKind("RingOfHaggler", Ring::new);
        registerItemKind("RingOfElements", Ring::new);
        registerItemKind("RingOfThorns", Ring::new);
        registerItemKind("RingOfStoneWalking", Ring::new);
        // data wands: one Wand framework + scripts/items/<Kind>.lua each
        // (MagicMissile always-known; Icebolt/Shadowbolt are non-handler kinds;
        // ChaosStaff stays a java subclass with no kind script)
        registerItemClass(Wand.class);
        registerItemKind("WandOfTeleportation", Wand::new);
        registerItemKind("WandOfSlowness", Wand::new);
        registerItemKind("WandOfFirebolt", Wand::new);
        registerItemKind("WandOfPoison", Wand::new);
        registerItemKind("WandOfRegrowth", Wand::new);
        registerItemKind("WandOfBlink", Wand::new);
        registerItemKind("WandOfLightning", Wand::new);
        registerItemKind("WandOfAmok", Wand::new);
        registerItemKind("WandOfTelekinesis", Wand::new);
        registerItemKind("WandOfFlock", Wand::new);
        registerItemKind("WandOfDisintegration", Wand::new);
        registerItemKind("WandOfAvalanche", Wand::new);
        registerItemKind("WandOfMagicMissile", Wand::new);
        // data melee weapons + bows: MeleeWeapon/KindOfBow frameworks +
        // scripts/items/<Kind>.lua each
        // with the mine flow; ShortSword is a reforger kind)
        registerItemClass(MeleeWeapon.class);
        registerItemKind("Dagger", MeleeWeapon::new);
        registerItemKind("Knuckles", MeleeWeapon::new);
        // the fused chaos wand (was chaos/ChaosStaff.java, class-served with a
        // Class.forName restore - the kind registry serves old saves now)
        registerItemKind("ChaosStaff", Wand::new);
        registerItemKind("ShortSword", MeleeWeapon::new);
        registerItemKind("Quarterstaff", MeleeWeapon::new);
        registerItemKind("Spear", MeleeWeapon::new);
        registerItemKind("SacrificialSword", MeleeWeapon::new);
        registerItemKind("Sword", MeleeWeapon::new);
        registerItemKind("Mace", MeleeWeapon::new);
        registerItemKind("GoldenSword", MeleeWeapon::new);
        registerItemKind("Kusarigama", MeleeWeapon::new);
        registerItemKind("Longsword", MeleeWeapon::new);
        registerItemKind("BattleAxe", MeleeWeapon::new);
        registerItemKind("WarHammer", MeleeWeapon::new);
        registerItemKind("Glaive", MeleeWeapon::new);
        registerItemKind("Claymore", MeleeWeapon::new);
        registerItemKind("Halberd", MeleeWeapon::new);
        registerItemKind("TitanSword", MeleeWeapon::new);
        registerItemKind("ElvenDagger", MeleeWeapon::new);
        registerItemKind("ChaosBlade", MeleeWeapon::new);
        registerItemKind("ChaosSword", MeleeWeapon::new);
        registerItemClass(KindOfBow.class);
        registerItemKind("WoodenBow", KindOfBow::new);
        registerItemKind("ElvenBow", KindOfBow::new);
        registerItemKind("CompoundBow", KindOfBow::new);
        registerItemKind("RubyBow", KindOfBow::new);
        registerItemKind("ChaosBow", KindOfBow::new);
        registerItemKind("WoodenCrossbow", KindOfBow::new);
        registerItemKind("CompositeCrossbow", KindOfBow::new);
        registerItemKind("RubyCrossbow", KindOfBow::new);
        // data missiles: MissileWeapon/Arrow/Dart frameworks + scripts each
        registerItemClass(MissileWeapon.class);
        registerItemKind("Shuriken", MissileWeapon::new);
        registerItemKind("Javelin", MissileWeapon::new);
        registerItemKind("Tamahawk", MissileWeapon::new);
        registerItemKind("GnollTamahawk", MissileWeapon::new);
        registerItemKind("Boomerang", MissileWeapon::new);
        registerItemClass(Arrow.class);
        registerItemKind("CommonArrow", Arrow::new);
        registerItemKind("FireArrow", Arrow::new);
        registerItemKind("FrostArrow", Arrow::new);
        registerItemKind("ParalysisArrow", Arrow::new);
        registerItemKind("PoisonArrow", Arrow::new);
        registerItemKind("HealthArrow", Arrow::new);
        registerItemKind("AmokArrow", Arrow::new);
        registerItemKind("Dart", Dart::new);
        registerItemKind("CurareDart", Dart::new);
        registerItemKind("IncendiaryDart", Dart::new);
        registerItemKind("HealthDart", Dart::new);
        registerItemKind("AmokDart", Dart::new);
        // data armors: Armor/ClassArmor frameworks + scripts/items/<Kind>.lua each
        registerItemClass(Armor.class);
        registerItemKind("ClothArmor", Armor::new);
        registerItemKind("LeatherArmor", Armor::new);
        registerItemKind("MailArmor", Armor::new);
        registerItemKind("ScaleArmor", Armor::new);
        registerItemKind("PlateArmor", Armor::new);
        registerItemKind("GothicArmor", Armor::new);
        registerItemKind("RatArmor", Armor::new);
        registerItemKind("SpiderArmor", Armor::new);
        registerItemKind("NecromancerRobe", Armor::new);
        registerItemKind("ChaosArmor", Armor::new);
        registerItemClass(ClassArmor.class);
        registerItemKind("WarriorArmor", ClassArmor::new);
        registerItemKind("GladiatorArmor", ClassArmor::new);
        registerItemKind("BerserkArmor", ClassArmor::new);
        registerItemKind("MageArmor", ClassArmor::new);
        registerItemKind("BattleMageArmor", ClassArmor::new);
        registerItemKind("WarlockArmor", ClassArmor::new);
        registerItemKind("RogueArmor", ClassArmor::new);
        registerItemKind("AssasinArmor", ClassArmor::new);
        registerItemKind("FreeRunnerArmor", ClassArmor::new);
        registerItemKind("HuntressArmor", ClassArmor::new);
        registerItemKind("SniperArmor", ClassArmor::new);
        registerItemKind("WardenArmor", ClassArmor::new);
        registerItemKind("ElfArmor", ClassArmor::new);
        registerItemKind("ScoutArmor", ClassArmor::new);
        registerItemKind("ShamanArmor", ClassArmor::new);
        registerItemKind("GnollArmor", ClassArmor::new);
        registerItemKind("GuardianArmor", ClassArmor::new);
        registerItemKind("WitchdoctorArmor", ClassArmor::new);
        registerItemKind("DoctorArmor", ClassArmor::new);
        registerItemKind("NecromancerArmor", ClassArmor::new);
        registerItemKind("PriestArmor", ClassArmor::new);
        registerItemKind(Food.PUMPKIN_PIE, Food::new);
        registerItemKind(Food.ROTTEN_PUMPKIN_PIE, Food::new);
        registerItemKind(Food.CHRISTMAS_TURKEY, Food::new);
        registerItemKind("WandOfIcebolt", Wand::new);

        // data seeds: one Seed framework + scripts/items/<Kind>.lua each
        // (dotted kinds load through the dofile/asInstance path - require's
        // searchpath maps every dot to a separator)
        registerItemKind("Rotberry.Seed", Seed::new);
        registerItemKind("Earthroot.Seed", Seed::new);
        registerItemKind("Firebloom.Seed", Seed::new);
        registerItemKind("Sungrass.Seed", Seed::new);
        registerItemKind("Dreamweed.Seed", Seed::new);
        registerItemKind("Sorrowmoss.Seed", Seed::new);
        registerItemKind("Icecap.Seed", Seed::new);
        registerItemKind("Fadeleaf.Seed", Seed::new);
        registerItemKind("Moongrace.Seed", Seed::new);

        // seeds moved to data kinds (batch 20/21): item kinds unchanged, but
        // untagged saves carry the old inner-class FQNs whose derived kind
        // ("Seed") misses the "<Plant>.Seed" registries — route the old FQNs
        // through the alias map
        Bundle.addKindAlias("Rotberry.Seed", "com.watabou.pixeldungeon.plants.Rotberry$Seed");
        Bundle.addKindAlias("Rotberry.Seed", "com.watabou.pixeldungeon.actors.mobs.npcs.WandMaker$Rotberry$Seed");
        Bundle.addKindAlias("Earthroot.Seed", "com.watabou.pixeldungeon.plants.Earthroot$Seed");
        Bundle.addKindAlias("Firebloom.Seed", "com.watabou.pixeldungeon.plants.Firebloom$Seed");
        Bundle.addKindAlias("Sungrass.Seed", "com.watabou.pixeldungeon.plants.Sungrass$Seed");
        Bundle.addKindAlias("Dreamweed.Seed", "com.watabou.pixeldungeon.plants.Dreamweed$Seed");
        Bundle.addKindAlias("Sorrowmoss.Seed", "com.watabou.pixeldungeon.plants.Sorrowmoss$Seed");
        Bundle.addKindAlias("Icecap.Seed", "com.watabou.pixeldungeon.plants.Icecap$Seed");
        Bundle.addKindAlias("Fadeleaf.Seed", "com.watabou.pixeldungeon.plants.Fadeleaf$Seed");
        Bundle.addKindAlias("Moongrace.Seed", "com.watabou.pixeldungeon.plants.Moongrace$Seed");
        // the rage aura is a script buff now: alias the old CorpseDust FQN to
        // its kind string
        Bundle.addKindAlias("RageBuff", "com.watabou.pixeldungeon.items.quest.CorpseDust$UndeadRageAuraBuff");
        // Pickaxe + ChaosStaff: old quest/staff saves restore as kind strings
        Bundle.addKindAlias("Pickaxe", "com.watabou.pixeldungeon.items.quest.Pickaxe");
        Bundle.addKindAlias("ChaosStaff", "com.nyrds.pixeldungeon.items.chaos.ChaosStaff");

        registerItemKind("RingOfFrost", Ring::new);
        registerItemKind(Food.CANDY, Food::new);
        registerItemKind("WandOfShadowbolt", Wand::new);



        //registerItemClass(DummyItem.class);

    }

    public static boolean isValidItemClass(String itemClass) {
        return mKindFactories.containsKey(itemClass) || mItemsList.containsKey(itemClass);
    }

    /**
     * Strict gate for save-restore: java registration, a lua item def, or a
     * Carcass composite kind. Explicitly excludes the CustomItem/Gold tail of
     * {@link #itemByName} - an unknown kind must resolve to null, never Gold.
     */
    public static boolean hasItem(@NotNull String itemClass) {
        if (mKindFactories.containsKey(itemClass)) {
            return true;
        }
        if (isValidItemClass(itemClass)) {
            return true;
        }
        if (itemClass.startsWith(Carcass.CARCASS_OF)) {
            return MobFactory.hasMob(itemClass.substring(Carcass.CARCASS_OF.length()));
        }
        return ModdingMode.isResourceExists("scripts/items/" + itemClass + ".lua");
    }

    /**
     * Gated resolver for save-restore: null unless {@link #hasItem} passes.
     */
    @Nullable
    public static Item tryByName(@NotNull String itemClass) {
        if (!hasItem(itemClass)) {
            return null;
        }

        try {
            Function<String, Item> kindFactory = mKindFactories.get(itemClass);
            if (kindFactory != null) {
                return kindFactory.apply(itemClass);
            }
            Class<? extends Item> itemJavaClass = mItemsList.get(itemClass);
            if (itemJavaClass != null && itemJavaClass != CustomItem.class) {
                return itemJavaClass.newInstance();
            }
            if (itemClass.startsWith(Carcass.CARCASS_OF)) {
                return new Carcass(MobFactory.mobByName(itemClass.substring(Carcass.CARCASS_OF.length())));
            }
            return new CustomItem(itemClass);
        } catch (Exception e) {
            EventCollector.logException(e, itemClass);
            return null;
        }
    }

    @NotNull
    public static Item  itemByName(@NotNull String selectedItemClass) {
        try {
            {
                Function<String, Item> kindFactory = mKindFactories.get(selectedItemClass);
                if (kindFactory != null) {
                    return kindFactory.apply(selectedItemClass);
                }

                Class<? extends Item> itemClass = mItemsList.get(selectedItemClass);

                if (itemClass != null && itemClass != CustomItem.class) {
                    return itemClass.newInstance();
                }
            }

            if (selectedItemClass.startsWith(Carcass.CARCASS_OF))  {
                String mobKind  = selectedItemClass.substring(Carcass.CARCASS_OF.length());
                if (MobFactory.hasMob(mobKind)) {
                    try {
                        return new Carcass(MobFactory.mobByName(mobKind));
                    } catch (Exception e) {
                        EventCollector.logException(e, mobKind);
                        return new Carcass(new Mob(MobFactory.SNAIL));
                    }
                }
                return new Carcass(new Mob(MobFactory.SNAIL));
            }

            try {
                return new CustomItem(selectedItemClass);
            } catch (Exception e){
                EventCollector.logException(e,selectedItemClass);
                Game.toast("Unknown item: [%s], spawning Gold instead", selectedItemClass);
                return itemByName(GOLD);
            }

        } catch (InstantiationException | IllegalAccessException e) {
            throw new TrackedRuntimeException("itemFactory", e);
        }
    }

    public static String itemNameByClass(Class<? extends Item> clazz) {

        String ret = mNamesList.get(clazz);
        if (ret == null) {
            EventCollector.logException("Unregistered entry " + clazz.getCanonicalName());
            ret = "NoItem";
        }
        return ret;
    }

    @LuaInterface
    public static Item createItem(String itemClass, String jsonDesc) throws JSONException {
        Item item = itemByName(itemClass);
        item.fromJson(JsonHelper.readJsonFromString(jsonDesc));
        return item;
    }

    @NotNull
    public static Item createItemFromDesc(@NotNull JSONObject itemDesc) throws  JSONException {
        String kind = itemDesc.getString("kind");

        if (kind.equals("NoItem")) {
            return ItemsList.DUMMY;
        }

        Item item = ItemFactory.itemByName(kind);
        item.fromJson(itemDesc);

        return Treasury.get().check(item);
    }

    public static Item virtual(Item sourceItem) {
        Item item = itemByName(sourceItem.getEntityKind());
        item.quantity(0);
        item.setImage(sourceItem.image());
        item.setCustomImage(sourceItem.getCustomImage());
        return item;
    }

    public static List<Item> allItems() {
        List<Item> items = new ArrayList<>();

        for(String itemClass:mItemsList.keySet()) {
            GLog.debug("Spawning: %s",itemClass);
            items.add(itemByName(itemClass));
        }

        return items;
    }
}
