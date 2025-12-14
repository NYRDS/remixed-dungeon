package com.nyrds.pixeldungeon.desktop;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.nyrds.platform.game.QuickModTest;
import com.nyrds.pixeldungeon.items.common.ItemFactory;
import com.nyrds.pixeldungeon.mobs.common.MobFactory;
import com.nyrds.platform.gfx.BitmapData;
import com.watabou.gltextures.SmartTexture;
import com.watabou.noosa.Image;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.hero.HeroClass;
import com.watabou.pixeldungeon.actors.hero.HeroSubClass;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.armor.ClassArmor;
import com.watabou.pixeldungeon.items.potions.Potion;
import com.watabou.pixeldungeon.items.rings.Ring;
import com.watabou.pixeldungeon.items.scrolls.Scroll;
import com.watabou.pixeldungeon.items.wands.Wand;
// Note: There's no WandOfBlastWave in the game, using alternatives
import com.watabou.pixeldungeon.items.wands.WandOfDisintegration;
import com.watabou.pixeldungeon.items.wands.WandOfFirebolt;
import com.watabou.pixeldungeon.items.wands.WandOfLightning;
import com.watabou.pixeldungeon.items.wands.WandOfMagicMissile;
import com.watabou.pixeldungeon.items.wands.WandOfRegrowth;
import com.watabou.pixeldungeon.items.wands.WandOfSlowness;
import com.watabou.pixeldungeon.items.wands.WandOfTeleportation;
import com.watabou.pixeldungeon.items.wands.WandOfPoison;
import com.watabou.pixeldungeon.items.wands.WandOfAmok;
import com.watabou.pixeldungeon.items.wands.WandOfFlock;
import com.watabou.pixeldungeon.items.wands.SimpleWand;
import com.watabou.pixeldungeon.items.weapon.Weapon;
import com.watabou.pixeldungeon.items.weapon.melee.MeleeWeapon;
import com.watabou.pixeldungeon.items.weapon.melee.ShortSword;
import com.watabou.pixeldungeon.items.weapon.melee.Mace;
import com.watabou.pixeldungeon.items.weapon.melee.Dagger;
import com.watabou.pixeldungeon.items.weapon.melee.Glaive;
import com.watabou.pixeldungeon.items.weapon.melee.Spear;
import com.watabou.pixeldungeon.items.weapon.melee.WarHammer;
import com.watabou.pixeldungeon.items.weapon.melee.Quarterstaff;
import com.watabou.pixeldungeon.items.weapon.melee.Knuckles;
import com.watabou.pixeldungeon.items.weapon.melee.Longsword;
import com.watabou.pixeldungeon.items.weapon.melee.Sword;
import com.watabou.pixeldungeon.items.weapon.melee.BattleAxe;
import com.nyrds.pixeldungeon.items.common.ElvenDagger;
import com.nyrds.pixeldungeon.items.common.ElvenBow;
import com.watabou.pixeldungeon.items.weapon.missiles.Boomerang;
import com.watabou.pixeldungeon.items.weapon.missiles.Shuriken;
// Note: Shields are implemented in Lua, not as Java classes, so we'll skip shield implementation for now
import com.watabou.pixeldungeon.sprites.HeroSpriteDef;
import com.watabou.pixeldungeon.sprites.ItemSprite;
import com.watabou.pixeldungeon.sprites.MobSpriteDef;
import com.watabou.pixeldungeon.utils.GLog;

import java.util.List;
import java.util.Map;

/**
 * A utility class to generate sprite images for ALL mobs and items using the factory systems
 * This will attempt to generate sprites for all registered entities in the game
 */
public class FactorySpriteGenerator extends QuickModTest {

    @Override
    public void create() {
        super.create();

        // Create the sprites directory if it doesn't exist
        try {
            java.io.File spritesDir = new java.io.File("../../../../sprites/");
            if (!spritesDir.exists()) {
                spritesDir.mkdirs();
                GLog.i("Created sprites directory at: %s", spritesDir.getAbsolutePath());
            }
        } catch (Exception e) {
            GLog.w("Error creating sprites directory: %s", e.getMessage());
        }

        // Initialize the static handlers that are required for item creation
        // This mimics part of what happens in Dungeon.init()
        try {
            Wand.initWoods();
            Ring.initGems();
            Scroll.initLabels();
            Potion.initColors();
        } catch (Exception e) {
            GLog.w("Error initializing static handlers: %s", e.getMessage());
        }

        // Set texture to preserve bitmap data during sprite generation so we can extract actual sprite data
        SmartTexture.setAutoDisposeBitmapData(false);

        // Run the sprite generation after the game has been initialized
        generateAllMobsSpritesFromFactory();
        generateAllItemsSpritesFromFactory();
        generateAllSpellsSpritesFromFactory();
        generateAllBuffsIconsFromFactory();

        // Temporarily allow sprite creation for hero generation
        com.watabou.pixeldungeon.scenes.GameScene.setForceAllowSpriteCreation(true);
        generateAllHeroSprites();
        com.watabou.pixeldungeon.scenes.GameScene.setForceAllowSpriteCreation(false);

        // Reset to default behavior after generation
        SmartTexture.setAutoDisposeBitmapData(true);

        // Generate entity lists by kind
        generateEntityLists();

        // Exit the application after generating sprites
        System.exit(0);
    }

    private void generateAllMobsSpritesFromFactory() {
        GLog.i("Starting to generate sprites for all mobs from factory...");

        int successCount = 0;
        int errorCount = 0;

        // Use the proper public factory method instead of reflection
        java.util.List<Mob> mobs = MobFactory.allMobs();

        for(Mob mob : mobs) {
            try {
                MobSpriteDef mobSprite = (MobSpriteDef) mob.newSprite();
                if (mobSprite != null) {
                    // Use the avatar method to get the base sprite
                    com.watabou.noosa.Image avatar = mobSprite.avatar();
                    if (avatar != null) {
                        // Extract the actual bitmap data from the avatar
                        BitmapData bitmap = extractBitmapDataFromImage(avatar);
                        if (bitmap != null) {
                            String fileName = "../../../../sprites/mob_" + mob.getEntityKind() + ".png";
                            // Save the sprite image to a file
                            bitmap.savePng(fileName);
                            GLog.i("Saved mob sprite: %s", fileName);
                            successCount++;
                        } else {
                            GLog.w("Failed to extract BitmapData for mob: %s", mob.getEntityKind());
                        }
                    } else {
                        GLog.w("Avatar is null for mob: %s", mob.getEntityKind());
                    }
                } else {
                    GLog.w("MobSprite is null for mob: %s", mob.getEntityKind());
                }
            } catch (Exception e) {
                GLog.w("Error creating or saving mob sprite for %s: %s", mob.getEntityKind(), e.getMessage());
                errorCount++;
            }
        }

        GLog.i("Mob sprite generation completed. Success: %d, Errors: %d", successCount, errorCount);
    }

