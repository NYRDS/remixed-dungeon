package com.nyrds.pixeldungeon.windows;

import com.nyrds.pixeldungeon.alchemy.AlchemyRecipes;
import com.nyrds.pixeldungeon.items.common.ItemFactory;
import com.nyrds.pixeldungeon.mobs.common.MobFactory;
import com.watabou.noosa.Group;
import com.watabou.noosa.Image;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.nyrds.pixeldungeon.game.GameLoop;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.sprites.ItemSprite;

import java.util.ArrayList;
import java.util.List;

/**
 * Animated Transmutation Circle that shows flying ingredients and wiggling outputs
 */
public class AnimatedTransmutationCircle extends Group {

    private TransmutationCircle transmutationCircle;
    private List<Image> ingredientSprites = new ArrayList<>();
    private List<Image> outputSprites = new ArrayList<>();

    // Size properties
    public float width;
    public float height;

    // Animation properties
    private float animationTime = 0;
    private boolean isAnimating = false;

    // Center position for animations
    private float centerX, centerY;

    public AnimatedTransmutationCircle() {
        super();

        // Create the underlying transmutation circle
        transmutationCircle = new TransmutationCircle();
        add(transmutationCircle);

        // Set initial width and height based on the transmutation circle
        width = transmutationCircle.width;
        height = transmutationCircle.height;
    }

    @Override
    public void update() {
        super.update();
        transmutationCircle.update();

        animationTime += GameLoop.elapsed;

        // Update ingredient positions (flying around center)
        updateIngredientPositions();

        // Update output positions (wiggling near center)
        updateOutputPositions();
    }

    public void startAnimation(List<String> ingredients, List<String> outputs) {
        // Clear previous sprites
        clearSprites();

        centerX = width / 2f;
        centerY = height / 2f;

        // Create ingredient sprites
        for (String ingredientName : ingredients) {
            Item ingredient = ItemFactory.itemByName(ingredientName);
            if (ingredient != null) {
                ItemSprite sprite = new ItemSprite(ingredient);
                sprite.origin.set(sprite.width() / 2, sprite.height() / 2);

                // Randomize initial position around the center
                float randomRadius = Math.min(transmutationCircle.width, transmutationCircle.height) * 0.4f; // 40% of the smallest dimension
                float angle = (float) (Math.random() * 2 * Math.PI); // Random angle
                float distance = (float) (Math.random() * randomRadius); // Random distance up to radius

                float randomX = centerX + (float) Math.cos(angle) * distance;
                float randomY = centerY + (float) Math.sin(angle) * distance;

                // Position sprite at random location, accounting for sprite dimensions
                sprite.x = randomX;
                sprite.y = randomY;

                add(sprite);
                ingredientSprites.add(sprite);
            }
        }

        // Create output sprites
        for (String outputName : outputs) {
            AlchemyRecipes.EntityType entityType = determineOutputType(outputName);

            if (entityType == AlchemyRecipes.EntityType.ITEM) {
                Item outputItem = ItemFactory.itemByName(outputName);
                if (outputItem != null) {
                    ItemSprite sprite = new ItemSprite(outputItem);
                    sprite.origin.set(sprite.width() / 2, sprite.height() / 2);
                    add(sprite);
                    outputSprites.add(sprite);
                }
            } else if (entityType == AlchemyRecipes.EntityType.MOB) {
                Mob mob = MobFactory.mobByName(outputName);
                Image sprite = mob.newSprite().avatar();
                sprite.origin.set(sprite.width() / 2, sprite.height() / 2);
                add(sprite);
                outputSprites.add(sprite);
            }
        }

        isAnimating = true;
        animationTime = 0;
    }

