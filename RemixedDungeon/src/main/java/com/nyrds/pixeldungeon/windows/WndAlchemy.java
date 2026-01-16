package com.nyrds.pixeldungeon.windows;

import com.nyrds.pixeldungeon.alchemy.AlchemyRecipes;
import com.nyrds.pixeldungeon.game.GameLoop;
import com.nyrds.util.GuiProperties;
import com.watabou.noosa.Gizmo;
import com.watabou.noosa.Group;
import com.watabou.noosa.Text;
import com.watabou.noosa.TouchArea;
import com.watabou.noosa.particles.Emitter;
import com.watabou.noosa.ui.Button;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.effects.particles.AlchemyParticle;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.ui.RedButton;
import com.watabou.pixeldungeon.ui.ScrollPane;
import com.watabou.pixeldungeon.ui.Window;
import com.nyrds.platform.input.Touchscreen.Touch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Window for displaying and executing alchemy recipes
 */
public class WndAlchemy extends Window {

    private static final int MARGIN = 2;
    private static final int TITLE_COLOR = 0xFFFF44;
    private final VBox mainLayout;

    // Track selected recipe
    private Entry<List<String>, List<String>> selectedRecipe;
    private RecipeListItem selectedRow;
    private RedButton executeButton;

    // Track recipe rows for selection
    private ArrayList<RecipeListItem> recipeRows = new ArrayList<>();

    // Transmutation circle for alchemy effects
    private TransmutationCircle transmutationCircle;

    public WndAlchemy() {
        super();

        // Calculate almost fullscreen dimensions
        float screenWidth = PixelScene.uiCamera.width;
        float screenHeight = PixelScene.uiCamera.height;

        // Leave some margin around the window
        float windowWidth = screenWidth - 20; // 10px margin on each side
        float windowHeight = screenHeight - 20; // 10px margin on each side

        // Main layout
        mainLayout = new VBox();
        mainLayout.setRect(MARGIN, MARGIN, windowWidth - 2 * MARGIN, 0);
        add(mainLayout);

        // Title
        Text title = PixelScene.createText("Alchemy Recipes", GuiProperties.titleFontSize());
        title.hardlight(TITLE_COLOR);
        title.setX(MARGIN);
        mainLayout.add(title);

        // Recipes container
        VBox recipesContainer = new VBox();
        recipesContainer.setRect(0, 0, windowWidth - 4 * MARGIN, 0);
        recipesContainer.setGap(0);

        var allRecipes = AlchemyRecipes.getAllRecipes();

        for (var recipeEntry : allRecipes.entrySet()) {
            // Create a recipe list item
            RecipeListItem recipeItem = getRecipeListItem(recipeEntry, windowWidth);

            // Add the recipe item to the recipes container
            recipesContainer.add(recipeItem);

            // Track this recipe item for selection
            recipeRows.add(recipeItem);
        }


        // Add scroll pane for recipes with almost fullscreen height
        float scrollHeight = windowHeight - mainLayout.childsHeight() - 40; // Account for title, padding, and buttons

        // Create the scroll pane and override the onClick method to handle recipe selection


        ScrollPane recipeScrollPane = createScrollPane(recipesContainer, allRecipes);
        recipeScrollPane.setRect(0, 0, windowWidth, scrollHeight); // Almost fullscreen height for scrollable area
        recipeScrollPane.measure();
        mainLayout.add(recipeScrollPane);

        // Execute and Close buttons container
        HBox buttonsContainer = new HBox(windowWidth - 2 * MARGIN);
        buttonsContainer.setAlign(HBox.Align.Width);

        // Execute button
        executeButton = new RedButton("Execute Recipe") {
            @Override
            protected void onClick() {
                executeSelectedRecipe();
            }
        };
        executeButton.setSize(Math.min(80, windowWidth/4), 18);
        executeButton.enable(false); // Initially disabled until a recipe is selected
        buttonsContainer.add(executeButton);

        // Close button
        RedButton closeButton = new RedButton("Close") {
            @Override
            protected void onClick() {
                hide();
            }
        };
        closeButton.setSize(Math.min(60, windowWidth/5), 18);
        buttonsContainer.add(closeButton);

        // Add transmutation circle for alchemy effects on background FIRST (so it's behind other elements)
        setupTransmutationCircle(windowWidth, windowHeight);

        mainLayout.add(buttonsContainer);

        // Update the layout
        mainLayout.layout();

        resize((int)windowWidth, (int)windowHeight);
    }

