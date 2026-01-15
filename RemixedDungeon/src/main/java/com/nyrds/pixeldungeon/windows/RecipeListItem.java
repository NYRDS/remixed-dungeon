package com.nyrds.pixeldungeon.windows;

import com.nyrds.pixeldungeon.alchemy.AlchemyRecipes;
import com.nyrds.pixeldungeon.items.common.ItemFactory;
import com.nyrds.pixeldungeon.mobs.common.MobFactory;
import com.watabou.noosa.Image;
import com.watabou.noosa.ui.Component;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.sprites.CharSprite;
import com.watabou.pixeldungeon.ui.IClickable;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;

/**
 * Component for displaying an alchemy recipe in the WndAlchemy window
 */
public class RecipeListItem extends Component implements IClickable {

    private static final int SLOT_SIZE = 24;
    private static final int TITLE_COLOR = 0xFFFF44;
    private static final int SELECTED_COLOR = 0xFFCC00; // Yellow tint for selection

    @Getter
    private final List<String> inputs;
    @Getter
    private final String output; // First output for backward compatibility

    private final HBox rowBox;
    private final List<Item> inputItems; // Store actual item instances to check against hero inventory
    private final List<GrayableItemSprite> ingredientSlots; // Store references to item slots
    private Runnable clickListener;
    protected boolean clickable = false;
    @Getter
    private boolean isSelected = false;


    public RecipeListItem(List<String> inputs, List<String> outputs) {
        super();

        this.inputs = inputs;
        // All outputs
        List<String> outputs1 = outputs != null ? new ArrayList<>(outputs) : new ArrayList<>();
        this.output = outputs1.isEmpty() ? "" : outputs1.get(0); // Use first output for display

        // Initialize the input items and ingredient slots lists
        inputItems = new ArrayList<>();
        ingredientSlots = new ArrayList<>();

        rowBox = new HBox(150); // Will be resized later
        // Create HBox for ingredients
        HBox ingredientsBox = new HBox(100); // Will be resized later
        ingredientsBox.setGap(-10);

        // Add input ingredients to the ingredients box as ItemSlots
        for (String input : inputs) {
            Item ingredient = ItemFactory.itemByName(input);
            // Store the ingredient for later inventory checking
            inputItems.add(ingredient);

            // Create a GrayableItemSprite with the ingredient
            GrayableItemSprite inputSprite = new GrayableItemSprite(ingredient);
            inputSprite.setSize(SLOT_SIZE, SLOT_SIZE);

            // Initially set the item sprite to not grayed out
            // The actual state will be updated when the recipe list is refreshed
            inputSprite.setGrayedOut(false);

            // Check if the hero has this ingredient in inventory
            boolean hasIngredient = checkHeroHasItem(ingredient);

            // Gray out the item sprite if the ingredient is missing
            inputSprite.setGrayedOut(!hasIngredient);

            ingredientsBox.add(inputSprite);
            ingredientSlots.add(inputSprite); // Add to our list of sprites
        }

        // Determine output type and create appropriate output component
        AlchemyRecipes.OutputType outputType = determineOutputType(output);

        // Container for output display
        Component outputComponent;
        if (outputType == AlchemyRecipes.OutputType.ITEM) {
            // Create output slot for item
            Item outputItem = null;
            try {
                outputItem = ItemFactory.itemByName(output);
            } catch (Exception e) {
                // If item creation fails, outputItem remains null
            }

            final ItemSpriteWrapper outputSpriteTemp = new ItemSpriteWrapper(outputItem);
                outputSpriteTemp.setSize(SLOT_SIZE, SLOT_SIZE);
            // Wrap the ItemSpriteWrapper in a Component-compatible wrapper
            Component wrappedItemComponent = new com.watabou.noosa.ui.Component() {
                {
                    add(outputSpriteTemp);
                }

                @Override
                public void layout() {
                    super.layout();
                    outputSpriteTemp.x = this.x;
                    outputSpriteTemp.y = this.y;
                }
            };
            wrappedItemComponent.setSize(SLOT_SIZE, SLOT_SIZE);
        } else if (outputType == AlchemyRecipes.OutputType.MOB) {
            // Create output slot for mob
            Image outputSpriteTemp;

                Mob mob = MobFactory.mobByName(output);
                CharSprite sprite = mob.newSprite();
                outputSpriteTemp = sprite.avatar();
                outputSpriteTemp.scale.set(SLOT_SIZE / outputSpriteTemp.width(), SLOT_SIZE / outputSpriteTemp.height());

            final Image finalOutputSpriteTemp = outputSpriteTemp;
            // Wrap the Image in a Component-compatible wrapper
            Component wrappedComponent = new Component() {
                {
                    add(finalOutputSpriteTemp);
                }

                @Override
                public void layout() {
                    super.layout();
                    finalOutputSpriteTemp.x = this.x;
                    finalOutputSpriteTemp.y = this.y;
                }
            };
            wrappedComponent.setSize(SLOT_SIZE, SLOT_SIZE);
            outputComponent = wrappedComponent;

        }

        // Create HBox for output
        HBox outputBox = new HBox(100); // Will be resized later
        outputBox.setGap(2);

        // Add all output components to the output box
        for (String singleOutput : outputs) {
            AlchemyRecipes.OutputType singleOutputType = determineOutputType(singleOutput);

            if (singleOutputType == AlchemyRecipes.OutputType.ITEM) {
                // Create output slot for item
                Item outputItem = null;
                try {
                    outputItem = ItemFactory.itemByName(singleOutput);
                } catch (Exception e) {
                    // If item creation fails, outputItem remains null
                }

                final ItemSpriteWrapper outputSpriteTemp = new ItemSpriteWrapper(outputItem);
                outputSpriteTemp.setSize(SLOT_SIZE, SLOT_SIZE);

                // Wrap the ItemSpriteWrapper in a Component-compatible wrapper
                Component wrappedItemComponent = new Component() {
                    {
                        add(outputSpriteTemp);
                    }

                    @Override
                    public void layout() {
                        super.layout();
                        outputSpriteTemp.x = this.x;
                        outputSpriteTemp.y = this.y;
                    }
                };
                wrappedItemComponent.setSize(SLOT_SIZE, SLOT_SIZE);

                outputBox.add(wrappedItemComponent);
            } else if (singleOutputType == AlchemyRecipes.OutputType.MOB) {
                // Create output slot for mob
                Image outputSpriteTemp;

                Mob mob = MobFactory.mobByName(singleOutput);

                CharSprite sprite = mob.newSprite();
                outputSpriteTemp = sprite.avatar();
                outputSpriteTemp.scale.set(SLOT_SIZE / outputSpriteTemp.width(), SLOT_SIZE / outputSpriteTemp.height());

                final Image finalOutputSpriteTemp = outputSpriteTemp;
                // Wrap the Image in a Component-compatible wrapper
                Component wrappedComponent = new Component() {
                    {
                        add(finalOutputSpriteTemp);
                    }

                    @Override
                    public void layout() {
                        super.layout();
                        finalOutputSpriteTemp.x = this.x;
                        finalOutputSpriteTemp.y = this.y;
                    }
                };
                wrappedComponent.setSize(SLOT_SIZE, SLOT_SIZE);

                outputBox.add(wrappedComponent);
            }
        }

        rowBox.add(ingredientsBox);
//        rowBox.add(arrow);
        rowBox.add(outputBox);
        rowBox.setSize(width, height);
        rowBox.setAlign(HBox.Align.Width);
        add(rowBox);
        rowBox.layout();
        rowBox.setPos(x, y);

        setSize(rowBox.width(), rowBox.height());

    }

