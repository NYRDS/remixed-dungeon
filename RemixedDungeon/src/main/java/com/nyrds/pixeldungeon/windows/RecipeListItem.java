package com.nyrds.pixeldungeon.windows;

import com.nyrds.pixeldungeon.alchemy.AlchemyRecipes;
import com.nyrds.pixeldungeon.items.common.ItemFactory;
import com.nyrds.pixeldungeon.mobs.common.MobFactory;
import com.nyrds.util.GuiProperties;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.Image;
import com.watabou.noosa.Text;
import com.watabou.noosa.ui.Component;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.sprites.CharSprite;
import com.watabou.pixeldungeon.ui.IClickable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lombok.Getter;

/**
 * Component for displaying an alchemy recipe in the WndAlchemy window
 */
public class RecipeListItem extends Component implements IClickable {

    private static final int SLOT_SIZE = 24;
    private static final int TITLE_COLOR = 0xFFFF44;
    private static final int SELECTED_COLOR = 0xFFCC00; // Yellow tint for selection
    private static final int UNSELECTED_COLOR = 0x00000000; // Transparent

    @Getter
    private final List<String> inputs;
    @Getter
    private final String output; // First output for backward compatibility
    private final List<String> outputs; // All outputs
    private final HBox ingredientsBox;
    private final com.watabou.noosa.ui.Component outputComponent; // Container for output display
    private final HBox outputBox;
    private final Text arrow;
    private final List<Item> inputItems; // Store actual item instances to check against hero inventory
    private final List<GrayableItemSprite> ingredientSlots; // Store references to item slots
    private ColorBlock background; // Store reference to background
    private Runnable clickListener;
    protected boolean clickable = false;
    @Getter
    private boolean isSelected = false;

    public RecipeListItem(List<String> inputs, String output) {
        this(inputs, Arrays.asList(output));
    }

