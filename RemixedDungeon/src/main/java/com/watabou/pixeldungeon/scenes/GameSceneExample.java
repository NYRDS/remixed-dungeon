package com.watabou.pixeldungeon.scenes;

import com.nyrds.platform.gfx.BitmapData;
import com.watabou.gltextures.SmartTexture;

/**
 * Example class showing usage of the GameScene force sprite creation functionality
 */
public class GameSceneExample {

    /**
     * Example of how to temporarily allow sprite creation in contexts where no game scene is active
     * This is useful for sprite generation utilities or other offline processing tasks
     */
    public static void exampleUsage() {
        // Enable sprite creation even when no scene is active
        GameScene.setForceAllowSpriteCreation(true);

        // Now sprite creation operations can proceed without "scene not ready" errors
        // For example, creating hero sprites for generation:
        // HeroSpriteDef heroSprite = (HeroSpriteDef) hero.newSprite();

        // Perform sprite generation operations here...

        // Always reset to default behavior after generation
        GameScene.setForceAllowSpriteCreation(false);
    }

    /**
     * Example of how the force allowance works with SmartTexture
     */
    public static void exampleWithSmartTexture() {
        // Change the global setting to preserve bitmap data during generation
        SmartTexture.setAutoDisposeBitmapData(false);

        // Enable sprite creation
        GameScene.setForceAllowSpriteCreation(true);

        // Create textures and sprites for generation
        // BitmapData sourceBmp = ...;
        // SmartTexture texture = new SmartTexture(sourceBmp);
        // Image avatar = heroSprite.avatar();
        // BitmapData result = extractBitmapDataFromImage(avatar);

        // Save the result bitmap as PNG
        // result.savePng("path/to/output.png");

        // Reset to normal behavior after generation
        GameScene.setForceAllowSpriteCreation(false);
        SmartTexture.setAutoDisposeBitmapData(true);
    }
}