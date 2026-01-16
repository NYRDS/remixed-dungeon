package com.nyrds.pixeldungeon.windows;

import com.watabou.noosa.Image;
import com.watabou.gltextures.SmartTexture;
import com.nyrds.platform.gfx.BitmapData;
import com.nyrds.platform.compatibility.RectF;

/**
 * A procedurally generated transmutation circle that uses the selected recipe as a seed
 */
public class TransmutationCircle extends Image {

    private String currentRecipeSeed = "";
    private boolean needsUpdate = true;
    private static final int SIZE = 256; // 256x256 texture

    public TransmutationCircle() {
        super();
        // Initialize with a generated transmutation circle texture
        updateTexture();
        alpha(0.4f); // Make it semi-transparent
    }

    @Override
    public void update() {
        super.update();

        if (needsUpdate) {
            updateTexture();
            needsUpdate = false;
        }
    }

    public void setRecipeSeed(String recipeSeed) {
        if (!currentRecipeSeed.equals(recipeSeed)) {
            currentRecipeSeed = recipeSeed;
            needsUpdate = true;
        }
    }

    private void updateTexture() {
        // Generate a new texture based on the recipe seed
        SmartTexture tex = generateCircleTexture(currentRecipeSeed);
        texture(tex);

        // Set the image size to match the texture first
        setWidth(SIZE);
        setHeight(SIZE);

        // Use a frame that covers the entire texture
        // The frame format is (left, top, right, bottom) where right/bottom are exclusive
        frame(new RectF(0, 0, 1, 1));
    }

    private SmartTexture generateCircleTexture(String seed) {
        // Create a new texture with a transmutation circle
        int[] pixels = new int[SIZE * SIZE];
        int seedHash = seed.hashCode();

        // Use the seed hash to influence visual properties
        float hueOffset = (seedHash & 0xFF) / 255.0f;
        float saturation = 0.5f + ((seedHash >> 8) & 0xFF) / 512.0f; // 0.5 to 0.75 range
        float brightness = 0.6f + ((seedHash >> 16) & 0xFF) / 512.0f; // 0.6 to 0.85 range

        float centerX = SIZE / 2.0f;
        float centerY = SIZE / 2.0f;
        float maxRadius = SIZE / 2.0f;

        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                float dx = x - centerX;
                float dy = y - centerY;
                float distance = (float) Math.sqrt(dx * dx + dy * dy);

                int color = 0x00000000; // Transparent by default

                // Draw the main circle
                if (distance <= maxRadius) {
                    // Determine if this pixel is part of the transmutation circle
                    float normalizedDistance = distance / maxRadius;

                    // Draw concentric circles/rings
                    if (Math.abs(normalizedDistance - 0.8f) < 0.02f ||
                        Math.abs(normalizedDistance - 0.6f) < 0.02f ||
                        Math.abs(normalizedDistance - 0.4f) < 0.02f ||
                        Math.abs(normalizedDistance - 0.2f) < 0.02f) {
                        // Ring color based on seed
                        color = getColorFromHSB(hueOffset, saturation, brightness);
                    }
                    // Draw radial lines
                    else if (distance > maxRadius * 0.1 && distance < maxRadius * 0.9) {
                        double angle = Math.atan2(dy, dx);
                        double normalizedAngle = (angle + Math.PI) / (2 * Math.PI); // 0 to 1

                        // Create radial lines based on the seed
                        if (Math.abs(((normalizedAngle + hueOffset) * 16) % 1 - 0.5) < 0.1) {
                            color = getColorFromHSB((hueOffset + 0.3f) % 1.0f, saturation, brightness * 0.8f);
                        }
                        // Add some decorative elements
                        else if (Math.abs(((normalizedAngle + hueOffset) * 8) % 1 - 0.5) < 0.05 &&
                                 Math.abs(normalizedDistance - 0.5f) < 0.1f) {
                            color = getColorFromHSB((hueOffset + 0.6f) % 1.0f, saturation, brightness * 1.2f);
                        }
                    }
                    // Draw center
                    else if (normalizedDistance < 0.1f) {
                        color = getColorFromHSB((hueOffset + 0.5f) % 1.0f, saturation * 0.7f, brightness * 1.5f);
                    }
                }

                pixels[y * SIZE + x] = color;
            }
        }

        // Create the texture from the pixel data using BitmapData
        BitmapData bitmap = BitmapData.createBitmap(SIZE, SIZE);

        // Fill the bitmap with the pixel data
        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                int pixel = pixels[y * SIZE + x];
                bitmap.setPixel(x, y, pixel);
            }
        }

        // Create the texture from the bitmap
        SmartTexture tex = new SmartTexture(bitmap);
        return tex;
    }

    // Simple HSB to RGB conversion
    private int getColorFromHSB(float h, float s, float b) {
        // Convert HSB to RGB
        float c = b * s; // Chroma
        float hp = h * 6;
        float x = c * (1 - Math.abs((hp % 2) - 1));

        float r, g, b_val;
        if (hp < 1) {
            r = c; g = x; b_val = 0;
        } else if (hp < 2) {
            r = x; g = c; b_val = 0;
        } else if (hp < 3) {
            r = 0; g = c; b_val = x;
        } else if (hp < 4) {
            r = 0; g = x; b_val = c;
        } else if (hp < 5) {
            r = x; g = 0; b_val = c;
        } else {
            r = c; g = 0; b_val = x;
        }

        float m = b - c;
        int red = (int) ((r + m) * 255) << 16;
        int green = (int) ((g + m) * 255) << 8;
        int blue = (int) ((b_val + m) * 255);

        // Alpha channel - make it semi-transparent
        int alpha = 0x66000000; // About 40% opacity

        return alpha | red | green | blue;
    }

    public void setSize(float width, float height) {
        // Don't change the internal size since we're using a fixed-size texture
        // Just update the needsUpdate flag
        needsUpdate = true;
        setWidth(width);
        setHeight(height);
    }
}