    private void setupTransmutationCircle(float windowWidth, float windowHeight) {
        // Create the transmutation circle
        transmutationCircle = new TransmutationCircle();
        transmutationCircle.setSize(windowWidth, windowHeight);
        transmutationCircle.setRecipeSeed("initial"); // Set initial seed
        // Add it to the window first so it appears behind other elements
        add(transmutationCircle);
        bringToFront(transmutationCircle);
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

            // Update the transmutation circle with the selected recipe as seed
            if (transmutationCircle != null) {
                // Create a seed string from the recipe ingredients
                StringBuilder seedBuilder = new StringBuilder();
                for (String ingredient : recipeEntry.getKey()) {
                    seedBuilder.append(ingredient).append(":");
                }
                for (String output : recipeEntry.getValue()) {
                    seedBuilder.append(output).append(":");
                }
                transmutationCircle.setRecipeSeed(seedBuilder.toString());
            }
        });
        return recipeItem;
    }

    private ScrollPane createScrollPane(VBox recipesContainer, Map<List<String>, List<String>> allRecipes) {
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
                        Object[] recipeArray = allRecipes.entrySet().toArray();
                        selectedRecipe = (Entry<List<String>, List<String>>) recipeArray[i];

                        // Highlight the selected row
                        selectedRow.setSelected(true);

                        // Update button to reflect selection
                        updateExecuteButton();

                        // Update the transmutation circle with the selected recipe as seed
                        if (transmutationCircle != null) {
                            // Create a seed string from the recipe ingredients
                            StringBuilder seedBuilder = new StringBuilder();
                            for (String ingredient : selectedRecipe.getKey()) {
                                seedBuilder.append(ingredient).append(":");
                            }
                            for (String output : selectedRecipe.getValue()) {
                                seedBuilder.append(output).append(":");
                            }
                            transmutationCircle.setRecipeSeed(seedBuilder.toString());
                        }
                        break;
                    }
                }
            }
        };
        recipeScrollPane.scrollTo(0,0);
        return recipeScrollPane;
    }

    private void updateExecuteButton() {
        // Find the execute button and enable/disable it based on selection
        // We'll store a reference to the execute button instead of searching each time
        executeButton.enable(selectedRecipe != null);
    }

    private void executeSelectedRecipe() {
        if (selectedRecipe != null) {
            // Get all output types for this recipe
            java.util.List<AlchemyRecipes.OutputType> outputTypes = AlchemyRecipes.getOutputTypeForInput(selectedRecipe.getKey());

            // Check if there are any item outputs
            boolean hasItemOutput = outputTypes.stream().anyMatch(type -> type == AlchemyRecipes.OutputType.ITEM);
            // Check if there are any mob outputs
            boolean hasMobOutput = outputTypes.stream().anyMatch(type -> type == AlchemyRecipes.OutputType.MOB);

            if (hasItemOutput && hasMobOutput) {
                // Handle mixed outputs (both items and mobs)
                executeMixedRecipe();
            } else if (hasItemOutput) {
                // Handle item-only outputs
                executeItemRecipe();
            } else if (hasMobOutput) {
                // Handle mob-only outputs
                executeMobRecipe();
            } else {
                // Unknown output type
                showMessageWindow("Failed to execute recipe:\nUnknown output type!", 0xFF4444, false);
            }
        }
    }

    private void executeItemRecipe() {
        // Get all outputs for this recipe
        var outputs = AlchemyRecipes.getOutputForInput(selectedRecipe.getKey());
        var outputTypes = AlchemyRecipes.getOutputTypeForInput(selectedRecipe.getKey());

        // Collect all item outputs
        List<Item> outputItems = new ArrayList<>();
        for (int i = 0; i < outputs.size(); i++) {
            if (outputTypes.get(i) == AlchemyRecipes.OutputType.ITEM) {
                Item item = AlchemyRecipes.createOutputItem(Collections.singletonList(outputs.get(i)));
                if (item != null) {
                    outputItems.add(item);
                }
            }
        }

        if (!outputItems.isEmpty()) {
            boolean allAdded = true;
            for (Item outputItem : outputItems) {
                if (!outputItem.collect(Dungeon.hero)) {
                    allAdded = false;
                    break;
                }
            }

            if (allAdded) {
                // All items were successfully added to inventory
                showMessageWindow("Recipe executed successfully!\nOutputs: " + String.join(", ", outputs), 0x44FF44, true);
            } else {
                // At least one item couldn't be added (inventory full)
                showMessageWindow("Recipe executed successfully, but some items couldn't be added due to full inventory!", 0xFFAA44, true);
            }
        } else {
            // Failed to create any output items
            showMessageWindow("Failed to execute recipe:\nCould not create output items!", 0xFF4444, false);
        }
    }

    private void executeMobRecipe() {
        // Get all outputs for this recipe
        var outputs = AlchemyRecipes.getOutputForInput(selectedRecipe.getKey());
        var outputTypes = AlchemyRecipes.getOutputTypeForInput(selectedRecipe.getKey());

        // Collect all mob outputs
        List<Mob> outputMobs = new ArrayList<>();
        for (int i = 0; i < outputs.size(); i++) {
            if (outputTypes.get(i) == AlchemyRecipes.OutputType.MOB) {
                Mob mob = AlchemyRecipes.createOutputMob(Collections.singletonList(outputs.get(i)));
                if (mob != null) {
                    outputMobs.add(mob);
                }
            }
        }

        if (!outputMobs.isEmpty()) {
            // Try to spawn all mobs near the hero
            boolean allSpawned = true;
            for (Mob outputMob : outputMobs) {
                int spawnPos = Dungeon.level.getEmptyCellNextTo(Dungeon.hero.pos);
                if (spawnPos != -1) {
                    outputMob.pos = spawnPos;
                    Dungeon.level.spawnMob(outputMob);
                } else {
                    allSpawned = false;
                }
            }

            if (allSpawned) {
                // All mobs were successfully spawned
                showMessageWindow("Recipe executed successfully!\nSpawned: " + String.join(", ", outputs), 0x44FF44, true);
            } else {
                // At least one mob couldn't be spawned (no space)
                showMessageWindow("Recipe executed successfully, but some mobs couldn't be spawned due to lack of space!", 0xFFAA44, true);
            }
        } else {
            // Failed to create any output mobs
            showMessageWindow("Failed to execute recipe:\nCould not create output mobs!", 0xFF4444, false);
        }
    }

    private void executeMixedRecipe() {
        // Get all outputs for this recipe
        var outputs = AlchemyRecipes.getOutputForInput(selectedRecipe.getKey());
        var outputTypes = AlchemyRecipes.getOutputTypeForInput(selectedRecipe.getKey());

        // Collect all item and mob outputs
        List<Item> outputItems = new ArrayList<>();
        List<Mob> outputMobs = new ArrayList<>();

        for (int i = 0; i < outputs.size(); i++) {
            if (outputTypes.get(i) == AlchemyRecipes.OutputType.ITEM) {
                Item item = AlchemyRecipes.createOutputItem(Collections.singletonList(outputs.get(i)));
                if (item != null) {
                    outputItems.add(item);
                }
            } else if (outputTypes.get(i) == AlchemyRecipes.OutputType.MOB) {
                Mob mob = AlchemyRecipes.createOutputMob(Collections.singletonList(outputs.get(i)));
                if (mob != null) {
                    outputMobs.add(mob);
                }
            }
        }

        boolean itemsAdded = true;
        boolean mobsSpawned = true;

        // Add items to inventory
        if (!outputItems.isEmpty()) {
            for (Item outputItem : outputItems) {
                if (!outputItem.collect(Dungeon.hero)) {
                    itemsAdded = false;
                    break;
                }
            }
        }

        // Spawn mobs
        if (!outputMobs.isEmpty()) {
            for (Mob outputMob : outputMobs) {
                int spawnPos = Dungeon.level.getEmptyCellNextTo(Dungeon.hero.pos);
                if (spawnPos != -1) {
                    outputMob.pos = spawnPos;
                    Dungeon.level.spawnMob(outputMob);
                } else {
                    mobsSpawned = false;
                    break;
                }
            }
        }

        // Show appropriate message based on results
        if (itemsAdded && mobsSpawned) {
            showMessageWindow("Recipe executed successfully!\nItems: " + outputItems.size() + " | Mobs: " + outputMobs.size(), 0x44FF44, true);
        } else if (itemsAdded && !mobsSpawned) {
            showMessageWindow("Recipe executed partially!\nItems added but some mobs couldn't be spawned due to lack of space!", 0xFFAA44, true);
        } else if (!itemsAdded && mobsSpawned) {
            showMessageWindow("Recipe executed partially!\nMobs spawned but some items couldn't be added due to full inventory!", 0xFFAA44, true);
        } else {
            showMessageWindow("Recipe executed partially!\nSome items couldn't be added and some mobs couldn't be spawned!", 0xFFAA44, true);
        }
    }

    private void showMessageWindow(String messageText, int color, boolean closeParent) {
        GameScene.show(new Window() {
            {
                Text message = PixelScene.createText(messageText, GuiProperties.regularFontSize());
                message.hardlight(color);
                add(message);

                RedButton okButton = new RedButton("OK") {
                    @Override
                    protected void onClick() {
                        hide();
                        if (closeParent) {
                            hide(); // Close the parent window too
                        }
                    }
                };
                okButton.setSize(40, 18);
                add(okButton);

                float msgWidth = Math.max(message.width() + 10, 120);
                float msgHeight = message.height() + 28;

                message.setPos(5, 5);
                okButton.setPos((msgWidth - okButton.width()) / 2, message.height() + 10);

                resize((int)msgWidth, (int)msgHeight);
            }
        });
    }

    @Override
    public void update() {
        super.update();
        // Update the layout
        mainLayout.layout();
        float windowHeight = PixelScene.uiCamera.height - 20; // 10px margin on each side
        resize(PixelScene.uiCamera.width - 20, (int)windowHeight);
    }

    @Override
    public void layout() {
        super.layout();

        // Calculate almost fullscreen dimensions
        float screenWidth = PixelScene.uiCamera.width;
        float screenHeight = PixelScene.uiCamera.height;

        // Leave some margin around the window
        float windowWidth = screenWidth - 20; // 10px margin on each side
        float windowHeight = screenHeight - 20; // 10px margin on each side

        // Update the layout
        mainLayout.setRect(MARGIN, MARGIN, windowWidth - 2 * MARGIN, 0);
        mainLayout.layout();

        // Update transmutation circle size based on new window size
        if (transmutationCircle != null) {
            transmutationCircle.setSize(windowWidth, windowHeight);
        }

        resize((int)windowWidth, (int)windowHeight);
    }
}