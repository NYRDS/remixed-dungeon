package com.nyrds.pixeldungeon.windows;

import com.nyrds.pixeldungeon.alchemy.AlchemyRecipes;
import com.nyrds.pixeldungeon.items.common.ItemFactory;
import com.nyrds.pixeldungeon.mobs.common.MobFactory;
import com.nyrds.platform.EventCollector;
import com.nyrds.platform.game.RemixedDungeon;
import com.nyrds.util.GuiProperties;
import com.watabou.noosa.Text;
import com.watabou.noosa.ui.Component;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.ui.RedButton;
import com.watabou.pixeldungeon.ui.ScrollPane;
import com.watabou.pixeldungeon.ui.Window;

import org.jetbrains.annotations.NotNull;

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
    private final RedButton executeButton;

    // Track recipe rows for selection
    private final ArrayList<RecipeListItem> recipeRows = new ArrayList<>();

    // Recipe description text
    private final Text recipeDescription;
    final private Char hero;

    public WndItemAlchemy(Item item, @NotNull Char chr) {
        super();

        hero = chr;

        // Calculate almost fullscreen dimensions
        float screenWidth = RemixedDungeon.landscape() ? Window.STD_WIDTH_L : Window.STD_WIDTH_P;
        float screenHeight = 120;

        // Leave some margin around the window
        float windowWidth = screenWidth - 10; // Reduced margin for tighter fit
        float windowHeight = screenHeight - 10; // Reduced margin for tighter fit

        // Main layout
        mainLayout = new VBox();
        mainLayout.setRect(MARGIN, MARGIN, windowWidth - 2 * MARGIN, 0);
        mainLayout.setGap(SMALL_GAP); // Reduced gap for tighter fit
        add(mainLayout);

        // Title
        Text title = PixelScene.createText("Alchemy recipes with " + item.name(), GuiProperties.titleFontSize());
        title.hardlight(TITLE_COLOR);
        title.setX(MARGIN);
        mainLayout.add(title);

        // Calculate player's inventory
        Map<String, Integer> playerInventory = new HashMap<>();
        for (Item inventoryItem : hero.getBelongings().backpack.items) {
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
            noRecipes.maxWidth((int) (windowWidth - 4 * MARGIN));
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
            float scrollHeight =  windowHeight - 40;

            ScrollPane recipeScrollPane = createScrollPane(recipesContainer, availableRecipes);
            recipeScrollPane.setRect(0, 0, windowWidth, scrollHeight);
            recipeScrollPane.measure();
            mainLayout.add(recipeScrollPane);
        }


        VBox controlsBox = new VBox();

        // Recipe description area
        recipeDescription = PixelScene.createMultiline("Select recipe", GuiProperties.regularFontSize());
        recipeDescription.maxWidth((int) (windowWidth - 4 * MARGIN));
        recipeDescription.minHeight(60);
        controlsBox.add(recipeDescription);

        controlsBox.layout();

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
        executeButton.autoSize();

        executeButton.enable(selectedRecipe != null);
        buttonsContainer.add(executeButton);

        // Close button
        RedButton closeButton = new RedButton("Close") {
            @Override
            protected void onClick() {
                hide();
            }
        };

        closeButton.autoSize();
        buttonsContainer.add(closeButton);

        // Add the buttons container to the main layout
        controlsBox.add(buttonsContainer);

        mainLayout.add(controlsBox);

        mainLayout.layout();
        resize((int) windowWidth, (int) windowHeight);
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
        recipeScrollPane.scrollTo(0, 0);
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

                Item inputItem = ItemFactory.itemByName(input);
                if (count > 1) {
                    description.append(inputItem.name()).append(" x").append(count);
                } else {
                    description.append(inputItem.name());
                }
            }

            description.append(" = ");

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

                Item outputItem = ItemFactory.itemByName(output);
                // Determine the entity type to handle items, mobs, and carcasses differently
                AlchemyRecipes.EntityType entityType =
                        AlchemyRecipes.determineEntityType(output);

                String displayName = output; // Default to the raw name

                if (entityType == AlchemyRecipes.EntityType.ITEM ||
                        entityType == AlchemyRecipes.EntityType.CARCASS) {
                    // Handle items and carcasses
                    displayName = outputItem.name();
                } else if (entityType == AlchemyRecipes.EntityType.MOB) {
                    // Handle mobs - try to get mob name
                    try {
                        Mob mob = MobFactory.mobByName(output);
                        displayName = mob.name();
                    } catch (Exception e) {
                        // If we can't get the mob name, use the raw name
                        displayName = output;
                    }
                }

                if (count > 1) {
                    description.append(displayName).append(" x").append(count);
                } else {
                    description.append(displayName);
                }
            }

            recipeDescription.text(description.toString());
        } else {
            recipeDescription.text("Select recipe");
        }

        // Update the layout to accommodate the new text
        mainLayout.layout();
    }

    private void executeSelectedRecipe() {
        Level level = hero.level();
        int pos = hero.getPos();

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

                for (Item inventoryItem : hero.getBelongings()) {
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

                hero.detachItemList(itemsToRemove);
            }

            // Process the output items and mobs
            List<String> outputs = selectedRecipe.getValue();
            for (String outputName : outputs) {
                // Determine the entity type to handle items, mobs, and carcasses differently
                AlchemyRecipes.EntityType entityType = AlchemyRecipes.determineEntityType(outputName);
                try {
                    if (entityType == AlchemyRecipes.EntityType.ITEM ||
                            entityType == AlchemyRecipes.EntityType.CARCASS) {
                        // Handle items and carcasses
                        Item outputItem = ItemFactory.itemByName(outputName);
                        level.animatedDrop(outputItem, pos);
                    } else if (entityType == AlchemyRecipes.EntityType.MOB) {
                        // Handle mobs - spawn them at the hero's position

                        Mob mob = MobFactory.mobByName(outputName);
                        int mobPos = level.getEmptyCellNextTo(pos);
                        if (level.cellValid(mobPos)) {
                            level.spawnMob(mob);
                        } else {
                            level.animatedDrop(mob.carcass(), mobPos);
                        }
                    }
                } catch (Exception e) {
                    // If there's an error spawning the mob, log it but continue
                    EventCollector.logException(e);
                }
            }
            // Close the window after executing the recipe
            hide();
        }
    }
}