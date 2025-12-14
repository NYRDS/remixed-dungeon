package com.nyrds.pixeldungeon.desktop;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.nyrds.platform.game.QuickModTest;
import com.nyrds.pixeldungeon.items.common.ItemFactory;
import com.nyrds.pixeldungeon.mobs.common.MobFactory;
import com.nyrds.platform.gfx.BitmapData;
import com.watabou.gltextures.SmartTexture;
import com.watabou.noosa.Image;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.potions.Potion;
import com.watabou.pixeldungeon.items.rings.Ring;
import com.watabou.pixeldungeon.items.scrolls.Scroll;
import com.watabou.pixeldungeon.items.wands.Wand;
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

        // Reset to default behavior after generation
        SmartTexture.setAutoDisposeBitmapData(true);

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