    public RecipeListItem(List<String> inputs, List<String> outputs) {
        super();

        this.inputs = inputs;
        this.outputs = outputs != null ? new ArrayList<>(outputs) : new ArrayList<>();
        this.output = this.outputs.isEmpty() ? "" : this.outputs.get(0); // Use first output for display

        // Initialize the input items and ingredient slots lists
        inputItems = new ArrayList<>();
        ingredientSlots = new ArrayList<>();

        // Create HBox for ingredients
        ingredientsBox = new HBox(100); // Will be resized later
        ingredientsBox.setGap(2);

        // Add input ingredients to the ingredients box as ItemSlots
        for (String input : inputs) {
            Item ingredient = null;
            try {
                ingredient = ItemFactory.itemByName(input);
            } catch (Exception e) {
                // If item creation fails, ingredient remains null
            }

            if (ingredient != null) {
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
            } else {
                // Create an empty GrayableItemSprite as placeholder
                GrayableItemSprite inputSprite = new GrayableItemSprite();
                inputSprite.setSize(SLOT_SIZE, SLOT_SIZE);
                ingredientsBox.add(inputSprite);

                // Add to our list of sprites (will be grayed out by default)
                ingredientSlots.add(inputSprite);

                // Add null to maintain index alignment
                inputItems.add(null);
            }
        }

        // Create arrow symbol
        arrow = PixelScene.createText("→", GuiProperties.regularFontSize()); // Using unicode arrow
        arrow.hardlight(TITLE_COLOR);

        // Determine output type and create appropriate output component
        AlchemyRecipes.OutputType outputType = determineOutputType(output);

        if (outputType == AlchemyRecipes.OutputType.ITEM) {
            // Create output slot for item
            Item outputItem = null;
            try {
                outputItem = ItemFactory.itemByName(output);
            } catch (Exception e) {
                // If item creation fails, outputItem remains null
            }

            final ItemSpriteWrapper outputSpriteTemp;
            if (outputItem != null) {
                outputSpriteTemp = new ItemSpriteWrapper(outputItem);
                outputSpriteTemp.setSize(SLOT_SIZE, SLOT_SIZE);
            } else {
                // Create an empty ItemSpriteWrapper as placeholder
                outputSpriteTemp = new ItemSpriteWrapper();
                outputSpriteTemp.setSize(SLOT_SIZE, SLOT_SIZE);
            }
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
            outputComponent = wrappedItemComponent;
        } else if (outputType == AlchemyRecipes.OutputType.MOB) {
            // Create output slot for mob
            Image outputSpriteTemp;
            try {
                Mob mob = MobFactory.mobByName(output);
                CharSprite sprite = mob.newSprite();
                outputSpriteTemp = sprite.avatar();
                outputSpriteTemp.scale.set(SLOT_SIZE / outputSpriteTemp.width(), SLOT_SIZE / outputSpriteTemp.height());
            } catch (Exception e) {
                // If mob creation fails, create an empty Image as placeholder
                outputSpriteTemp = new Image();
                outputSpriteTemp.scale.set(SLOT_SIZE / outputSpriteTemp.width(), SLOT_SIZE / outputSpriteTemp.height());
            }

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
        } else {
            // Default to item if type is unknown
            final ItemSpriteWrapper defaultSprite = new ItemSpriteWrapper();
            defaultSprite.setSize(SLOT_SIZE, SLOT_SIZE);
            // Wrap the default ItemSpriteWrapper in a Component-compatible wrapper
            Component wrappedDefaultComponent = new Component() {
                {
                    add(defaultSprite);
                }

                @Override
                public void layout() {
                    super.layout();
                    defaultSprite.x = this.x;
                    defaultSprite.y = this.y;
                }
            };
            wrappedDefaultComponent.setSize(SLOT_SIZE, SLOT_SIZE);
            outputComponent = wrappedDefaultComponent;
        }

        // Create HBox for output
        outputBox = new HBox(100); // Will be resized later
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

                final ItemSpriteWrapper outputSpriteTemp;
                if (outputItem != null) {
                    outputSpriteTemp = new ItemSpriteWrapper(outputItem);
                    outputSpriteTemp.setSize(SLOT_SIZE, SLOT_SIZE);
                } else {
                    // Create an empty ItemSpriteWrapper as placeholder
                    outputSpriteTemp = new ItemSpriteWrapper();
                    outputSpriteTemp.setSize(SLOT_SIZE, SLOT_SIZE);
                }

                // Wrap the ItemSpriteWrapper in a Component-compatible wrapper
                com.watabou.noosa.ui.Component wrappedItemComponent = new com.watabou.noosa.ui.Component() {
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
                try {
                    com.watabou.pixeldungeon.actors.mobs.Mob mob = MobFactory.mobByName(singleOutput);
                    if (mob != null) {
                        CharSprite sprite = mob.newSprite();
                        outputSpriteTemp = sprite.avatar();
                        outputSpriteTemp.scale.set(SLOT_SIZE / outputSpriteTemp.width(), SLOT_SIZE / outputSpriteTemp.height());
                    } else {
                        outputSpriteTemp = new Image();
                        outputSpriteTemp.scale.set(SLOT_SIZE / outputSpriteTemp.width(), SLOT_SIZE / outputSpriteTemp.height());
                    }
                } catch (Exception e) {
                    // If mob creation fails, create an empty Image as placeholder
                    outputSpriteTemp = new Image();
                    outputSpriteTemp.scale.set(SLOT_SIZE / outputSpriteTemp.width(), SLOT_SIZE / outputSpriteTemp.height());
                }

                final Image finalOutputSpriteTemp = outputSpriteTemp;
                // Wrap the Image in a Component-compatible wrapper
                com.watabou.noosa.ui.Component wrappedComponent = new com.watabou.noosa.ui.Component() {
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
            } else {
                // Default to item if type is unknown
                final ItemSpriteWrapper defaultSprite = new ItemSpriteWrapper();
                defaultSprite.setSize(SLOT_SIZE, SLOT_SIZE);
                // Wrap the default ItemSpriteWrapper in a Component-compatible wrapper
                com.watabou.noosa.ui.Component wrappedDefaultComponent = new com.watabou.noosa.ui.Component() {
                    {
                        add(defaultSprite);
                    }

                    @Override
                    public void layout() {
                        super.layout();
                        defaultSprite.x = this.x;
                        defaultSprite.y = this.y;
                    }
                };
                wrappedDefaultComponent.setSize(SLOT_SIZE, SLOT_SIZE);

                outputBox.add(wrappedDefaultComponent);
            }
        }

        // Create background color block for selection highlighting
        ColorBlock background = new ColorBlock(width, height, UNSELECTED_COLOR);

        // Add components to this list item in the correct order (background first)
        add(background);
        add(ingredientsBox);
        add(arrow);
        add(outputBox);
    }

