package com.nyrds.pixeldungeon.items.common;

import com.nyrds.LuaInterface;
import com.nyrds.pixeldungeon.items.Carcass;
import com.nyrds.pixeldungeon.items.CustomItem;
import com.nyrds.pixeldungeon.items.Treasury;
import com.nyrds.pixeldungeon.items.artifacts.CandleOfMindVision;
import com.nyrds.pixeldungeon.items.artifacts.SpellBook;
import com.nyrds.pixeldungeon.items.books.TomeOfKnowledge;
import com.nyrds.pixeldungeon.items.chaos.ChaosArmor;
import com.nyrds.pixeldungeon.items.chaos.ChaosBlade;
import com.nyrds.pixeldungeon.items.chaos.ChaosBow;
import com.nyrds.pixeldungeon.items.chaos.ChaosCrystal;
import com.nyrds.pixeldungeon.items.chaos.ChaosStaff;
import com.nyrds.pixeldungeon.items.chaos.ChaosSword;
import com.nyrds.pixeldungeon.items.common.armor.DoctorArmor;
import com.nyrds.pixeldungeon.items.common.armor.NecromancerArmor;
import com.nyrds.pixeldungeon.items.common.armor.NecromancerRobe;
import com.nyrds.pixeldungeon.items.common.armor.PriestArmor;
import com.nyrds.pixeldungeon.items.common.armor.SpiderArmor;
import com.nyrds.pixeldungeon.items.common.rings.RingOfFrost;
import com.nyrds.pixeldungeon.items.drinks.ManaPotion;
import com.nyrds.pixeldungeon.items.guts.HeartOfDarkness;
import com.nyrds.pixeldungeon.items.guts.PseudoAmulet;
import com.nyrds.pixeldungeon.items.guts.armor.GothicArmor;
import com.nyrds.pixeldungeon.items.guts.weapon.melee.Claymore;
import com.nyrds.pixeldungeon.items.guts.weapon.melee.Halberd;
import com.nyrds.pixeldungeon.items.guts.weapon.ranged.CompositeCrossbow;
import com.nyrds.pixeldungeon.items.guts.weapon.ranged.RubyCrossbow;
import com.nyrds.pixeldungeon.items.guts.weapon.ranged.WoodenCrossbow;
import com.nyrds.pixeldungeon.items.icecaves.IceKey;
import com.nyrds.pixeldungeon.items.icecaves.WandOfIcebolt;
import com.nyrds.pixeldungeon.items.material.IceGuardianCoreModule;
import com.nyrds.pixeldungeon.items.material.SoulShard;
import com.nyrds.pixeldungeon.items.material.SpiderQueenCarapace;
import com.nyrds.pixeldungeon.items.necropolis.BlackSkull;
import com.nyrds.pixeldungeon.items.necropolis.BlackSkullOfMastery;
import com.nyrds.pixeldungeon.mobs.common.MobFactory;
import com.nyrds.pixeldungeon.utils.ItemsList;
import com.nyrds.platform.EventCollector;
import com.nyrds.platform.game.Game;
import com.nyrds.platform.util.TrackedRuntimeException;
import com.nyrds.util.JsonHelper;
import com.nyrds.util.ModdingMode;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.items.Amulet;
import com.watabou.pixeldungeon.items.Ankh;
import com.watabou.pixeldungeon.items.ArmorKit;
import com.watabou.pixeldungeon.items.Codex;
import com.watabou.pixeldungeon.items.DewVial;
import com.watabou.pixeldungeon.items.Dewdrop;
import com.watabou.pixeldungeon.items.Gold;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.LloydsBeacon;
import com.watabou.pixeldungeon.items.SpiderCharm;
import com.watabou.pixeldungeon.items.Stylus;
import com.watabou.pixeldungeon.items.TomeOfMastery;
import com.watabou.pixeldungeon.items.Torch;
import com.watabou.pixeldungeon.items.Weightstone;
import com.watabou.pixeldungeon.items.armor.AssasinArmor;
import com.watabou.pixeldungeon.items.armor.BattleMageArmor;
import com.watabou.pixeldungeon.items.armor.BerserkArmor;
import com.watabou.pixeldungeon.items.armor.ClothArmor;
import com.watabou.pixeldungeon.items.armor.ElfArmor;
import com.watabou.pixeldungeon.items.armor.FreeRunnerArmor;
import com.watabou.pixeldungeon.items.armor.GladiatorArmor;
import com.watabou.pixeldungeon.items.armor.GnollArmor;
import com.watabou.pixeldungeon.items.armor.GuardianArmor;
import com.watabou.pixeldungeon.items.armor.HuntressArmor;
import com.watabou.pixeldungeon.items.armor.LeatherArmor;
import com.watabou.pixeldungeon.items.armor.MageArmor;
import com.watabou.pixeldungeon.items.armor.MailArmor;
import com.watabou.pixeldungeon.items.armor.PlateArmor;
import com.watabou.pixeldungeon.items.armor.RogueArmor;
import com.watabou.pixeldungeon.items.armor.ScaleArmor;
import com.watabou.pixeldungeon.items.armor.ScoutArmor;
import com.watabou.pixeldungeon.items.armor.ShamanArmor;
import com.watabou.pixeldungeon.items.armor.SniperArmor;
import com.watabou.pixeldungeon.items.armor.WardenArmor;
import com.watabou.pixeldungeon.items.armor.WarlockArmor;
import com.watabou.pixeldungeon.items.armor.WarriorArmor;
import com.watabou.pixeldungeon.items.armor.WitchdoctorArmor;
import com.watabou.pixeldungeon.items.bags.Keyring;
import com.watabou.pixeldungeon.items.bags.PotionBelt;
import com.watabou.pixeldungeon.items.bags.Quiver;
import com.watabou.pixeldungeon.items.bags.ScrollHolder;
import com.watabou.pixeldungeon.items.bags.SeedPouch;
import com.watabou.pixeldungeon.items.bags.WandHolster;
import com.watabou.pixeldungeon.items.food.Food;
import com.watabou.pixeldungeon.items.keys.GoldenKey;
import com.watabou.pixeldungeon.items.keys.IronKey;
import com.watabou.pixeldungeon.items.keys.SkeletonKey;
import com.watabou.pixeldungeon.items.potions.Potion;
import com.watabou.pixeldungeon.items.quest.CorpseDust;
import com.watabou.pixeldungeon.items.quest.DarkGold;
import com.watabou.pixeldungeon.items.quest.DriedRose;
import com.watabou.pixeldungeon.items.quest.DwarfToken;
import com.watabou.pixeldungeon.items.quest.Pickaxe;
import com.watabou.pixeldungeon.items.quest.RatSkull;
import com.watabou.pixeldungeon.items.rings.RingOfAccuracy;
import com.watabou.pixeldungeon.items.rings.RingOfDetection;
import com.watabou.pixeldungeon.items.rings.RingOfElements;
import com.watabou.pixeldungeon.items.rings.RingOfEvasion;
import com.watabou.pixeldungeon.items.rings.RingOfHaggler;
import com.watabou.pixeldungeon.items.rings.RingOfHaste;
import com.watabou.pixeldungeon.items.rings.RingOfHerbalism;
import com.watabou.pixeldungeon.items.rings.RingOfMending;
import com.watabou.pixeldungeon.items.rings.RingOfPower;
import com.watabou.pixeldungeon.items.rings.RingOfSatiety;
import com.watabou.pixeldungeon.items.rings.RingOfShadows;
import com.watabou.pixeldungeon.items.rings.RingOfStoneWalking;
import com.watabou.pixeldungeon.items.rings.RingOfThorns;
import com.watabou.pixeldungeon.items.scrolls.BlankScroll;
import com.watabou.pixeldungeon.items.scrolls.Scroll;
import com.watabou.pixeldungeon.items.scrolls.ScrollOfIdentify;
import com.watabou.pixeldungeon.items.scrolls.ScrollOfUpgrade;
import com.watabou.pixeldungeon.items.scrolls.ScrollOfWeaponUpgrade;
import com.watabou.pixeldungeon.items.wands.WandOfAmok;
import com.watabou.pixeldungeon.items.wands.WandOfAvalanche;
import com.watabou.pixeldungeon.items.wands.WandOfBlink;
import com.watabou.pixeldungeon.items.wands.WandOfDisintegration;
import com.watabou.pixeldungeon.items.wands.WandOfFirebolt;
import com.watabou.pixeldungeon.items.wands.WandOfFlock;
import com.watabou.pixeldungeon.items.wands.WandOfLightning;
import com.watabou.pixeldungeon.items.wands.WandOfMagicMissile;
import com.watabou.pixeldungeon.items.wands.WandOfPoison;
import com.watabou.pixeldungeon.items.wands.WandOfRegrowth;
import com.watabou.pixeldungeon.items.wands.WandOfSlowness;
import com.watabou.pixeldungeon.items.wands.WandOfTelekinesis;
import com.watabou.pixeldungeon.items.wands.WandOfTeleportation;
import com.watabou.pixeldungeon.items.weapon.melee.BattleAxe;
import com.watabou.pixeldungeon.items.weapon.melee.CompoundBow;
import com.watabou.pixeldungeon.items.weapon.melee.Dagger;
import com.watabou.pixeldungeon.items.weapon.melee.Glaive;
import com.watabou.pixeldungeon.items.weapon.melee.Knuckles;
import com.watabou.pixeldungeon.items.weapon.melee.Kusarigama;
import com.watabou.pixeldungeon.items.weapon.melee.Longsword;
import com.watabou.pixeldungeon.items.weapon.melee.Mace;
import com.watabou.pixeldungeon.items.weapon.melee.Quarterstaff;
import com.watabou.pixeldungeon.items.weapon.melee.RubyBow;
import com.watabou.pixeldungeon.items.weapon.melee.ShortSword;
import com.watabou.pixeldungeon.items.weapon.melee.Spear;
import com.watabou.pixeldungeon.items.weapon.melee.Sword;
import com.watabou.pixeldungeon.items.weapon.melee.WarHammer;
import com.watabou.pixeldungeon.items.weapon.melee.WoodenBow;
import com.watabou.pixeldungeon.items.weapon.missiles.AmokArrow;
import com.watabou.pixeldungeon.items.weapon.missiles.AmokDart;
import com.watabou.pixeldungeon.items.weapon.missiles.Boomerang;
import com.watabou.pixeldungeon.items.weapon.missiles.CommonArrow;
import com.watabou.pixeldungeon.items.weapon.missiles.CurareDart;
import com.watabou.pixeldungeon.items.weapon.missiles.Dart;
import com.watabou.pixeldungeon.items.weapon.missiles.FireArrow;
import com.watabou.pixeldungeon.items.weapon.missiles.FrostArrow;
import com.watabou.pixeldungeon.items.weapon.missiles.HealthArrow;
import com.watabou.pixeldungeon.items.weapon.missiles.HealthDart;
import com.watabou.pixeldungeon.items.weapon.missiles.IncendiaryDart;
import com.watabou.pixeldungeon.items.weapon.missiles.Javelin;
import com.watabou.pixeldungeon.items.weapon.missiles.ParalysisArrow;
import com.watabou.pixeldungeon.items.weapon.missiles.PoisonArrow;
import com.watabou.pixeldungeon.items.weapon.missiles.Shuriken;
import com.watabou.pixeldungeon.items.weapon.missiles.Tamahawk;
import com.watabou.pixeldungeon.plants.DreamweedSeed;
import com.watabou.pixeldungeon.plants.EarthrootSeed;
import com.watabou.pixeldungeon.plants.FadeleafSeed;
import com.watabou.pixeldungeon.plants.FirebloomSeed;
import com.watabou.pixeldungeon.plants.IcecapSeed;
import com.watabou.pixeldungeon.plants.MoongraceSeed;
import com.watabou.pixeldungeon.plants.RotberrySeed;
import com.watabou.pixeldungeon.plants.SorrowmossSeed;
import com.watabou.pixeldungeon.plants.SungrassSeed;
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

        registerItemClass(Amulet.class);
        registerItemClass(Ankh.class);
        registerItemClass(ArmorKit.class);
        registerItemClass(Codex.class);
        registerItemClass(Dewdrop.class);
        registerItemClass(DewVial.class);
        registerItemClass(Gold.class);
        registerItemClass(LloydsBeacon.class);
        registerItemClass(Stylus.class);
        registerItemClass(TomeOfMastery.class);
        registerItemClass(Torch.class);
        registerItemClass(Weightstone.class);

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
        registerItemClass(RatSkull.class);
        registerItemClass(ChaosCrystal.class);
        registerItemClass(ChaosBlade.class);
        registerItemClass(SpiderCharm.class);
        registerItemClass(RingOfDetection.class);
        registerItemClass(RingOfShadows.class);
        registerItemClass(RingOfHerbalism.class);
        registerItemClass(RingOfPower.class);
        registerItemClass(RingOfHaste.class);
        registerItemClass(RingOfSatiety.class);
        registerItemClass(RingOfEvasion.class);
        registerItemClass(RingOfAccuracy.class);
        registerItemClass(RingOfThorns.class);
        registerItemClass(RingOfHaggler.class);
        registerItemClass(RingOfElements.class);
        registerItemClass(RingOfMending.class);
        registerItemClass(RingOfStoneWalking.class);
        registerItemClass(CorpseDust.class);
        registerItemClass(RatKingCrown.class);
        registerItemClass(DriedRose.class);
        registerItemClass(WandOfRegrowth.class);
        registerItemClass(WandOfPoison.class);
        registerItemClass(WandOfLightning.class);
        registerItemClass(WandOfFirebolt.class);
        registerItemClass(WandOfAmok.class);
        registerItemClass(WandOfMagicMissile.class);
        registerItemClass(WandOfFlock.class);
        registerItemClass(WandOfDisintegration.class);
        registerItemClass(WandOfAvalanche.class);
        registerItemClass(WandOfSlowness.class);
        registerItemClass(WandOfBlink.class);
        registerItemClass(WandOfTelekinesis.class);
        registerItemClass(WandOfTeleportation.class);
        registerItemClass(ChaosStaff.class);
        registerItemClass(ChaosSword.class);
        registerItemClass(Spear.class);
        registerItemClass(SacrificialSword.class);
        registerItemClass(Kusarigama.class);
        registerItemClass(Dagger.class);
        registerItemClass(Longsword.class);
        registerItemClass(WarHammer.class);
        registerItemClass(Mace.class);
        registerItemClass(Quarterstaff.class);
        registerItemClass(BattleAxe.class);
        registerItemClass(Glaive.class);
        registerItemClass(Sword.class);
        registerItemClass(ShortSword.class);
        registerItemClass(Knuckles.class);
        registerItemClass(CompoundBow.class);
        registerItemClass(WoodenBow.class);
        registerItemClass(ChaosBow.class);
        registerItemClass(RubyBow.class);
        registerItemClass(Pickaxe.class);
        registerItemClass(IncendiaryDart.class);
        registerItemClass(Dart.class);
        registerItemClass(Boomerang.class);
        registerItemClass(Shuriken.class);
        registerItemClass(Tamahawk.class);
        registerItemClass(Javelin.class);
        registerItemClass(CurareDart.class);
        registerItemClass(FireArrow.class);
        registerItemClass(CommonArrow.class);
        registerItemClass(ParalysisArrow.class);
        registerItemClass(PoisonArrow.class);
        registerItemClass(FrostArrow.class);
        registerItemClass(PlateArmor.class);
        registerItemClass(MailArmor.class);
        registerItemClass(ClothArmor.class);
        registerItemClass(ScaleArmor.class);
        registerItemClass(LeatherArmor.class);
        registerItemClass(GladiatorArmor.class);
        registerItemClass(WarriorArmor.class);
        registerItemClass(HuntressArmor.class);
        registerItemClass(WardenArmor.class);
        registerItemClass(SniperArmor.class);
        registerItemClass(ShamanArmor.class);
        registerItemClass(ElfArmor.class);
        registerItemClass(ScoutArmor.class);
        registerItemClass(BerserkArmor.class);
        registerItemClass(RogueArmor.class);
        registerItemClass(AssasinArmor.class);
        registerItemClass(FreeRunnerArmor.class);
        registerItemClass(MageArmor.class);
        registerItemClass(BattleMageArmor.class);
        registerItemClass(PotionBelt.class);
        registerItemClass(Keyring.class);
        registerItemClass(WandHolster.class);
        registerItemClass(SeedPouch.class);
        registerItemClass(Quiver.class);
        registerItemClass(ScrollHolder.class);
        registerItemClass(SkeletonKey.class);
        registerItemClass(GoldenKey.class);
        registerItemClass(IronKey.class);
        registerItemClass(DarkGold.class);
        registerItemClass(GothicArmor.class);
        registerItemClass(Claymore.class);
        registerItemClass(Halberd.class);
        registerItemClass(WoodenCrossbow.class);
        registerItemClass(CompositeCrossbow.class);
        registerItemClass(RubyCrossbow.class);
        registerItemClass(ChaosArmor.class);
        registerItemClass(HeartOfDarkness.class);
        registerItemClass(GoldenSword.class);
        registerItemClass(BlackSkull.class);
        registerItemClass(SpiderArmor.class);
        //registerItemClass(BladeOfSouls.class);
        registerItemClass(SoulShard.class);
        registerItemClass(NecromancerArmor.class);
        registerItemClass(NecromancerRobe.class);
        registerItemKind(Food.PUMPKIN_PIE, Food::new);
        registerItemKind(Food.ROTTEN_PUMPKIN_PIE, Food::new);
        registerItemKind(Food.CHRISTMAS_TURKEY, Food::new);
        registerItemClass(WandOfIcebolt.class);
        registerItemClass(IceKey.class);
        registerItemClass(ElvenBow.class);
        registerItemClass(RatHide.class);
        registerItemClass(BlackSkullOfMastery.class);
        registerItemClass(CandleOfMindVision.class);

        registerItemClassByName(RotberrySeed.class, "Rotberry.Seed");
        registerItemClassByName(EarthrootSeed.class, "Earthroot.Seed");
        registerItemClassByName(FirebloomSeed.class, "Firebloom.Seed");
        registerItemClassByName(SungrassSeed.class, "Sungrass.Seed");
        registerItemClassByName(DreamweedSeed.class, "Dreamweed.Seed");
        registerItemClassByName(SorrowmossSeed.class, "Sorrowmoss.Seed");
        registerItemClassByName(IcecapSeed.class, "Icecap.Seed");
        registerItemClassByName(FadeleafSeed.class, "Fadeleaf.Seed");
        registerItemClassByName(MoongraceSeed.class, "Moongrace.Seed");

        // seeds moved to top-level classes (batch 20): item kinds unchanged,
        // but untagged saves carry the old inner-class FQNs whose derived kind
        // ("Seed") misses the "<Plant>.Seed" registries — route the old FQNs
        // through the alias map
        Bundle.addAlias(RotberrySeed.class, "com.watabou.pixeldungeon.plants.Rotberry$Seed");
        Bundle.addAlias(RotberrySeed.class, "com.watabou.pixeldungeon.actors.mobs.npcs.WandMaker$Rotberry$Seed");
        Bundle.addAlias(EarthrootSeed.class, "com.watabou.pixeldungeon.plants.Earthroot$Seed");
        Bundle.addAlias(FirebloomSeed.class, "com.watabou.pixeldungeon.plants.Firebloom$Seed");
        Bundle.addAlias(SungrassSeed.class, "com.watabou.pixeldungeon.plants.Sungrass$Seed");
        Bundle.addAlias(DreamweedSeed.class, "com.watabou.pixeldungeon.plants.Dreamweed$Seed");
        Bundle.addAlias(SorrowmossSeed.class, "com.watabou.pixeldungeon.plants.Sorrowmoss$Seed");
        Bundle.addAlias(IcecapSeed.class, "com.watabou.pixeldungeon.plants.Icecap$Seed");
        Bundle.addAlias(FadeleafSeed.class, "com.watabou.pixeldungeon.plants.Fadeleaf$Seed");
        Bundle.addAlias(MoongraceSeed.class, "com.watabou.pixeldungeon.plants.Moongrace$Seed");

        registerItemClass(DwarfToken.class);
        registerItemClass(RatArmor.class);
        registerItemClass(ElvenDagger.class);
        registerItemClass(IceGuardianCoreModule.class);
        registerItemClass(SpiderQueenCarapace.class);
        registerItemClass(RingOfFrost.class);
        registerItemClass(WarlockArmor.class);
        registerItemKind(Food.CANDY, Food::new);
        registerItemClass(WandOfShadowbolt.class);
        registerItemClass(PseudoAmulet.class);
        registerItemClass(ManaPotion.class);
        registerItemClass(TomeOfKnowledge.class);
        registerItemClass(SpellBook.class);

        registerItemClass(GnollArmor.class);
        registerItemClass(GnollTamahawk.class);

        registerItemClass(HealthDart.class);
        registerItemClass(AmokDart.class);
        registerItemClass(HealthArrow.class);
        registerItemClass(AmokArrow.class);
        //registerItemClass(TacticalCrossbow.class); // no item pic

        //registerItemClass(DummyItem.class);
        registerItemClass(GuardianArmor.class);
        registerItemClass(WitchdoctorArmor.class);
        registerItemClass(DoctorArmor.class);
        registerItemClass(PriestArmor.class);

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
