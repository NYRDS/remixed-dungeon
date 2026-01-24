package com.nyrds.pixeldungeon.windows;

import com.nyrds.pixeldungeon.alchemy.AlchemyRecipes;
import com.nyrds.util.GuiProperties;
import com.watabou.noosa.Text;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.ui.RedButton;
import com.watabou.pixeldungeon.ui.ScrollPane;
import com.watabou.pixeldungeon.ui.Window;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Window for displaying and executing alchemy recipes that include a specific item
 */
public class WndItemAlchemy extends Window {

    private static final int MARGIN = 2;
    private static final int SMALL_GAP = 1;
    private static final int TITLE_COLOR = 0xFFFF44;
    private final VBox mainLayout;

    // Track selected recipe
    private Entry<List<String>, List<String>> selectedRecipe;
    private RecipeListItem selectedRow;
    private RedButton executeButton;
    private Item item;

    // Track recipe rows for selection
    private ArrayList<RecipeListItem> recipeRows = new ArrayList<>();

    // Recipe description text
    private Text recipeDescription;

    public WndItemAlchemy(Item item) {
        super();
        this.item = item;

        // Calculate almost fullscreen dimensions
        float screenWidth = PixelScene.uiCamera.width;
        float screenHeight = PixelScene.uiCamera.height;

        // Leave some margin around the window
        float windowWidth = screenWidth - 10; // Reduced margin for tighter fit
        float windowHeight = screenHeight - 10; // Reduced margin for tighter fit

        // Main layout
        mainLayout = new VBox();
        mainLayout.setRect(MARGIN, MARGIN, windowWidth - 2 * MARGIN, 0);
        mainLayout.setGap(SMALL_GAP); // Reduced gap for tighter fit
        add(mainLayout);

        // Title
        Text title = PixelScene.createText("Alchemy with " + item.name(), GuiProperties.titleFontSize());
        title.hardlight(TITLE_COLOR);
        title.setX(MARGIN);
        mainLayout.add(title);

        // Calculate player's inventory
        Map<String, Integer> playerInventory = new HashMap<>();
        for (Item inventoryItem : Dungeon.hero.getBelongings().backpack.items) {
            String itemName = inventoryItem.getEntityKind(); // Use getEntityKind() to get the class name
            playerInventory.put(itemName, playerInventory.getOrDefault(itemName, 0) + inventoryItem.quantity());
        }

        // Get recipes that contain this specific item
        List<Entry<List<String>, List<String>>> recipesWithItem =
            AlchemyRecipes.getRecipesContainingItem(item.getEntityKind());

        // Filter to only show recipes for which the player has all required ingredients
        List<Entry<List<String>, List<String>>> availableRecipes = new ArrayList<>();
        for (Entry<List<String>, List<String>> recipe : recipesWithItem) {
            if (AlchemyRecipes.hasRequiredIngredients(recipe.getKey(), playerInventory)) {
                availableRecipes.add(recipe);
            }
        }

        // Recipes container
        VBox recipesContainer = new VBox();
        recipesContainer.setRect(0, 0, windowWidth - 4 * MARGIN, 0);
        recipesContainer.setGap(SMALL_GAP); // Reduced gap for tighter fit

        if (availableRecipes.isEmpty()) {
            Text noRecipes = PixelScene.createMultiline("No recipes available with this item and your current inventory.", 8);
            noRecipes.maxWidth((int)(windowWidth - 4 * MARGIN));
            mainLayout.add(noRecipes);
        } else {
            for (var recipeEntry : availableRecipes) {
                // Create a recipe list item
                RecipeListItem recipeItem = getRecipeListItem(recipeEntry, windowWidth);

                // Add the recipe item to the recipes container
                recipesContainer.add(recipeItem);

                // Track this recipe item for selection
                recipeRows.add(recipeItem);
            }

            // Add scroll pane for recipes - limit height for better layout
            float scrollHeight = Math.min(windowHeight * 0.4f, 120); // Limit recipe list height for better layout

            ScrollPane recipeScrollPane = createScrollPane(recipesContainer, availableRecipes);
            recipeScrollPane.setRect(0, 0, windowWidth - 4 * MARGIN, scrollHeight);
            recipeScrollPane.measure();
            mainLayout.add(recipeScrollPane);
        }

        // Recipe description area
        recipeDescription = PixelScene.createMultiline("", 6); // Smaller font for tighter fit
        recipeDescription.maxWidth((int)(windowWidth - 4 * MARGIN));
        mainLayout.add(recipeDescription);

        // Execute and Close buttons container
        HBox buttonsContainer = new HBox(windowWidth - 2 * MARGIN);
        buttonsContainer.setAlign(HBox.Align.Width);
        buttonsContainer.setGap(SMALL_GAP); // Reduced gap for tighter fit

        // Execute button
        executeButton = new RedButton("Execute Recipe") {
            @Override
            protected void onClick() {
                executeSelectedRecipe();
            }
        };
        executeButton.setSize(Math.min(80, windowWidth/4), 16); // Smaller button height for tighter fit
        executeButton.enable(selectedRecipe != null); // Initially disabled until a recipe is selected
        buttonsContainer.add(executeButton);

        // Close button
        RedButton closeButton = new RedButton("Close") {
            @Override
            protected void onClick() {
                hide();
            }
        };
        closeButton.setSize(Math.min(60, windowWidth/5), 16); // Smaller button height for tighter fit
        buttonsContainer.add(closeButton);

        // Add the buttons container to the main layout
        mainLayout.add(buttonsContainer);

        // Update the layout
        mainLayout.layout();

        // Calculate remaining space to fill and add a flexible spacer
        float totalHeight = mainLayout.bottom();
        float buttonsY = windowHeight - MARGIN - buttonsContainer.height();
        float availableSpace = buttonsY - totalHeight;

        if (availableSpace > 0) {
            // Add empty space at the end to push buttons to the bottom
            com.watabou.noosa.ui.Component spacer = new com.watabou.noosa.ui.Component() {
                @Override
                public void layout() {
                    height = availableSpace;
                }
            };
            mainLayout.add(spacer);
        }

        mainLayout.layout();
        resize((int)windowWidth, (int)windowHeight);
    }

