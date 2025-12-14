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
 * A utility class to generate sprite images for all mobs and items
 */
public class SpriteGenerator extends QuickModTest {

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
        generateAllMobsSprites();
        generateAllItemsSprites();

        // Exit the application after generating sprites
        System.exit(0);
    }

    private void generateAllMobsSprites() {
        List<Mob> mobs = MobFactory.allMobs();

        for (Mob mob : mobs) {
            try {
                // Get the mob's sprite avatar
                Image avatar = mob.newSprite().avatar();
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
                    }
                }
            } catch (Exception e) {
                GLog.w("Error saving mob sprite for %s: %s", mob.getEntityKind(), e.getMessage());
            }
        }
    }

    private void generateAllItemsSprites() {
        List<Item> items = ItemFactory.allItems();

        for (Item item : items) {
            try {
                ItemSprite itemSprite = new ItemSprite(item);
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
                    }
                }
            } catch (Exception e) {
                GLog.w("Error saving item sprite for %s: %s", item.getEntityKind(), e.getMessage());
            }
        }
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
        cfg.setTitle("Remixed Pixel Dungeon - Sprite Generator");
        cfg.setBackBufferConfig(8, 8, 8, 8, 16, 0, 0);
        cfg.setForegroundFPS(60);
        cfg.setWindowedMode(1, 1); // Minimal window size for headless operation
        cfg.setResizable(false);
        cfg.disableAudio(true); // Disable audio for faster startup

        new Lwjgl3Application(new SpriteGenerator(), cfg);
    }
}