    /**
     * Determine the output type (item or mob) based on the entity name
     */
    private AlchemyRecipes.OutputType determineOutputType(String entityName) {
        // Check if it's a valid item class first
        if (ItemFactory.isValidItemClass(entityName)) {
            return AlchemyRecipes.OutputType.ITEM;
        }

        // If it's not an item, check if it's a mob
        if (MobFactory.hasMob(entityName)) {
            return AlchemyRecipes.OutputType.MOB;
        }

        // Default to item for backward compatibility
        return AlchemyRecipes.OutputType.ITEM;
    }

    private boolean checkHeroHasItem(Item item) {
        // Check if the hero exists and has the item in their inventory
        // Use the public checkItem method to see if the hero has this item
        Item heroItem = Dungeon.hero.checkItem(item.getEntityKind());

        // If the returned item is not a dummy, it means the hero has it
        return heroItem.valid();
    }

    @Override
    public void layout() {
        super.layout();
        rowBox.setMaxWidth(width);

        rowBox.setPos(PixelScene.align(x), PixelScene.align(y));
    }

    public void setOnClickListener(Runnable listener) {
        this.clickListener = listener;
        this.clickable = true;
    }


    public void setSelected(boolean selected) {
        isSelected = selected;
        for (var item:ingredientSlots) {
            item.setGrayedOut(!selected);
        }
    }

    /**
     * Gets the items that are used as ingredients in this recipe
     *
     * @return List of ingredient items
     */
    public List<Item> getIngredientItems() {
        return inputItems;
    }

    @Override
    public boolean onClick(float x, float y) {
        if (clickable && inside(x, y)) {
            if (clickListener != null) {
                clickListener.run();
            }
            return true;
        }
        return false;
    }
}