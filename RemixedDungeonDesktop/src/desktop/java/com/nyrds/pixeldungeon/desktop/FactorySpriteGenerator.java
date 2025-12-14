package com.nyrds.pixeldungeon.desktop;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.nyrds.platform.game.QuickModTest;
import com.nyrds.pixeldungeon.items.common.ItemFactory;
import com.nyrds.pixeldungeon.mobs.common.MobFactory;
import com.nyrds.platform.gfx.BitmapData;
import com.watabou.noosa.Image;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.sprites.ItemSprite;
import com.watabou.pixeldungeon.utils.GLog;

import java.util.List;

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
            com.watabou.pixeldungeon.items.wands.Wand.initWoods();
            com.watabou.pixeldungeon.items.rings.Ring.initGems();
            com.watabou.pixeldungeon.items.scrolls.Scroll.initLabels();
            com.watabou.pixeldungeon.items.potions.Potion.initColors();
        } catch (Exception e) {
            GLog.w("Error initializing static handlers: %s", e.getMessage());
        }

        // Run the sprite generation after the game has been initialized
        generateAllMobsSpritesFromFactory();
        generateAllItemsSpritesFromFactory();

        // Exit the application after generating sprites
        System.exit(0);
    }

    private void generateAllMobsSpritesFromFactory() {
        GLog.i("Starting to generate sprites for all mobs from factory...");

        int successCount = 0;
        int errorCount = 0;

        // Access the factory's internal map to get class names
        java.lang.reflect.Field mobsListField = null;
        try {
            mobsListField = com.nyrds.pixeldungeon.mobs.common.MobFactory.class.getDeclaredField("mMobsList");
            mobsListField.setAccessible(true);

            @SuppressWarnings("unchecked")
            java.util.Map<String, Class<? extends com.watabou.pixeldungeon.actors.mobs.Mob>> mobsMap =
                (java.util.Map<String, Class<? extends com.watabou.pixeldungeon.actors.mobs.Mob>>) mobsListField.get(null);

            for(String mobClass : mobsMap.keySet()) {
                try {
                    Class<? extends com.watabou.pixeldungeon.actors.mobs.Mob> clazz = mobsMap.get(mobClass);

                    // Skip CustomMob class as it's a wrapper for JSON-defined mobs
                    if (clazz == com.nyrds.pixeldungeon.mobs.common.CustomMob.class) {
                        continue;
                    }

                    // Try to create the mob using the factory's mobByName method inside a try-catch
                    com.watabou.pixeldungeon.actors.mobs.Mob mob =
                        com.nyrds.pixeldungeon.mobs.common.MobFactory.mobByName(mobClass);

                    // Get the mob's sprite avatar
                    com.watabou.noosa.Image avatar = mob.newSprite().avatar();
                    if (avatar != null) {
                        // Create a BitmapData object (16x16 pixels, standard sprite size)
                        // Using standard sprite size of 16x16 from ItemSprite.SIZE
                        BitmapData bitmap = new BitmapData(16, 16);
                        if (bitmap != null) {
                            // Fill the image with a unique color based on the mob name
                            int color = getDeterministicColor(mob.getEntityKind());
                            bitmap.clear(color);
                            String fileName = "../../../../sprites/mob_" + mob.getEntityKind() + ".png";
                            // Save the sprite image to a file
                            bitmap.savePng(fileName);
                            GLog.i("Saved mob sprite: %s", fileName);
                            successCount++;
                        } else {
                            GLog.w("Failed to create BitmapData for mob: %s", mob.getEntityKind());
                        }
                    } else {
                        GLog.w("Avatar is null for mob: %s", mob.getEntityKind());
                    }
                } catch (Exception e) {
                    GLog.w("Error creating or saving mob sprite for %s: %s", mobClass, e.getMessage());
                    errorCount++;
                }
            }
        } catch (Exception e) {
            GLog.w("Error accessing MobFactory internal map: %s", e.getMessage());
        }

        GLog.i("Mob sprite generation completed. Success: %d, Errors: %d", successCount, errorCount);
    }

    private void generateAllItemsSpritesFromFactory() {
        GLog.i("Starting to generate sprites for all items from factory...");

        int successCount = 0;
        int errorCount = 0;

        // Access the factory's internal map to get class names
        java.lang.reflect.Field itemsListField = null;
        try {
            itemsListField = com.nyrds.pixeldungeon.items.common.ItemFactory.class.getDeclaredField("mItemsList");
            itemsListField.setAccessible(true);

            @SuppressWarnings("unchecked")
            java.util.Map<String, Class<? extends com.watabou.pixeldungeon.items.Item>> itemsMap =
                (java.util.Map<String, Class<? extends com.watabou.pixeldungeon.items.Item>>) itemsListField.get(null);

            for(String itemClass : itemsMap.keySet()) {
                try {
                    Class<? extends com.watabou.pixeldungeon.items.Item> clazz = itemsMap.get(itemClass);

                    // Skip CustomItem class as it's a wrapper for Lua-defined items
                    if (clazz == com.nyrds.pixeldungeon.items.CustomItem.class) {
                        continue;
                    }

                    // Try to create the item using the factory's itemByName method inside a try-catch
                    com.watabou.pixeldungeon.items.Item item =
                        com.nyrds.pixeldungeon.items.common.ItemFactory.itemByName(itemClass);

                    com.watabou.pixeldungeon.sprites.ItemSprite itemSprite = new com.watabou.pixeldungeon.sprites.ItemSprite(item);
                    if (itemSprite != null) {
                        // Create BitmapData from the texture information
                        // We'll create a basic square image with a specific color for each item type
                        BitmapData bitmap = new BitmapData(16, 16); // Standard sprite size
                        if (bitmap != null) {
                            // Fill the image with a unique color based on the item name
                            int color = getDeterministicColor(item.getEntityKind());
                            bitmap.clear(color);
                            String fileName = "../../../../sprites/item_" + item.getEntityKind() + ".png";
                            // Save the sprite image to a file
                            bitmap.savePng(fileName);
                            GLog.i("Saved item sprite: %s", fileName);
                            successCount++;
                        } else {
                            GLog.w("Failed to create BitmapData for item: %s", item.getEntityKind());
                        }
                    } else {
                        GLog.w("ItemSprite is null for item: %s", item.getEntityKind());
                    }
                } catch (Exception e) {
                    GLog.w("Error creating or saving item sprite for %s: %s", itemClass, e.getMessage());
                    errorCount++;
                }
            }
        } catch (Exception e) {
            GLog.w("Error accessing ItemFactory internal map: %s", e.getMessage());
        }

        GLog.i("Item sprite generation completed. Success: %d, Errors: %d", successCount, errorCount);
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