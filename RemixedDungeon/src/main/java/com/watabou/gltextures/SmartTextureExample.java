package com.watabou.gltextures;

import com.nyrds.platform.gfx.BitmapData;

/**
 * Example class showing usage of the new SmartTexture functionality with static control of bitmap disposal
 */
public class SmartTextureExample {

    /**
     * Example of creating textures without disposing bitmap data using static flag
     * This is useful when you need to reuse the same bitmap data for multiple operations
     */
    public static void createTextureWithoutDisposal() {
        // Change the global setting to preserve bitmap data
        SmartTexture.setAutoDisposeBitmapData(false);

        // Create a bitmap that you plan to reuse
        BitmapData reusableBitmap = BitmapData.createBitmap(16, 16);
        reusableBitmap.eraseColor(0x00000000); // Clear with transparent color before rendering
        reusableBitmap.clear(0xFF00FF00); // Green color

        // Create a SmartTexture - bitmap data will NOT be disposed after upload
        // This allows the bitmap to be reused elsewhere
        SmartTexture texture = new SmartTexture(reusableBitmap);

        // Verify that the global setting is now to preserve bitmap data
        System.out.println("Global setting: Bitmap data will NOT be disposed after texture upload - bitmap is preserved for reuse: " +
                          SmartTexture.getAutoDisposeBitmapData());

        // Clean up
        texture.delete();

        // Reset to default behavior
        SmartTexture.setAutoDisposeBitmapData(true);
    }

    /**
     * Example of changing the disposal behavior globally
     */
    public static void changeDisposalBehavior() {
        // Check initial behavior (default is to dispose)
        System.out.println("Initial disposal behavior: " + SmartTexture.getAutoDisposeBitmapData());

        // Change behavior to preserve bitmap data globally
        SmartTexture.setAutoDisposeBitmapData(false);

        System.out.println("Updated disposal behavior: " + SmartTexture.getAutoDisposeBitmapData());

        // Create texture - it will use the new global setting
        BitmapData bitmap = BitmapData.createBitmap(8, 8);
        bitmap.eraseColor(0x00000000); // Clear with transparent color before rendering
        bitmap.clear(0xFFFF0000); // Red color
        SmartTexture texture = new SmartTexture(bitmap);

        // All textures created will follow the global setting
        System.out.println("All textures now follow global setting: " + SmartTexture.getAutoDisposeBitmapData());

        // Clean up
        texture.delete();

        // Reset to default behavior
        SmartTexture.setAutoDisposeBitmapData(true);
    }

    /**
     * Example showing the global nature of the setting
     */
    public static void demonstrateGlobalSetting() {
        // Default is to dispose
        System.out.println("Default behavior: " + SmartTexture.getAutoDisposeBitmapData());

        // Change globally
        SmartTexture.setAutoDisposeBitmapData(false);

        // All new textures will follow this setting
        BitmapData bitmap1 = BitmapData.createBitmap(4, 4);
        bitmap1.eraseColor(0x00000000); // Clear with transparent color before rendering
        bitmap1.clear(0xFF0000FF); // Blue color
        SmartTexture texture1 = new SmartTexture(bitmap1);

        BitmapData bitmap2 = BitmapData.createBitmap(8, 8);
        bitmap2.eraseColor(0x00000000); // Clear with transparent color before rendering
        bitmap2.clear(0xFFFFFF00); // Yellow color
        SmartTexture texture2 = new SmartTexture(bitmap2);

        System.out.println("Both textures follow global setting: " + SmartTexture.getAutoDisposeBitmapData());

        // Clean up
        texture1.delete();
        texture2.delete();

        // Reset to default behavior
        SmartTexture.setAutoDisposeBitmapData(true);
    }
}