    private void generateAllItemsSpritesFromFactory() {
        GLog.i("Starting to generate sprites for all items from factory...");

        int successCount = 0;
        int errorCount = 0;

        // Use the proper public factory method instead of reflection
        java.util.List<Item> items = com.nyrds.pixeldungeon.items.common.ItemFactory.allItems();

        for(Item item : items) {
            try {

                    // Instead of creating ItemSprite and extracting from it (which might have effects),
                    // get the image data directly from the item properties
                    String imageFile = item.imageFile();
                    int imageIndex = item.image();

                    if (imageFile != null && imageIndex >= 0) {
                        // Get the source bitmap from the image file
                        BitmapData sourceBmp = com.nyrds.util.ModdingMode.getBitmapData(imageFile);

                        // Item sprites are typically 16x16 pixels based on ItemSprite.SIZE
                        final int SPRITE_SIZE = 16;

                        // Calculate the position in the texture atlas based on the image index
                        int texWidth = sourceBmp.getWidth();
                        int cols = texWidth / SPRITE_SIZE;

                        int frameX = (imageIndex % cols) * SPRITE_SIZE;
                        int frameY = (imageIndex / cols) * SPRITE_SIZE;

                        // Create BitmapData for the specific frame
                        BitmapData result = BitmapData.createBitmap(SPRITE_SIZE, SPRITE_SIZE);
                        if (result != null) {
                            result.eraseColor(0x00000000); // Clear with transparent color before rendering
                            result.copyRect(sourceBmp, frameX, frameY, SPRITE_SIZE, SPRITE_SIZE, 0, 0);
                            String fileName = "../../../../sprites/item_" + item.getEntityKind() + ".png";
                            result.savePng(fileName);
                            GLog.i("Saved item sprite: %s", fileName);
                            successCount++;
                        } else {
                            GLog.w("Failed to create result BitmapData for item: %s", item.getEntityKind());
                        }
                    }
            } catch (Exception e) {
                    GLog.w("Error creating or saving item sprite for %s: %s", item.getEntityKind(), e.getMessage());
                    errorCount++;
            }
        }

        GLog.i("Item sprite generation completed. Success: %d, Errors: %d", successCount, errorCount);
    }

    // Helper method to extract bitmap data from an image/sprite by accessing the source texture directly
    // This accesses the original texture atlas and extracts the specific frame region
    private BitmapData extractBitmapDataFromImage(com.watabou.noosa.Image image) {
        if (image == null || image.texture == null) {
            return null;
        }

        // Special handling for CompositeTextureImage to render all layers
        if (image instanceof com.watabou.noosa.CompositeTextureImage) {
            return extractBitmapDataFromCompositeImage((com.watabou.noosa.CompositeTextureImage) image);
        }

        // First try to get the bitmap data directly from the texture using the new method
        SmartTexture smartTexture = image.texture;
        BitmapData textureBitmap = smartTexture.getBitmapData();

        if (textureBitmap != null) {
            // Calculate the actual pixel coordinates in the texture
            com.nyrds.platform.compatibility.RectF frame = image.frame();
            int srcWidth = smartTexture.width;
            int srcHeight = smartTexture.height;

            int x = (int) (frame.left * srcWidth);
            int y = (int) (frame.top * srcHeight);
            int width = (int) (frame.width() * srcWidth);
            int height = (int) (frame.height() * srcHeight);

            // Create a new BitmapData with the size of the sprite
            BitmapData result = BitmapData.createBitmap(width, height);
            if (result != null) {
                result.eraseColor(0x00000000); // Clear with transparent color before rendering
                // Copy the relevant portion of the source texture to the result bitmap
                result.copyRect(textureBitmap, x, y, width, height, 0, 0);
                return result;
            }
        }

        return null;
    }

    // Special method to extract bitmap data from CompositeTextureImage which has multiple texture layers
    private BitmapData extractBitmapDataFromCompositeImage(com.watabou.noosa.CompositeTextureImage image) {
        if (image == null) {
            return null;
        }

        // Get the base texture
        SmartTexture smartTexture = image.texture;
        BitmapData baseBitmap = smartTexture.getBitmapData();

        if (baseBitmap == null) {
            return null;
        }

        // Calculate the actual pixel coordinates in the texture
        com.nyrds.platform.compatibility.RectF frame = image.frame();
        int srcWidth = smartTexture.width;
        int srcHeight = smartTexture.height;

        int x = (int) (frame.left * srcWidth);
        int y = (int) (frame.top * srcHeight);
        int width = (int) (frame.width() * srcWidth);
        int height = (int) (frame.height() * srcHeight);

        // Create a new BitmapData with the size of the sprite
        BitmapData result = BitmapData.createBitmap(width, height);
        if (result == null) {
            return null;
        }

        result.eraseColor(0x00000000); // Clear with transparent color

        // Copy the base texture
        if (baseBitmap != null) {
            result.copyRect(baseBitmap, x, y, width, height, 0, 0);
        }

        // Now composite each additional layer on top
        // This requires reflection to access the private mLayers field in CompositeTextureImage
        try {
            java.lang.reflect.Field layersField = com.watabou.noosa.CompositeTextureImage.class.getDeclaredField("mLayers");
            layersField.setAccessible(true);
            java.util.ArrayList<com.nyrds.platform.gl.Texture> layers =
                (java.util.ArrayList<com.nyrds.platform.gl.Texture>) layersField.get(image);

            if (layers != null) {
                for (int i = 0; i < layers.size(); i++) {
                    com.nyrds.platform.gl.Texture layer = layers.get(i);
                    com.watabou.gltextures.SmartTexture layerSmartTexture = (com.watabou.gltextures.SmartTexture) layer;
                    BitmapData layerBitmap = layerSmartTexture.getBitmapData();

                    if (layerBitmap != null) {
                        // Copy this layer onto the result bitmap
                        result.copyRect(layerBitmap, x, y, width, height, 0, 0);
                    }
                }
            }
        } catch (Exception e) {
            GLog.w("Error extracting layers from CompositeTextureImage: %s", e.getMessage());
            // If reflection fails, just return the base bitmap
        }

        return result;
    }

