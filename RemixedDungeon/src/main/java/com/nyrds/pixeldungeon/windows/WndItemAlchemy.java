package com.nyrds.pixeldungeon.windows;

import com.nyrds.pixeldungeon.alchemy.AlchemyRecipe;
import com.nyrds.pixeldungeon.alchemy.AlchemyRecipes;
import com.nyrds.pixeldungeon.alchemy.OutputItem;
import com.nyrds.pixeldungeon.items.common.ItemFactory;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.mobs.common.MobFactory;
import com.nyrds.platform.EventCollector;
import com.nyrds.platform.game.RemixedDungeon;
import com.nyrds.util.GuiProperties;
import com.watabou.noosa.Text;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.ui.RedButton;
import com.watabou.pixeldungeon.ui.ScrollPane;
import com.watabou.pixeldungeon.ui.Window;
import com.watabou.pixeldungeon.utils.Utils;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Window for displaying and executing alchemy recipes that include a specific item
 */
public class WndItemAlchemy extends Window {

    private static final int MARGIN = 2;
    private static final int SMALL_GAP = 1;
    private final VBox mainLayout;

    // Track selected recipe
    private AlchemyRecipe selectedRecipe;
    private RecipeListItem selectedRow;
    private final RedButton executeButton;

    // Track recipe rows for selection
    private final ArrayList<RecipeListItem> recipeRows = new ArrayList<>();

    // Recipe description text
    private final Text recipeDescription;
    final private Char hero;

    public WndItemAlchemy(Item baseItem, @NotNull Char chr) {
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
        Text title = PixelScene.createMultiline(Utils.format(R.string.WndItemAlchemy_Title, baseItem.name()), GuiProperties.titleFontSize());
        title.maxWidth((int) windowWidth);
        title.hardlight(Window.TITLE_COLOR);
        title.setX(MARGIN);
        mainLayout.add(title);

        // Calculate player's inventory
        Map<String, Integer> playerInventory = new HashMap<>();
        for (Item item : hero.getBelongings()) {
            if(item.isIdentified()) {
                String itemName = item.getEntityKind();
                playerInventory.put(itemName, playerInventory.getOrDefault(itemName, 0) + item.quantity());
            }
        }

        // Get recipes that contain this specific baseItem
        var recipesWithItem =
                AlchemyRecipes.getRecipesContainingItem(baseItem.getEntityKind());

        // Filter to only show recipes for which the player has all required ingredients
        List<AlchemyRecipe> availableRecipes = new ArrayList<>();
        for (var recipe : recipesWithItem) {
            if (AlchemyRecipes.hasRequiredIngredients(recipe.getInput(), playerInventory)) {
                availableRecipes.add(recipe);
            }
        }

        // Recipes container
        VBox recipesContainer = new VBox();
        recipesContainer.setRect(0, 0, windowWidth - 4 * MARGIN, 0);
        recipesContainer.setGap(SMALL_GAP); // Reduced gap for tighter fit

        if (availableRecipes.isEmpty()) {
            Text noRecipes = PixelScene.createMultiline(R.string.WndItemAlchemy_NoRecipes, 8);
            noRecipes.maxWidth((int) (windowWidth - 4 * MARGIN));
            mainLayout.add(noRecipes);
        } else {
            for (var recipeEntry : availableRecipes) {
                // Create a recipe list baseItem
                RecipeListItem recipeItem = getRecipeListItem(recipeEntry, windowWidth);

                // Add the recipe baseItem to the recipes container
                recipesContainer.add(recipeItem);

                // Track this recipe baseItem for selection
                recipeRows.add(recipeItem);
            }

            // Add scroll pane for recipes - limit height for better layout
            float scrollHeight =  windowHeight - 60;

            ScrollPane recipeScrollPane = createScrollPane(recipesContainer, availableRecipes);
            recipeScrollPane.setRect(0, 0, windowWidth, scrollHeight);
            recipeScrollPane.measure();
            mainLayout.add(recipeScrollPane);
        }


        VBox controlsBox = new VBox();

        // Recipe description area
        recipeDescription = PixelScene.createMultiline(R.string.WndItemAlchemy_SelectRecipe, GuiProperties.regularFontSize());
        recipeDescription.maxWidth((int) (windowWidth - 4 * MARGIN));
        //recipeDescription.minHeight(60);
        controlsBox.add(recipeDescription);

        // Execute and Close buttons container
        HBox buttonsContainer = new HBox(windowWidth - 2 * MARGIN);
        buttonsContainer.setAlign(HBox.Align.Width);
        buttonsContainer.setGap(SMALL_GAP); // Reduced gap for tighter fit

        // Execute button
        executeButton = new RedButton(R.string.WndItemAlchemy_ExecuteRecipe) {
            @Override
            protected void onClick() {
                executeSelectedRecipe();
            }
        };
        executeButton.autoSize();

        executeButton.enable(selectedRecipe != null);
        buttonsContainer.add(executeButton);

        // Close button
        RedButton closeButton = new RedButton(R.string.Wnd_Button_Close) {
            @Override
            protected void onClick() {
                hide();
            }
        };

        closeButton.autoSize();
        buttonsContainer.add(closeButton);

        // Add the buttons container to the main layout
        controlsBox.add(buttonsContainer);
        controlsBox.setMaxHeight(40);
        controlsBox.setAlign(VBox.Align.Height);
        controlsBox.layout();

        mainLayout.add(controlsBox);

        mainLayout.layout();
        resize((int) windowWidth, (int) windowHeight);
    }