    private void updateIngredientPositions() {
        for (int i = 0; i < ingredientSprites.size(); i++) {
            Image sprite = ingredientSprites.get(i);

            float x = 0;
            float y = 0;

            // Apply gravitational pull toward center
            float dx = centerX - sprite.x;
            float dy = centerY - sprite.y;
            float distanceToCenter = (float) Math.sqrt(dx * dx + dy * dy);


            float gravitationConstant = 2000f; // Adjust for desired strength
            if (distanceToCenter > 10) {
                // Apply center gravity with inverse square law
                float centerGravity = gravitationConstant / (distanceToCenter * distanceToCenter);
                x += dx * centerGravity * 0.02f;  // Reduced force to make it more stable
                y += dy * centerGravity * 0.02f;
            }

            // Apply gravitational forces between ingredients
            for (int j = 0; j < ingredientSprites.size(); j++) {
                if (i != j) {
                    Image otherSprite = ingredientSprites.get(j);
                    float otherX = otherSprite.x;  // Convert from top-left to center coordinates
                    float otherY = otherSprite.y;

                    float diffX = otherX - sprite.x;
                    float diffY = otherY - sprite.y;
                    float distanceToOther = (float) Math.sqrt(diffX * diffX + diffY * diffY);

                    if (distanceToOther > 10) {
                        // Calculate gravitational force (attraction) using inverse square law
                        float attractionForce = gravitationConstant / (distanceToOther * distanceToOther);

                        // Apply attraction force toward the other ingredient
                        x += (diffX / distanceToOther) * attractionForce;  // Reduced force to make it more stable
                        y += (diffY / distanceToOther) * attractionForce;
                    }
                }
            }

            // Smoothly animate to calculated position
            sprite.speed.x += x * GameLoop.elapsed;
            sprite.speed.y += y * GameLoop.elapsed;
            sprite.dirtyMatrix = true;

            // Add slight rotation based on movement
            sprite.angle = (float) Math.toDegrees(Math.atan2(dy, dx));
        }
    }

    private void updateOutputPositions() {
        float radius = Math.min(transmutationCircle.width, transmutationCircle.height) * 0.1f; // Smaller radius for outputs

        for (int i = 0; i < outputSprites.size(); i++) {
            Image sprite = outputSprites.get(i);

            // Outputs should stay closer to the center with wiggling motion
            float offsetX = (float) (Math.sin(animationTime + i) * radius);
            float offsetY = (float) (Math.cos(animationTime + i * 0.5f) * radius);

            sprite.x = centerX + offsetX;
            sprite.y = centerY + offsetY;

            sprite.dirtyMatrix = true;
        }
    }

    public void stopAnimation() {
        isAnimating = false;
        clearSprites();
    }

    private void clearSprites() {
        // Remove all ingredient sprites
        for (Image sprite : ingredientSprites) {
            remove(sprite);
        }
        ingredientSprites.clear();

        // Remove all output sprites
        for (Image sprite : outputSprites) {
            remove(sprite);
        }
        outputSprites.clear();
    }

    /**
     * Determine the output type (item or mob) based on the entity name
     */
    private AlchemyRecipes.EntityType determineOutputType(String entityName) {
        // Check if it's a valid item class first
        if (ItemFactory.isValidItemClass(entityName)) {
            return AlchemyRecipes.EntityType.ITEM;
        }

        // If it's not an item, check if it's a mob
        if (MobFactory.hasMob(entityName)) {
            return AlchemyRecipes.EntityType.MOB;
        }

        // Default to item for backward compatibility
        return AlchemyRecipes.EntityType.ITEM;
    }

    public void setScale(float scale) {
        transmutationCircle.setScale(scale);
        width = transmutationCircle.width;
        height = transmutationCircle.height;
    }

    public void setPos(float x, float y) {
        transmutationCircle.setPos(x, y);
        for (Image sprite : ingredientSprites) {
            sprite.setPos(sprite.x + x, sprite.y + y);
        }
        for (Image sprite : outputSprites) {
            sprite.setPos(sprite.x + x, sprite.y + y);
        }
    }
}