    private void generateAllSpellsSpritesFromFactory() {
        GLog.i("Starting to generate sprites for all spells from factory...");

        int successCount = 0;
        int errorCount = 0;

        // Get all registered spell names from the SpellFactory
        java.util.List<String> spellNames = (java.util.List<String>) com.nyrds.pixeldungeon.mechanics.spells.SpellFactory.getAllSpells();

        for(String spellName : spellNames) {
            try {
                // Create an instance of the spell
                com.nyrds.pixeldungeon.mechanics.spells.Spell spell = com.nyrds.pixeldungeon.mechanics.spells.SpellFactory.getSpellByName(spellName);

                if (spell != null) {
                    // Use the spell's itemForSlot to access the image data
                    com.nyrds.pixeldungeon.mechanics.spells.Spell.SpellItem spellItem = spell.itemForSlot();

                    if (spellItem != null) {
                        // Get the spell's image data from the spell item
                        String imageFile = spellItem.imageFile();
                        int imageIndex = spellItem.image();

                        if (imageFile != null && imageIndex >= 0) {
                            // Get the source bitmap from the image file
                            BitmapData sourceBmp = com.nyrds.util.ModdingMode.getBitmapData(imageFile);

                            if (sourceBmp != null) {
                                // Spell sprites are typically 16x16 pixels
                                final int SPRITE_SIZE = 16;

                                // Calculate the position in the texture atlas based on the image index
                                int texWidth = sourceBmp.getWidth();
                                int cols = texWidth / SPRITE_SIZE;

                                int frameX = (imageIndex % cols) * SPRITE_SIZE;
                                int frameY = (imageIndex / cols) * SPRITE_SIZE;

                                // Create BitmapData for the specific frame
                                BitmapData result = BitmapData.createBitmap(SPRITE_SIZE, SPRITE_SIZE);
                                if (result != null) {
                                    result.eraseColor(0x00000000); // Clear with transparent color before rendering
                                    result.copyRect(sourceBmp, frameX, frameY, SPRITE_SIZE, SPRITE_SIZE, 0, 0);
                                    String fileName = "../../../../sprites/spell_" + spellName + ".png";
                                    result.savePng(fileName);
                                    GLog.i("Saved spell sprite: %s", fileName);
                                    successCount++;
                                } else {
                                    GLog.w("Failed to create result BitmapData for spell: %s", spellName);
                                }
                            } else {
                                GLog.w("Failed to load source bitmap for spell: %s from file: %s", spellName, imageFile);
                            }
                        } else {
                            GLog.w("Spell has null image file or negative image index: %s", spellName);
                        }
                    } else {
                        GLog.w("Spell item is null for spell: %s", spellName);
                    }
                } else {
                    GLog.w("Spell is null for name: %s", spellName);
                }
            } catch (Exception e) {
                GLog.w("Error creating or saving spell sprite for %s: %s", spellName, e.getMessage());
                errorCount++;
            }
        }

        GLog.i("Spell sprite generation completed. Success: %d, Errors: %d", successCount, errorCount);
    }

    private void generateAllBuffsIconsFromFactory() {
        GLog.i("Starting to generate icons for all buffs from factory...");

        int successCount = 0;
        int errorCount = 0;

        // Get all registered buff names from the BuffFactory
        java.util.Set<String> buffNames = com.nyrds.pixeldungeon.mechanics.buffs.BuffFactory.getAllBuffsNames();

        for(String buffName : buffNames) {
            try {
                // Create an instance of the buff
                com.watabou.pixeldungeon.actors.buffs.Buff buff = com.nyrds.pixeldungeon.mechanics.buffs.BuffFactory.getBuffByName(buffName);

                if (buff != null) {
                    // Get the buff's small icon
                    com.watabou.noosa.Image icon = buff.smallIcon();

                    if (icon != null) {
                        // Extract the actual bitmap data from the icon
                        BitmapData bitmap = extractBitmapDataFromImage(icon);
                        if (bitmap != null) {
                            String fileName = "../../../../sprites/buff_" + buffName + ".png";
                            // Save the icon image to a file
                            bitmap.savePng(fileName);
                            GLog.i("Saved buff icon: %s", fileName);
                            successCount++;
                        } else {
                            GLog.w("Failed to extract BitmapData for buff icon: %s", buffName);
                        }
                    } else {
                        GLog.w("Small icon is null for buff: %s", buffName);
                    }
                } else {
                    GLog.w("Buff is null for name: %s", buffName);
                }
            } catch (Exception e) {
                GLog.w("Error creating or saving buff icon for %s: %s", buffName, e.getMessage());
                errorCount++;
            }
        }

        GLog.i("Buff icon generation completed. Success: %d, Errors: %d", successCount, errorCount);
    }

    // Helper method to generate a deterministic color based on the entity name
    private int getDeterministicColor(String entityName) {
        // Create a hash-based color for each entity
        int hash = entityName.hashCode();
        int r = (hash & 0xFF);
        int g = ((hash >> 8) & 0xFF);
        int b = ((hash >> 16) & 0xFF);
        int a = 255; // Fully opaque

        // Convert ARGB to the platform-specific format (RGBA) for BitmapData
        return BitmapData.color((a << 24) | (r << 16) | (g << 8) | b);
    }