    private RecipeListItem getRecipeListItem(Entry<List<String>, List<String>> recipeEntry, float windowWidth) {
        RecipeListItem recipeItem = new RecipeListItem(recipeEntry.getKey(), recipeEntry.getValue());

        // Set the size for the recipe item
        recipeItem.setSize(windowWidth, recipeItem.height());
        // Click handling is now done in the recipe item itself
        recipeItem.setOnClickListener(() -> {
            // Deselect previous selection
            if (selectedRow != null) {
                selectedRow.setSelected(false);
            }

            // Update selection
            selectedRow = recipeItem;
            selectedRecipe = recipeEntry;

            // Highlight the selected row
            selectedRow.setSelected(true);

            // Update button to reflect selection
            updateExecuteButton();

            // Update recipe description
            updateRecipeDescription();
        });
        return recipeItem;
    }

    private ScrollPane createScrollPane(VBox recipesContainer, List<Entry<List<String>, List<String>>> availableRecipes) {
        ScrollPane recipeScrollPane = new ScrollPane(recipesContainer) {
            @Override
            public void onClick(float x, float y) {
                // Iterate through the recipe items to find which one was clicked
                for (int i = 0; i < recipeRows.size(); i++) {
                    RecipeListItem recipeItem = recipeRows.get(i);

                    // Check if the click is within the bounds of this recipe item
                    if (recipeItem.inside(x, y)) {
                        // Deselect previous selection
                        if (selectedRow != null) {
                            selectedRow.setSelected(false);
                        }

                        // Select current item
                        selectedRow = recipeItem;
                        selectedRecipe = availableRecipes.get(i);

                        // Highlight the selected row
                        selectedRow.setSelected(true);

                        // Update button to reflect selection
                        updateExecuteButton();

                        // Update recipe description
                        updateRecipeDescription();
                        break;
                    }
                }
            }
        };
        recipeScrollPane.scrollTo(0,0);
        return recipeScrollPane;
    }

    private void updateExecuteButton() {
        executeButton.enable(selectedRecipe != null);
    }