    private RecipeListItem getRecipeListItem(AlchemyRecipe recipeEntry, float windowWidth) {
        RecipeListItem recipeItem = new RecipeListItem(recipeEntry.getInput(), recipeEntry.getOutput());
        recipeItem.setSelected(false);

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

    private ScrollPane createScrollPane(VBox recipesContainer, List<AlchemyRecipe> availableRecipes) {
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
            var inputs = selectedRecipe.getInput();
            var outputs = selectedRecipe.getOutput();

            StringBuilder description = new StringBuilder();
            description.append("Recipe: ");

            // Add input ingredients with counts
            boolean first = true;
            for (var inputItem : inputs) {
                if (!first) {
                    description.append(" + ");
                }
                first = false;

                String input = inputItem.getName();
                Item inputItemInstance = ItemFactory.itemByName(input);
                input = input.toLowerCase();
                int count = inputItem.getCount();

                String color = "green";
                if (input.contains("remains")) {
                    color = "red";
                }

                description.append(Text.color(inputItemInstance.name(), color));
                if (count > 1) {
                    description.append(" x").append(count);
                }
            }

            description.append(" = ");

            // Add output items with counts
            first = true;
            for (var outputItem : outputs) {
                if (!first) {
                    description.append(" + ");
                }
                first = false;

                String output = outputItem.getName();
                String lower_output = output.toLowerCase();
                int count = outputItem.getCount();

                // Determine the entity type to handle items, mobs, and carcasses differently
                AlchemyRecipes.EntityType entityType =
                        AlchemyRecipes.determineEntityType(output);

                String displayName = output; // Default to the raw name

                String color = "green";
                if (lower_output.contains("remains")) {
                    color = "red";
                }

                if (entityType == AlchemyRecipes.EntityType.ITEM ||
                        entityType == AlchemyRecipes.EntityType.CARCASS) {
                    Item outputItemInstance = ItemFactory.itemByName(output);
                    // Handle items and carcasses
                    displayName = outputItemInstance.name();

                } else if (entityType == AlchemyRecipes.EntityType.MOB) {
                    Mob mob = MobFactory.mobByName(output);
                    displayName = mob.name();
                    color = "purple";
                }

                description.append(Text.color(displayName.toLowerCase(), color));
                if (count > 1) {
                    description.append(" x").append(count);
                }
            }

            recipeDescription.text(description.toString());
        } else {
            recipeDescription.text(R.string.WndItemAlchemy_SelectRecipe);
        }

        // Update the layout to accommodate the new text
        mainLayout.layout();
    }

    private void executeSelectedRecipe() {
        Level level = hero.level();
        int pos = hero.getPos();

        if (selectedRecipe != null) {
            // Remove the required ingredients from the player's inventory
            var inputs = selectedRecipe.getInput();

            // Remove ingredients from inventory
            for (var inputItem : inputs) {
                String ingredientName = inputItem.getName();
                int requiredQty = inputItem.getCount();

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
            List<OutputItem> outputs = selectedRecipe.getOutput();
            for (OutputItem outputItem : outputs) {
                String outputName = outputItem.getName();
                int outputCount = outputItem.getCount();
                
                // Determine the entity type to handle items, mobs, and carcasses differently
                AlchemyRecipes.EntityType entityType = AlchemyRecipes.determineEntityType(outputName);
                try {
                    if (entityType == AlchemyRecipes.EntityType.ITEM ||
                            entityType == AlchemyRecipes.EntityType.CARCASS) {
                        // Handle items and carcasses
                        for (int i = 0; i < outputCount; i++) {
                            Item outputItemInstance = ItemFactory.itemByName(outputName);
                            level.animatedDrop(outputItemInstance, pos);
                        }
                    } else if (entityType == AlchemyRecipes.EntityType.MOB) {
                        // Handle mobs - spawn them at the hero's position
                        for (int i = 0; i < outputCount; i++) {
                            Mob mob = MobFactory.mobByName(outputName);
                            int mobPos = level.getEmptyCellNextTo(pos);
                            if (level.cellValid(mobPos)) {
                                mob.setPos(mobPos);
                                mob.makePet(hero);
                                level.spawnMob(mob, -1, pos);
                            } else {
                                level.animatedDrop(mob.carcass(), pos);
                            }
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