    private void generateAllHeroSprites() {
        GLog.i("Starting to generate sprites for all hero classes and subclasses...");

        // Initialize item validation by getting all valid items
        java.util.Set<String> validItemNames = new java.util.HashSet<>();
        try {
            java.util.List<com.watabou.pixeldungeon.items.Item> allItems = com.nyrds.pixeldungeon.items.common.ItemFactory.allItems();
            for (com.watabou.pixeldungeon.items.Item item : allItems) {
                validItemNames.add(item.getEntityKind());
            }
        } catch (Exception e) {
            GLog.w("Error getting list of valid items: %s", e.getMessage());
        }

        int successCount = 0;
        int errorCount = 0;

        // Generate sprites for all hero classes
        for (HeroClass heroClass : HeroClass.values()) {
            if (heroClass == HeroClass.NONE) continue; // Skip the NONE class as it's not a real hero class

            try {
                // Create a temporary hero instance with this class
                Hero hero = new Hero(2); // Using difficulty 2 which is normal
                hero.setHeroClass(heroClass);

                // Equip the appropriate class armor for this class
                com.watabou.pixeldungeon.items.armor.ClassArmor classArmor = null;
                switch (heroClass) {
                    case WARRIOR:
                        classArmor = new com.watabou.pixeldungeon.items.armor.WarriorArmor();
                        break;
                    case MAGE:
                        classArmor = new com.watabou.pixeldungeon.items.armor.MageArmor();
                        break;
                    case ROGUE:
                        classArmor = new com.watabou.pixeldungeon.items.armor.RogueArmor();
                        break;
                    case HUNTRESS:
                        classArmor = new com.watabou.pixeldungeon.items.armor.HuntressArmor();
                        break;
                    case ELF:
                        classArmor = new com.watabou.pixeldungeon.items.armor.ElfArmor();
                        break;
                    case NECROMANCER:
                        classArmor = new com.nyrds.pixeldungeon.items.common.armor.NecromancerArmor();
                        break;
                    case GNOLL:
                        classArmor = new com.watabou.pixeldungeon.items.armor.GnollArmor();
                        break;
                }

                if (classArmor != null) {
                    // Equip the class armor
                    try {
                        classArmor.upgrade(0); // Ensure it's at +0 to avoid any upgrade visuals
                        classArmor.doEquip(hero);
                        GLog.i("Class armor equipped for hero class: %s", heroClass.name());
                    } catch (Exception e) {
                        GLog.w("Failed to equip class armor for hero class %s: %s", heroClass.name(), e.getMessage());
                    }
                }

                // Equip a basic weapon based on class archetype
                com.watabou.pixeldungeon.items.Item basicItem = null;
                switch (heroClass) {
                    case WARRIOR:
                        // Warriors get a basic sword
                        if (validItemNames.contains("ShortSword")) {
                            basicItem = com.nyrds.pixeldungeon.items.common.ItemFactory.itemByName("ShortSword");
                        } else if (validItemNames.contains("Dagger")) {
                            basicItem = com.nyrds.pixeldungeon.items.common.ItemFactory.itemByName("Dagger");
                        }
                        break;
                    case MAGE:
                        // Mages get a basic wand
                        if (validItemNames.contains("WandOfMagicMissile")) {
                            basicItem = com.nyrds.pixeldungeon.items.common.ItemFactory.itemByName("WandOfMagicMissile");
                        } else if (validItemNames.contains("WandOfFirebolt")) {
                            basicItem = com.nyrds.pixeldungeon.items.common.ItemFactory.itemByName("WandOfFirebolt");
                        }
                        break;
                    case ROGUE:
                        // Rogues get a basic dagger
                        if (validItemNames.contains("Dagger")) {
                            basicItem = com.nyrds.pixeldungeon.items.common.ItemFactory.itemByName("Dagger");
                        } else if (validItemNames.contains("Shuriken")) {
                            basicItem = com.nyrds.pixeldungeon.items.common.ItemFactory.itemByName("Shuriken");
                        }
                        break;
                    case HUNTRESS:
                        // Huntresses get a basic ranged weapon
                        if (validItemNames.contains("Dart")) {
                            basicItem = com.nyrds.pixeldungeon.items.common.ItemFactory.itemByName("Dart");
                        } else if (validItemNames.contains("Shuriken")) {
                            basicItem = com.nyrds.pixeldungeon.items.common.ItemFactory.itemByName("Shuriken");
                        }
                        break;
                    case ELF:
                        // Elves get a versatile weapon
                        if (validItemNames.contains("ElvenDagger")) {
                            basicItem = com.nyrds.pixeldungeon.items.common.ItemFactory.itemByName("ElvenDagger");
                        } else if (validItemNames.contains("Quarterstaff")) {
                            basicItem = com.nyrds.pixeldungeon.items.common.ItemFactory.itemByName("Quarterstaff");
                        }
                        break;
                    case NECROMANCER:
                        // Necromancers get an appropriate weapon
                        if (validItemNames.contains("Knuckles")) {
                            basicItem = com.nyrds.pixeldungeon.items.common.ItemFactory.itemByName("Knuckles");
                        } else if (validItemNames.contains("Mace")) {
                            basicItem = com.nyrds.pixeldungeon.items.common.ItemFactory.itemByName("Mace");
                        }
                        break;
                    case GNOLL:
                        // Gnolls get an appropriate weapon
                        if (validItemNames.contains("ShortSword")) {
                            basicItem = com.nyrds.pixeldungeon.items.common.ItemFactory.itemByName("ShortSword");
                        } else if (validItemNames.contains("Dagger")) {
                            basicItem = com.nyrds.pixeldungeon.items.common.ItemFactory.itemByName("Dagger");
                        }
                        break;
                    case PRIEST:
                    case DOCTOR:
                        // Generic melee weapon for other classes
                        if (validItemNames.contains("Quarterstaff")) {
                            basicItem = com.nyrds.pixeldungeon.items.common.ItemFactory.itemByName("Quarterstaff");
                        } else if (validItemNames.contains("Knuckles")) {
                            basicItem = com.nyrds.pixeldungeon.items.common.ItemFactory.itemByName("Knuckles");
                        }
                        break;
                }

                if (basicItem != null && basicItem instanceof com.watabou.pixeldungeon.items.EquipableItem) {
                    try {
                        com.watabou.pixeldungeon.items.EquipableItem equipableItem = (com.watabou.pixeldungeon.items.EquipableItem) basicItem;
                        equipableItem.upgrade(0); // Ensure it's at +0 to avoid any upgrade visuals
                        equipableItem.doEquip(hero);
                        GLog.i("Basic item equipped for hero class: %s", heroClass.name());
                    } catch (Exception e) {
                        GLog.w("Failed to equip basic item for hero class %s: %s", heroClass.name(), e.getMessage());
                    }
                }

                // Generate sprite for this hero class
                HeroSpriteDef heroSprite = (HeroSpriteDef) hero.newSprite();
                if (heroSprite != null) {
                    // Update the sprite to ensure all layers are properly applied after equipping items
                    try {
                        heroSprite.heroUpdated(hero);
                    } catch (Exception e) {
                        GLog.w("Failed to update HeroSprite for hero class %s: %s", heroClass.name(), e.getMessage());
                    }

                    // Use the avatar method to get the layered sprite
                    com.watabou.noosa.Image avatar = heroSprite.avatar();
                    if (avatar != null) {
                        // Extract the actual bitmap data from the avatar
                        BitmapData bitmap = extractBitmapDataFromImage(avatar);
                        if (bitmap != null) {
                            String fileName = "../../../../sprites/hero_" + heroClass.name() + ".png";
                            // Save the sprite image to a file
                            bitmap.savePng(fileName);
                            GLog.i("Saved hero class sprite: %s", fileName);
                            successCount++;
                        } else {
                            GLog.w("Failed to extract BitmapData for hero class: %s", heroClass.name());
                        }
                    } else {
                        GLog.w("Avatar is null for hero class: %s", heroClass.name());
                    }
                } else {
                    GLog.w("HeroSprite is null for hero class: %s", heroClass.name());
                }
            } catch (Exception e) {
                GLog.w("Error creating or saving hero class sprite for %s: %s", heroClass.name(), e.getMessage());
                errorCount++;
            }
        }


        // Also generate combinations of hero classes and subclasses
        // Only for valid class|subclass combinations as defined in WndClass.java
        for (HeroClass heroClass : HeroClass.values()) {
            if (heroClass == HeroClass.NONE) continue;

            for (HeroSubClass heroSubClass : HeroSubClass.values()) {
                if (heroSubClass == HeroSubClass.NONE) continue;

                // Check if this is a valid class-subclass combination
                boolean isValidCombination = false;
                switch (heroClass) {
                    case WARRIOR:
                        isValidCombination = (heroSubClass == HeroSubClass.GLADIATOR || heroSubClass == HeroSubClass.BERSERKER);
                        break;
                    case MAGE:
                        isValidCombination = (heroSubClass == HeroSubClass.BATTLEMAGE || heroSubClass == HeroSubClass.WARLOCK);
                        break;
                    case ROGUE:
                        isValidCombination = (heroSubClass == HeroSubClass.FREERUNNER || heroSubClass == HeroSubClass.ASSASSIN);
                        break;
                    case HUNTRESS:
                        isValidCombination = (heroSubClass == HeroSubClass.SNIPER || heroSubClass == HeroSubClass.WARDEN);
                        break;
                    case ELF:
                        isValidCombination = (heroSubClass == HeroSubClass.SCOUT || heroSubClass == HeroSubClass.SHAMAN);
                        break;
                    case NECROMANCER:
                        isValidCombination = (heroSubClass == HeroSubClass.LICH);
                        break;
                    case GNOLL:
                        isValidCombination = (heroSubClass == HeroSubClass.GUARDIAN || heroSubClass == HeroSubClass.WITCHDOCTOR);
                        break;
                    case PRIEST:
                    case DOCTOR:
                        // These classes currently have no subclasses
                        isValidCombination = false;
                        break;
                }

                if (isValidCombination) {
                    try {
                        GLog.i("Attempting to create hero class+subclass combination: %s+%s", heroClass.name(), heroSubClass.name());

                        // Create a temporary hero instance with this class and subclass combination
                        Hero hero = new Hero(2); // Using difficulty 2 which is normal
                        GLog.i("Hero instance created successfully for: %s+%s", heroClass.name(), heroSubClass.name());

                        hero.setHeroClass(heroClass);
                        GLog.i("Hero class set to: %s", heroClass.name());

                        hero.setSubClass(heroSubClass);
                        GLog.i("Hero subclass set to: %s", heroSubClass.name());

                        // Equip the appropriate class armor for this combination
                        ClassArmor classArmor = null;
                        switch (heroClass) {
                            case WARRIOR:
                                if (heroSubClass == HeroSubClass.GLADIATOR || heroSubClass == HeroSubClass.BERSERKER) {
                                    classArmor = new com.watabou.pixeldungeon.items.armor.WarriorArmor();
                                    GLog.i("Equipping WarriorArmor for %s+%s", heroClass.name(), heroSubClass.name());
                                }
                                break;
                            case MAGE:
                                if (heroSubClass == HeroSubClass.BATTLEMAGE || heroSubClass == HeroSubClass.WARLOCK) {
                                    classArmor = new com.watabou.pixeldungeon.items.armor.MageArmor();
                                    GLog.i("Equipping MageArmor for %s+%s", heroClass.name(), heroSubClass.name());
                                }
                                break;
                            case ROGUE:
                                if (heroSubClass == HeroSubClass.FREERUNNER || heroSubClass == HeroSubClass.ASSASSIN) {
                                    classArmor = new com.watabou.pixeldungeon.items.armor.RogueArmor();
                                    GLog.i("Equipping RogueArmor for %s+%s", heroClass.name(), heroSubClass.name());
                                }
                                break;
                            case HUNTRESS:
                                if (heroSubClass == HeroSubClass.SNIPER || heroSubClass == HeroSubClass.WARDEN) {
                                    classArmor = new com.watabou.pixeldungeon.items.armor.HuntressArmor();
                                    GLog.i("Equipping HuntressArmor for %s+%s", heroClass.name(), heroSubClass.name());
                                }
                                break;
                            case ELF:
                                if (heroSubClass == HeroSubClass.SCOUT || heroSubClass == HeroSubClass.SHAMAN) {
                                    classArmor = new com.watabou.pixeldungeon.items.armor.ElfArmor();
                                    GLog.i("Equipping ElfArmor for %s+%s", heroClass.name(), heroSubClass.name());
                                }
                                break;
                            case NECROMANCER:
                                if (heroSubClass == HeroSubClass.LICH) {
                                    classArmor = new com.nyrds.pixeldungeon.items.common.armor.NecromancerArmor();
                                    GLog.i("Equipping NecromancerArmor for %s+%s", heroClass.name(), heroSubClass.name());
                                }
                                break;
                            case GNOLL:
                                if (heroSubClass == HeroSubClass.GUARDIAN || heroSubClass == HeroSubClass.WITCHDOCTOR) {
                                    classArmor = new com.watabou.pixeldungeon.items.armor.GnollArmor();
                                    GLog.i("Equipping GnollArmor for %s+%s", heroClass.name(), heroSubClass.name());
                                }
                                break;
                        }

                        if (classArmor != null) {
                            GLog.i("Class armor found for combination: %s+%s", heroClass.name(), heroSubClass.name());
                            // Equip the class armor - wrap in try-catch to prevent issues during sprite generation
                            try {
                                classArmor.upgrade(0); // Ensure it's at +0 to avoid any upgrade visuals
                                classArmor.doEquip(hero);
                                GLog.i("Class armor equipped successfully for: %s+%s", heroClass.name(), heroSubClass.name());
                            } catch (Exception e) {
                                GLog.w("Failed to equip class armor for %s+%s: %s", heroClass.name(), heroSubClass.name(), e.getMessage());
                                // Continue without armor if equipping fails
                            }
                        } else {
                            GLog.w("No class armor found for combination: %s+%s", heroClass.name(), heroSubClass.name());
                        }

                        // Equip random weapon and/or shield
                        // Use a seed based on class+subclass combination for consistent random items per combination
                        long seed = (heroClass.name() + "_" + heroSubClass.name()).hashCode();
                        java.util.Random random = new java.util.Random(seed);
                        GLog.i("Generated seed %d for random item selection for: %s+%s", seed, heroClass.name(), heroSubClass.name());

                        // Add a random weapon based on class archetype using ItemFactory
                        com.watabou.pixeldungeon.items.Item randomItem = null;
                        switch (heroClass) {
                            case WARRIOR:
                                // Warriors get melee weapons
                                String[] warriorItems = {
                                    "ShortSword",
                                    "Longsword",
                                    "Mace",
                                    "Dagger",
                                    "Glaive",
                                    "Spear",
                                    "WarHammer"
                                };
                                GLog.i("Checking warrior items: %s for %s+%s", java.util.Arrays.toString(warriorItems), heroClass.name(), heroSubClass.name());
                                // Find a valid item
                                for (String item : warriorItems) {
                                    if (validItemNames.contains(item)) {
                                        randomItem = com.nyrds.pixeldungeon.items.common.ItemFactory.itemByName(item);
                                        GLog.i("Found valid warrior item: %s for %s+%s", item, heroClass.name(), heroSubClass.name());
                                        break;
                                    } else {
                                        GLog.i("Item %s not found in validItemNames for %s+%s", item, heroClass.name(), heroSubClass.name());
                                    }
                                }
                                if (randomItem == null) {
                                    // If none of the preferred items are found, pick any valid weapon
                                    String randomItemName = warriorItems[random.nextInt(warriorItems.length)];
                                    randomItem = com.nyrds.pixeldungeon.items.common.ItemFactory.itemByName(randomItemName);
                                    GLog.i("Selected random warrior item: %s for %s+%s", randomItemName, heroClass.name(), heroSubClass.name());
                                }
                                break;
                            case GNOLL:
                                // For Witch Doctor subclass, use wands since they focus on magical abilities
                                if (heroSubClass == HeroSubClass.WITCHDOCTOR) {
                                    String[] wandItems = {
                                        "WandOfDisintegration",
                                        "WandOfFirebolt",
                                        "WandOfLightning",
                                        "WandOfMagicMissile",
                                        "WandOfRegrowth",
                                        "WandOfSlowness",
                                        "WandOfTeleportation",
                                        "WandOfPoison"
                                    };
                                    GLog.i("Checking Witch Doctor wand items: %s for %s+%s", java.util.Arrays.toString(wandItems), heroClass.name(), heroSubClass.name());
                                    // Find a valid wand
                                    for (String item : wandItems) {
                                        if (validItemNames.contains(item)) {
                                            randomItem = com.nyrds.pixeldungeon.items.common.ItemFactory.itemByName(item);
                                            GLog.i("Found valid wand item: %s for %s+%s", item, heroClass.name(), heroSubClass.name());
                                            break;
                                        } else {
                                            GLog.i("Wand item %s not found in validItemNames for %s+%s", item, heroClass.name(), heroSubClass.name());
                                        }
                                    }
                                    if (randomItem == null) {
                                        // If none of the preferred items are found, pick any valid wand
                                        String randomItemName = wandItems[random.nextInt(wandItems.length)];
                                        randomItem = com.nyrds.pixeldungeon.items.common.ItemFactory.itemByName(randomItemName);
                                        GLog.i("Selected random wand item: %s for %s+%s", randomItemName, heroClass.name(), heroSubClass.name());
                                    }
                                } else {
                                    // For Guardian subclass and any other future Gnoll subclasses, use melee weapons
                                    // with specific handling for Guardian's shield-based gameplay
                                    String[] gnollItems = {
                                        "ShortSword",
                                        "Longsword",
                                        "Mace",
                                        "Dagger",
                                        "Glaive",
                                        "Spear",
                                        "WarHammer",
                                        "BattleAxe"
                                    };
                                    GLog.i("Checking Gnoll items: %s for %s+%s", java.util.Arrays.toString(gnollItems), heroClass.name(), heroSubClass.name());

                                    // Find a valid item
                                    for (String item : gnollItems) {
                                        if (validItemNames.contains(item)) {
                                            randomItem = com.nyrds.pixeldungeon.items.common.ItemFactory.itemByName(item);
                                            GLog.i("Found valid gnoll item: %s for %s+%s", item, heroClass.name(), heroSubClass.name());
                                            break;
                                        } else {
                                            GLog.i("Gnoll item %s not found in validItemNames for %s+%s", item, heroClass.name(), heroSubClass.name());
                                        }
                                    }
                                    if (randomItem == null) {
                                        // If none of the preferred items are found, pick any valid item
                                        String randomItemName = gnollItems[random.nextInt(gnollItems.length)];
                                        randomItem = com.nyrds.pixeldungeon.items.common.ItemFactory.itemByName(randomItemName);
                                        GLog.i("Selected random gnoll item: %s for %s+%s", randomItemName, heroClass.name(), heroSubClass.name());
                                    }
                                }
                                break;
                            case MAGE:
                                // Mages get wands
                                String[] mageWands = {
                                    "WandOfDisintegration",
                                    "WandOfFirebolt",
                                    "WandOfLightning",
                                    "WandOfMagicMissile",
                                    "WandOfRegrowth",
                                    "WandOfSlowness",
                                    "WandOfTeleportation",
                                    "WandOfPoison"
                                };
                                GLog.i("Checking mage wand items: %s for %s+%s", java.util.Arrays.toString(mageWands), heroClass.name(), heroSubClass.name());
                                // Find a valid wand
                                for (String item : mageWands) {
                                    if (validItemNames.contains(item)) {
                                        randomItem = com.nyrds.pixeldungeon.items.common.ItemFactory.itemByName(item);
                                        GLog.i("Found valid mage wand: %s for %s+%s", item, heroClass.name(), heroSubClass.name());
                                        break;
                                    } else {
                                        GLog.i("Mage wand %s not found in validItemNames for %s+%s", item, heroClass.name(), heroSubClass.name());
                                    }
                                }
                                if (randomItem == null) {
                                    // If none of the preferred items are found, pick any valid wand
                                    String randomItemName = mageWands[random.nextInt(mageWands.length)];
                                    randomItem = com.nyrds.pixeldungeon.items.common.ItemFactory.itemByName(randomItemName);
                                    GLog.i("Selected random mage wand: %s for %s+%s", randomItemName, heroClass.name(), heroSubClass.name());
                                }
                                break;
                            case ROGUE:
                            case HUNTRESS:
                                // Rogues and Huntresses get ranged or light melee
                                String[] rogueHuntItems = {
                                    "Dagger",
                                    "Quarterstaff",
                                    "Boomerang",
                                    "Shuriken"
                                };
                                GLog.i("Checking rogue/huntress items: %s for %s+%s", java.util.Arrays.toString(rogueHuntItems), heroClass.name(), heroSubClass.name());
                                // Find a valid item
                                for (String item : rogueHuntItems) {
                                    if (validItemNames.contains(item)) {
                                        randomItem = com.nyrds.pixeldungeon.items.common.ItemFactory.itemByName(item);
                                        GLog.i("Found valid rogue/huntress item: %s for %s+%s", item, heroClass.name(), heroSubClass.name());
                                        break;
                                    } else {
                                        GLog.i("Rogue/huntress item %s not found in validItemNames for %s+%s", item, heroClass.name(), heroSubClass.name());
                                    }
                                }
                                if (randomItem == null) {
                                    // If none of the preferred items are found, pick any valid item
                                    String randomItemName = rogueHuntItems[random.nextInt(rogueHuntItems.length)];
                                    randomItem = com.nyrds.pixeldungeon.items.common.ItemFactory.itemByName(randomItemName);
                                    GLog.i("Selected random rogue/huntress item: %s for %s+%s", randomItemName, heroClass.name(), heroSubClass.name());
                                }
                                break;
                            case ELF:
                                // Elves get versatile weapons
                                String[] elfItems = {
                                    "ElvenDagger",
                                    "ElvenBow",
                                    "Quarterstaff"
                                };
                                GLog.i("Checking elf items: %s for %s+%s", java.util.Arrays.toString(elfItems), heroClass.name(), heroSubClass.name());
                                // Find a valid item
                                for (String item : elfItems) {
                                    if (validItemNames.contains(item)) {
                                        randomItem = com.nyrds.pixeldungeon.items.common.ItemFactory.itemByName(item);
                                        GLog.i("Found valid elf item: %s for %s+%s", item, heroClass.name(), heroSubClass.name());
                                        break;
                                    } else {
                                        GLog.i("Elf item %s not found in validItemNames for %s+%s", item, heroClass.name(), heroSubClass.name());
                                    }
                                }
                                if (randomItem == null) {
                                    // If none of the preferred items are found, pick any valid item
                                    String randomItemName = elfItems[random.nextInt(elfItems.length)];
                                    randomItem = com.nyrds.pixeldungeon.items.common.ItemFactory.itemByName(randomItemName);
                                    GLog.i("Selected random elf item: %s for %s+%s", randomItemName, heroClass.name(), heroSubClass.name());
                                }
                                break;
                            case NECROMANCER:
                                // Necromancers get necromancy-themed items
                                String[] necroItems = {
                                    "Knuckles",
                                    "BattleAxe",
                                    "Sword",
                                    "Mace" // Appropriate for necromancy theme
                                };
                                GLog.i("Checking necromancer items: %s for %s+%s", java.util.Arrays.toString(necroItems), heroClass.name(), heroSubClass.name());
                                // Find a valid item
                                for (String item : necroItems) {
                                    if (validItemNames.contains(item)) {
                                        randomItem = com.nyrds.pixeldungeon.items.common.ItemFactory.itemByName(item);
                                        GLog.i("Found valid necromancer item: %s for %s+%s", item, heroClass.name(), heroSubClass.name());
                                        break;
                                    } else {
                                        GLog.i("Necromancer item %s not found in validItemNames for %s+%s", item, heroClass.name(), heroSubClass.name());
                                    }
                                }
                                if (randomItem == null) {
                                    // If none of the preferred items are found, pick any valid item
                                    String randomItemName = necroItems[random.nextInt(necroItems.length)];
                                    randomItem = com.nyrds.pixeldungeon.items.common.ItemFactory.itemByName(randomItemName);
                                    GLog.i("Selected random necromancer item: %s for %s+%s", randomItemName, heroClass.name(), heroSubClass.name());
                                }
                                break;
                            case PRIEST:
                                // Priests get religious-themed items
                                String[] priestItems = {
                                    "Quarterstaff",
                                    "Mace",
                                    "Knuckles"
                                };
                                GLog.i("Checking priest items: %s for %s+%s", java.util.Arrays.toString(priestItems), heroClass.name(), heroSubClass.name());
                                // Find a valid item
                                for (String item : priestItems) {
                                    if (validItemNames.contains(item)) {
                                        randomItem = com.nyrds.pixeldungeon.items.common.ItemFactory.itemByName(item);
                                        GLog.i("Found valid priest item: %s for %s+%s", item, heroClass.name(), heroSubClass.name());
                                        break;
                                    } else {
                                        GLog.i("Priest item %s not found in validItemNames for %s+%s", item, heroClass.name(), heroSubClass.name());
                                    }
                                }
                                if (randomItem == null) {
                                    // If none of the preferred items are found, pick any valid item
                                    String randomItemName = priestItems[random.nextInt(priestItems.length)];
                                    randomItem = com.nyrds.pixeldungeon.items.common.ItemFactory.itemByName(randomItemName);
                                    GLog.i("Selected random priest item: %s for %s+%s", randomItemName, heroClass.name(), heroSubClass.name());
                                }
                                break;
                            case DOCTOR:
                                // Doctors get medical-themed items
                                String[] doctorItems = {
                                    "Quarterstaff",
                                    "Knuckles",
                                    "BoneSaw" // Using the actual doctor-specific weapon that exists in the game
                                };
                                GLog.i("Checking doctor items: %s for %s+%s", java.util.Arrays.toString(doctorItems), heroClass.name(), heroSubClass.name());
                                // Find a valid item
                                for (String item : doctorItems) {
                                    if (validItemNames.contains(item)) {
                                        randomItem = com.nyrds.pixeldungeon.items.common.ItemFactory.itemByName(item);
                                        GLog.i("Found valid doctor item: %s for %s+%s", item, heroClass.name(), heroSubClass.name());
                                        break;
                                    } else {
                                        GLog.i("Doctor item %s not found in validItemNames for %s+%s", item, heroClass.name(), heroSubClass.name());
                                    }
                                }
                                if (randomItem == null) {
                                    // If none of the preferred items are found, pick any valid item
                                    String randomItemName = doctorItems[random.nextInt(doctorItems.length)];
                                    randomItem = com.nyrds.pixeldungeon.items.common.ItemFactory.itemByName(randomItemName);
                                    GLog.i("Selected random doctor item: %s for %s+%s", randomItemName, heroClass.name(), heroSubClass.name());
                                }
                                break;
                            default:
                                // Other classes get generic melee
                                String[] genericItems = {
                                    "Dagger",
                                    "Quarterstaff",
                                    "Knuckles",
                                    "Shuriken"
                                };
                                GLog.i("Checking generic items: %s for %s+%s", java.util.Arrays.toString(genericItems), heroClass.name(), heroSubClass.name());
                                // Find a valid item
                                for (String item : genericItems) {
                                    if (validItemNames.contains(item)) {
                                        randomItem = com.nyrds.pixeldungeon.items.common.ItemFactory.itemByName(item);
                                        GLog.i("Found valid generic item: %s for %s+%s", item, heroClass.name(), heroSubClass.name());
                                        break;
                                    } else {
                                        GLog.i("Generic item %s not found in validItemNames for %s+%s", item, heroClass.name(), heroSubClass.name());
                                    }
                                }
                                if (randomItem == null) {
                                    // If none of the preferred items are found, pick any valid item
                                    String randomItemName = genericItems[random.nextInt(genericItems.length)];
                                    randomItem = com.nyrds.pixeldungeon.items.common.ItemFactory.itemByName(randomItemName);
                                    GLog.i("Selected random generic item: %s for %s+%s", randomItemName, heroClass.name(), heroSubClass.name());
                                }
                                break;
                        }

                        if (randomItem != null) {
                            GLog.i("Random item selected: %s for %s+%s", randomItem.getEntityKind(), heroClass.name(), heroSubClass.name());
                            if (randomItem instanceof com.watabou.pixeldungeon.items.EquipableItem) {
                                com.watabou.pixeldungeon.items.EquipableItem equipableItem = (com.watabou.pixeldungeon.items.EquipableItem) randomItem;
                                try {
                                    equipableItem.upgrade(0); // Ensure it's at +0 to avoid any upgrade visuals
                                    equipableItem.doEquip(hero);
                                    GLog.i("Random item equipped successfully for %s+%s", heroClass.name(), heroSubClass.name());
                                } catch (Exception e) {
                                    GLog.w("Failed to equip random item for %s+%s: %s", heroClass.name(), heroSubClass.name(), e.getMessage());
                                    // Continue without the random item if equipping fails
                                }
                            } else {
                                GLog.w("Random item is not equipable: %s for %s+%s", randomItem.getEntityKind(), heroClass.name(), heroSubClass.name());
                            }
                        } else {
                            GLog.w("No random item found for %s+%s", heroClass.name(), heroSubClass.name());
                        }

                        // For Guardian subclass, try to equip a shield if possible
                        // Since shields are implemented as Lua scripts, we'll note this in the comment
                        if (heroClass == HeroClass.GNOLL && heroSubClass == HeroSubClass.GUARDIAN) {
                            GLog.i("Processing Guardian subclass specific logic for %s+%s", heroClass.name(), heroSubClass.name());
                            // The Guardian subclass provides shield benefits based on equipped left-hand items
                            // This is handled automatically by the game mechanics when the hero is created
                        }

                        // Generate sprite for this hero class and subclass combination
                        GLog.i("Attempting to generate sprite for hero class+subclass: %s+%s", heroClass.name(), heroSubClass.name());
                        HeroSpriteDef heroSprite = (HeroSpriteDef) hero.newSprite();
                        if (heroSprite != null) {
                            GLog.i("HeroSprite created successfully for: %s+%s", heroClass.name(), heroSubClass.name());
                            // Update the sprite to ensure all layers are properly applied after equipping items
                            try {
                                heroSprite.heroUpdated(hero);
                                GLog.i("HeroSprite updated for: %s+%s", heroClass.name(), heroSubClass.name());
                            } catch (Exception e) {
                                GLog.w("Failed to update HeroSprite for %s+%s: %s", heroClass.name(), heroSubClass.name(), e.getMessage());
                            }

                            // Use the avatar method to get the layered sprite
                            com.watabou.noosa.Image avatar = heroSprite.avatar();
                            if (avatar != null) {
                                GLog.i("Avatar created successfully for: %s+%s", heroClass.name(), heroSubClass.name());
                                // Extract the actual bitmap data from the avatar
                                BitmapData bitmap = extractBitmapDataFromImage(avatar);
                                if (bitmap != null) {
                                    String fileName = "../../../../sprites/hero_" + heroClass.name() + "_" + heroSubClass.name() + ".png";
                                    // Save the sprite image to a file
                                    bitmap.savePng(fileName);
                                    GLog.i("Saved hero class+subclass sprite: %s", fileName);
                                    successCount++;
                                } else {
                                    GLog.w("Failed to extract BitmapData for hero class+subclass: %s+%s", heroClass.name(), heroSubClass.name());
                                }
                            } else {
                                GLog.w("Avatar is null for hero class+subclass: %s+%s", heroClass.name(), heroSubClass.name());
                            }
                        } else {
                            GLog.w("HeroSprite is null for hero class+subclass: %s+%s", heroClass.name(), heroSubClass.name());
                        }
                    } catch (Exception e) {
                        GLog.w("Error creating or saving hero class+subclass sprite for %s+%s: %s", heroClass.name(), heroSubClass.name(), e.getMessage());
                        e.printStackTrace(); // Add stack trace to see exact error
                        errorCount++;
                    }
                } else {
                    GLog.i("Skipping invalid hero class+subclass combination: %s+%s", heroClass.name(), heroSubClass.name());
                }
            }
        }

        GLog.i("Hero sprite generation completed. Success: %d, Errors: %d", successCount, errorCount);
    }