    /**
     * Determine the output type (item or mob) based on the entity name
     */
    private AlchemyRecipes.OutputType determineOutputType(String entityName) {
        // Check if it's a valid item class first
        if (ItemFactory.isValidItemClass(entityName)) {
            return AlchemyRecipes.OutputType.ITEM;
        }

        // Check if it's a seed (special case)
        if (entityName.endsWith(".Seed") ||
            entityName.equals("Sungrass.Seed") ||
            entityName.equals("Firebloom.Seed") ||
            entityName.equals("Icecap.Seed") ||
            entityName.equals("Sorrowmoss.Seed") ||
            entityName.equals("Dreamweed.Seed") ||
            entityName.equals("Earthroot.Seed") ||
            entityName.equals("Fadeleaf.Seed") ||
            entityName.equals("Moongrace.Seed") ||
            entityName.equals("Rotberry.Seed")) {
            return AlchemyRecipes.OutputType.ITEM;
        }

        // Check for other special cases that might be valid in alchemy
        // For example, some items might be referenced by their full class name
        if (entityName.startsWith("com.watabou.pixeldungeon.items.") ||
            entityName.startsWith("com.nyrds.pixeldungeon.items.")) {
            // Extract the simple class name and check if it's valid
            String[] parts = entityName.split("\\.");
            String className = parts[parts.length - 1];
            if (ItemFactory.isValidItemClass(className)) {
                return AlchemyRecipes.OutputType.ITEM;
            }
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

        // Layout ingredients tightly together
        float totalIngredientsWidth = 0;
        for (int i = 0; i < ingredientsBox.getLength(); i++) {
            GrayableItemSprite sprite = (GrayableItemSprite) ingredientsBox.getMember(i);
            sprite.setPos(x + totalIngredientsWidth, y + (height - sprite.height()) / 2);
            totalIngredientsWidth += sprite.width() - 10; // 2 pixels spacing between items
        }

        // Position the arrow after the ingredients
        arrow.setX(totalIngredientsWidth + 5); // 5 pixels space after ingredients
        arrow.setY(PixelScene.align(y + (height - arrow.baseLine()) / 2));

        // Calculate total width of all outputs
        float totalOutputWidth = 0;
        for (int i = 0; i < outputBox.getLength(); i++) {
            com.watabou.noosa.ui.Component outputComponent = (com.watabou.noosa.ui.Component) outputBox.getMember(i);
            totalOutputWidth += outputComponent.width() - 10; // 2 pixels spacing between outputs
        }

        // Position the outputBox container to the far right
        float outputBoxX = x + width - totalOutputWidth;
        // Ensure it doesn't go past the arrow
        float minOutputX = x + totalIngredientsWidth + arrow.width() + 10;
        outputBox.setPos(Math.max(outputBoxX, minOutputX), y + (height - outputBox.height()) / 2);

        // Layout the output box with all outputs from right to left
        float currentOutputX = 0;
        for (int i = 0; i < outputBox.getLength(); i++) {
            com.watabou.noosa.ui.Component outputComponent = (com.watabou.noosa.ui.Component) outputBox.getMember(i);
            outputComponent.setPos(currentOutputX, 0); // Position relative to outputBox
            currentOutputX += outputComponent.width() - 10; // 2 pixels spacing between outputs
        }

        outputBox.setSize(totalOutputWidth, height);
    }

    public void setOnClickListener(Runnable listener) {
        this.clickListener = listener;
        this.clickable = true;
    }


    public void setSelected(boolean selected) {
        isSelected = selected;

        arrow.hardlight(selected ? SELECTED_COLOR : TITLE_COLOR);
    }

    /**
     * Gets the items that are used as ingredients in this recipe
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