    private void updateRecipeDescription() {
        if (selectedRecipe != null) {
            List<String> inputs = selectedRecipe.getKey();
            List<String> outputs = selectedRecipe.getValue();

            StringBuilder description = new StringBuilder();
            description.append("Recipe: ");

            // Count occurrences of each ingredient
            Map<String, Integer> inputCounts = new HashMap<>();
            for (String input : inputs) {
                inputCounts.put(input, inputCounts.getOrDefault(input, 0) + 1);
            }

            // Add input ingredients with counts
            boolean first = true;
            for (Map.Entry<String, Integer> entry : inputCounts.entrySet()) {
                if (!first) {
                    description.append(" + ");
                }
                first = false;

                String input = entry.getKey();
                int count = entry.getValue();

                Item inputItem = com.nyrds.pixeldungeon.items.common.ItemFactory.itemByName(input);
                if (inputItem != null) {
                    if (count > 1) {
                        description.append(inputItem.name()).append(" x").append(count);
                    } else {
                        description.append(inputItem.name());
                    }
                } else {
                    if (count > 1) {
                        description.append(input).append(" x").append(count);
                    } else {
                        description.append(input);
                    }
                }
            }

            description.append(" → ");

            // Count occurrences of each output
            Map<String, Integer> outputCounts = new HashMap<>();
            for (String output : outputs) {
                outputCounts.put(output, outputCounts.getOrDefault(output, 0) + 1);
            }

            // Add output items with counts
            first = true;
            for (Map.Entry<String, Integer> entry : outputCounts.entrySet()) {
                if (!first) {
                    description.append(" + ");
                }
                first = false;

                String output = entry.getKey();
                int count = entry.getValue();

                Item outputItem = com.nyrds.pixeldungeon.items.common.ItemFactory.itemByName(output);
                if (outputItem != null) {
                    if (count > 1) {
                        description.append(outputItem.name()).append(" x").append(count);
                    } else {
                        description.append(outputItem.name());
                    }
                } else {
                    if (count > 1) {
                        description.append(output).append(" x").append(count);
                    } else {
                        description.append(output);
                    }
                }
            }

            recipeDescription.text(description.toString());
        } else {
            recipeDescription.text("");
        }

        // Update the layout to accommodate the new text
        mainLayout.layout();
    }

    private void executeSelectedRecipe() {
        if (selectedRecipe != null) {
            // Remove the required ingredients from the player's inventory
            List<String> inputs = selectedRecipe.getKey();

            // Count required quantities for each ingredient
            Map<String, Integer> requiredQuantities = new HashMap<>();
            for (String ingredient : inputs) {
                requiredQuantities.put(ingredient, requiredQuantities.getOrDefault(ingredient, 0) + 1);
            }

            // Remove ingredients from inventory
            for (Map.Entry<String, Integer> required : requiredQuantities.entrySet()) {
                String ingredientName = required.getKey();
                int requiredQty = required.getValue();

                // Find and remove the required items
                List<Item> itemsToRemove = new ArrayList<>();
                int removedCount = 0;

                for (Item inventoryItem : Dungeon.hero.getBelongings().backpack.items) {
                    if (inventoryItem.getEntityKind().equals(ingredientName) && removedCount < requiredQty) {
                        int qtyToRemove = Math.min(inventoryItem.quantity(), requiredQty - removedCount);

                        if (qtyToRemove >= inventoryItem.quantity()) {
                            itemsToRemove.add(inventoryItem);
                            removedCount += inventoryItem.quantity();
                        } else {
                            inventoryItem.quantity(inventoryItem.quantity() - qtyToRemove);
                            removedCount += qtyToRemove;
                        }
                    }
                }

                // Actually remove the items
                for (Item itemToRemove : itemsToRemove) {
                    itemToRemove.detachAll(Dungeon.hero.getBelongings().backpack);
                }
            }

            // Add the output items to the player's inventory
            List<String> outputs = selectedRecipe.getValue();
            for (String outputName : outputs) {
                Item outputItem = com.nyrds.pixeldungeon.items.common.ItemFactory.itemByName(outputName);
                if (outputItem != null) {
                    Dungeon.level.drop(outputItem, Dungeon.hero.getPos()).sprite.drop();
                }
            }

            // Close the window after executing the recipe
            hide();
        }
    }
}