    private void generateEntityLists() {
        GLog.i("Starting to generate entity lists by kind...");

        // Create the entities directory if it doesn't exist
        try {
            java.io.File entitiesDir = new java.io.File("../../../../entities/");
            if (!entitiesDir.exists()) {
                entitiesDir.mkdirs();
                GLog.i("Created entities directory at: %s", entitiesDir.getAbsolutePath());
            }
        } catch (Exception e) {
            GLog.w("Error creating entities directory: %s", e.getMessage());
            return;
        }

        // Generate list of all mobs
        try {
            java.util.List<com.watabou.pixeldungeon.actors.mobs.Mob> mobs = com.nyrds.pixeldungeon.mobs.common.MobFactory.allMobs();
            java.util.Set<String> uniqueMobNames = new java.util.HashSet<>();
            for (com.watabou.pixeldungeon.actors.mobs.Mob mob : mobs) {
                uniqueMobNames.add(mob.getEntityKind());
            }
            java.util.List<String> mobNames = new java.util.ArrayList<>(uniqueMobNames);
            java.util.Collections.sort(mobNames);
            writeEntityListToFile(mobNames, "mobs.txt");
        } catch (Exception e) {
            GLog.w("Error generating mob list: %s", e.getMessage());
        }

        // Generate list of all items
        try {
            java.util.List<com.watabou.pixeldungeon.items.Item> items = com.nyrds.pixeldungeon.items.common.ItemFactory.allItems();
            java.util.Set<String> uniqueItemNames = new java.util.HashSet<>();
            for (com.watabou.pixeldungeon.items.Item item : items) {
                uniqueItemNames.add(item.getEntityKind());
            }
            java.util.List<String> itemNames = new java.util.ArrayList<>(uniqueItemNames);
            java.util.Collections.sort(itemNames);
            writeEntityListToFile(itemNames, "items.txt");
        } catch (Exception e) {
            GLog.w("Error generating item list: %s", e.getMessage());
        }

        // Generate list of all spells
        try {
            java.util.List<String> allSpellNames = (java.util.List<String>) com.nyrds.pixeldungeon.mechanics.spells.SpellFactory.getAllSpells();
            java.util.Set<String> uniqueSpellNames = new java.util.HashSet<>(allSpellNames);
            java.util.List<String> spellNames = new java.util.ArrayList<>(uniqueSpellNames);
            java.util.Collections.sort(spellNames);
            writeEntityListToFile(spellNames, "spells.txt");
        } catch (Exception e) {
            GLog.w("Error generating spell list: %s", e.getMessage());
        }

        // Generate list of all buffs
        try {
            java.util.Set<String> allBuffNames = com.nyrds.pixeldungeon.mechanics.buffs.BuffFactory.getAllBuffsNames();
            java.util.Set<String> uniqueBuffNames = new java.util.HashSet<>(allBuffNames);
            java.util.List<String> buffNames = new java.util.ArrayList<>(uniqueBuffNames);
            java.util.Collections.sort(buffNames);
            writeEntityListToFile(buffNames, "buffs.txt");
        } catch (Exception e) {
            GLog.w("Error generating buff list: %s", e.getMessage());
        }

        GLog.i("Entity list generation completed.");
    }

