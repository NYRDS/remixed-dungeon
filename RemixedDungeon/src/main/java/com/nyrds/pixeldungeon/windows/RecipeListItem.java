package com.nyrds.pixeldungeon.windows;

import com.nyrds.pixeldungeon.items.common.ItemFactory;
import com.nyrds.util.GuiProperties;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.Text;
import com.watabou.noosa.ui.Component;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.ui.IClickable;
import com.watabou.pixeldungeon.ui.ItemSlot;

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
    private static final int UNSELECTED_COLOR = 0x00000000; // Transparent

    @Getter
    private final List<String> inputs;
    @Getter
    private final String output;
    private final HBox ingredientsBox;
    private final HBox outputBox;
    private final Text arrow;
    private final List<Item> inputItems; // Store actual item instances to check against hero inventory
    private final List<GrayableItemSlot> ingredientSlots; // Store references to item slots
    private ColorBlock background; // Store reference to background
    private Runnable clickListener;
    protected boolean clickable = false;
    @Getter
    private boolean isSelected = false;

    public RecipeListItem(List<String> inputs, String output) {
        super();

        this.inputs = inputs;
        this.output = output;

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

                // Create a GrayableItemSlot with the ingredient
                GrayableItemSlot inputSlot = new GrayableItemSlot(ingredient);
                inputSlot.setSize(SLOT_SIZE, SLOT_SIZE);

                // Initially set the item slot to not grayed out
                // The actual state will be updated when the recipe list is refreshed
                inputSlot.setGrayedOut(false);

                // Check if the hero has this ingredient in inventory
                boolean hasIngredient = checkHeroHasItem(ingredient);

                // Gray out the item slot if the ingredient is missing
                inputSlot.setGrayedOut(!hasIngredient);

                ingredientsBox.add(inputSlot);
                ingredientSlots.add(inputSlot); // Add to our list of slots
            } else {
                // Create an empty GrayableItemSlot as placeholder
                GrayableItemSlot inputSlot = new GrayableItemSlot();
                inputSlot.setSize(SLOT_SIZE, SLOT_SIZE);
                ingredientsBox.add(inputSlot);

                // Add to our list of slots (will be grayed out by default)
                ingredientSlots.add(inputSlot);

                // Add null to maintain index alignment
                inputItems.add(null);
            }
        }

        // Create arrow symbol
        arrow = PixelScene.createText("→", GuiProperties.regularFontSize()); // Using unicode arrow
        arrow.hardlight(TITLE_COLOR);

        // Create output slot
        Item outputItem = null;
        try {
            outputItem = ItemFactory.itemByName(output);
        } catch (Exception e) {
            // If item creation fails, outputItem remains null
        }

        ItemSlot outputSlotTemp = null;
        if (outputItem != null) {
            outputSlotTemp = new ItemSlot(outputItem);
            outputSlotTemp.setSize(SLOT_SIZE, SLOT_SIZE);
        } else {
            // Create an empty ItemSlot as placeholder
            outputSlotTemp = new ItemSlot();
            outputSlotTemp.setSize(SLOT_SIZE, SLOT_SIZE);
        }

        // Create HBox for output
        outputBox = new HBox(100); // Will be resized later
        outputBox.setGap(2);
        outputBox.add(outputSlotTemp);

        // Create background color block for selection highlighting
        ColorBlock background = new ColorBlock(width, height, UNSELECTED_COLOR);

        // Add components to this list item in the correct order (background first)
        add(background);
        add(ingredientsBox);
        add(arrow);
        add(outputBox);
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
            GrayableItemSlot slot = (GrayableItemSlot) ingredientsBox.getMember(i);
            slot.setRect(x + totalIngredientsWidth, y, slot.width(), height);
            totalIngredientsWidth += slot.width() + 2; // 2 pixels spacing between items
        }

        // Position the arrow after the ingredients
        arrow.setX(totalIngredientsWidth + 5); // 5 pixels space after ingredients
        arrow.setY(PixelScene.align(y + (height - arrow.baseLine()) / 2));

        // Position the output box to the far right
        outputBox.setPos(width - outputBox.width(), y); // Align to the right edge of the component
        outputBox.setSize(outputBox.width(), height);

        // Adjust arrow position if output box overlaps
        if (arrow.getX() + arrow.width() >= outputBox.left()) {
            arrow.setX(outputBox.left() - arrow.width() - 5); // Ensure space between arrow and output
        }
    }

    public void setOnClickListener(Runnable listener) {
        this.clickListener = listener;
        this.clickable = true;
    }


    public void setSelected(boolean selected) {
        isSelected = selected;

        // Update the background color to highlight the entire row
        if (background != null) {
            background.color(selected ? SELECTED_COLOR : UNSELECTED_COLOR);
        }

        // Also highlight the arrow text
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