    private void writeEntityListToFile(java.util.List<String> entityNames, String fileName) {
        try {
            // Sort the list for better readability
            java.util.Collections.sort(entityNames);

            // Create entities directory if not exists
            java.io.File entitiesDir = new java.io.File("../../../../entities/");
            if (!entitiesDir.exists()) {
                entitiesDir.mkdirs();
            }

            // Write to file in the entities directory
            String filePath = "../../../../entities/" + fileName;
            java.io.FileWriter writer = new java.io.FileWriter(filePath);
            for (String entityName : entityNames) {
                writer.write(entityName + "\n");
            }
            writer.close();

            GLog.i("Saved entity list to: %s (%d entries)", filePath, entityNames.size());
        } catch (Exception e) {
            GLog.w("Error writing entity list to file %s: %s", fileName, e.getMessage());
        }
    }

    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration cfg = new Lwjgl3ApplicationConfiguration();
        cfg.setTitle("Remixed Pixel Dungeon - Factory Sprite Generator");
        cfg.setBackBufferConfig(8, 8, 8, 8, 16, 0, 0);
        cfg.setForegroundFPS(60);
        cfg.setWindowedMode(1, 1); // Minimal window size for headless operation
        cfg.setResizable(false);
        cfg.disableAudio(true); // Disable audio for faster startup

        new Lwjgl3Application(new FactorySpriteGenerator(), cfg);
    }
}