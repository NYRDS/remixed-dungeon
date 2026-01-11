package com.nyrds.pixeldungeon.windows;

import com.nyrds.pixeldungeon.alchemy.AlchemyRecipes;
import com.nyrds.util.GuiProperties;
import com.watabou.noosa.Text;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.ui.RedButton;
import com.watabou.pixeldungeon.ui.ScrollPane;
import com.watabou.pixeldungeon.ui.Window;

import java.util.ArrayList;
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
    private Entry<List<String>, String> selectedRecipe;
    private RecipeListItem selectedRow;
    private RedButton executeButton;

    // Track recipe rows for selection
    private ArrayList<RecipeListItem> recipeRows = new ArrayList<>();

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
        recipesContainer.setGap(2);

        Map<List<String>, String> allRecipes = AlchemyRecipes.getAllRecipes();
        if (!allRecipes.isEmpty()) {
            for (Entry<List<String>, String> recipeEntry : allRecipes.entrySet()) {
                // Create a recipe list item
                RecipeListItem recipeItem = getRecipeListItem(recipeEntry, windowWidth);

                // Add the recipe item to the recipes container
                recipesContainer.add(recipeItem);

                // Track this recipe item for selection
                recipeRows.add(recipeItem);
            }
        } else {
            // If no recipes are available, show a message
            Text noRecipesText = PixelScene.createText("No recipes available", GuiProperties.regularFontSize());
            noRecipesText.hardlight(TITLE_COLOR);
            recipesContainer.add(noRecipesText);
        }

        // Add scroll pane for recipes with almost fullscreen height
        float scrollHeight = windowHeight - mainLayout.childsHeight() - 60; // Account for title, padding, and buttons

        // Create the scroll pane and override the onClick method to handle recipe selection


        ScrollPane recipeScrollPane = createScrollPane(recipesContainer, allRecipes);
        recipeScrollPane.setRect(0, 0, windowWidth - 4 * MARGIN, scrollHeight); // Almost fullscreen height for scrollable area
        recipeScrollPane.measure();
        mainLayout.add(recipeScrollPane);

        // Execute and Close buttons container
        HBox buttonsContainer = new HBox(windowWidth - 2 * MARGIN);
        buttonsContainer.setGap(5);

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

        mainLayout.add(buttonsContainer);

        // Update the layout
        mainLayout.layout();
        resize((int)windowWidth, (int)windowHeight);
    }

    private RecipeListItem getRecipeListItem(Entry<List<String>, String> recipeEntry, float windowWidth) {
        RecipeListItem recipeItem = new RecipeListItem(recipeEntry.getKey(), recipeEntry.getValue());

        // Set the size for the recipe item
        recipeItem.setSize(windowWidth - 4 * MARGIN, 30); // Height can be adjusted as needed

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
        });
        return recipeItem;
    }

    private ScrollPane createScrollPane(VBox recipesContainer, Map<List<String>, String> allRecipes) {
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
                        selectedRecipe = allRecipes.entrySet().toArray(new Entry[0])[i];

                        // Highlight the selected row
                        selectedRow.setSelected(true);

                        // Update button to reflect selection
                        updateExecuteButton();
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
            // Determine the output type (item or mob)
            AlchemyRecipes.OutputType outputType = AlchemyRecipes.getOutputTypeForInput(selectedRecipe.getKey());

            if (outputType == AlchemyRecipes.OutputType.ITEM) {
                // Create the output item based on the recipe
                Item outputItem = AlchemyRecipes.createOutputItem(selectedRecipe.getKey());

                if (outputItem != null) {
                    // Add the item to the player's inventory
                    if (outputItem.collect(Dungeon.hero)) {
                        // Item was successfully added to inventory
                        GameScene.show(new Window() {
                            {
                                Text message = PixelScene.createText("Recipe executed successfully!\nOutput: " + selectedRecipe.getValue(), GuiProperties.regularFontSize());
                                message.hardlight(0x44FF44); // Green color for success
                                add(message);

                                RedButton okButton = new RedButton("OK") {
                                    @Override
                                    protected void onClick() {
                                        hide();
                                        hide(); // Close the parent window too
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
                    } else {
                        // Inventory is full
                        GameScene.show(new Window() {
                            {
                                Text message = PixelScene.createText("Cannot execute recipe:\nInventory is full!", GuiProperties.regularFontSize());
                                message.hardlight(0xFF4444); // Red color for error
                                add(message);

                                RedButton okButton = new RedButton("OK") {
                                    @Override
                                    protected void onClick() {
                                        hide();
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
                } else {
                    // Failed to create output item
                    GameScene.show(new Window() {
                        {
                            Text message = PixelScene.createText("Failed to execute recipe:\nCould not create output item!", GuiProperties.regularFontSize());
                            message.hardlight(0xFF4444); // Red color for error
                            add(message);

                            RedButton okButton = new RedButton("OK") {
                                @Override
                                protected void onClick() {
                                    hide();
                                }
                            };
                            okButton.setSize(40, 18);
                            add(okButton);

                            float msgWidth = Math.max(message.width() + 10, 150);
                            float msgHeight = message.height() + 28;

                            message.setPos(5, 5);
                            okButton.setPos((msgWidth - okButton.width()) / 2, message.height() + 10);

                            resize((int)msgWidth, (int)msgHeight);
                        }
                    });
                }
            } else if (outputType == AlchemyRecipes.OutputType.MOB) {
                // Create the output mob based on the recipe
                Mob outputMob = AlchemyRecipes.createOutputMob(selectedRecipe.getKey());

                if (outputMob != null) {
                    // Spawn the mob near the hero
                    int spawnPos = Dungeon.level.getEmptyCellNextTo(Dungeon.hero.pos);
                    if (spawnPos != -1) {
                        outputMob.pos = spawnPos;

                        Dungeon.level.spawnMob(outputMob);

                        // Show success message
                        GameScene.show(new Window() {
                            {
                                Text message = PixelScene.createText("Recipe executed successfully!\nSpawned: " + selectedRecipe.getValue(), GuiProperties.regularFontSize());
                                message.hardlight(0x44FF44); // Green color for success
                                add(message);

                                RedButton okButton = new RedButton("OK") {
                                    @Override
                                    protected void onClick() {
                                        hide();
                                        hide(); // Close the parent window too
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
                    } else {
                        // Could not find a position to spawn the mob
                        GameScene.show(new Window() {
                            {
                                Text message = PixelScene.createText("Cannot execute recipe:\nNo space to spawn mob!", GuiProperties.regularFontSize());
                                message.hardlight(0xFF4444); // Red color for error
                                add(message);

                                RedButton okButton = new RedButton("OK") {
                                    @Override
                                    protected void onClick() {
                                        hide();
                                    }
                                };
                                okButton.setSize(40, 18);
                                add(okButton);

                                float msgWidth = Math.max(message.width() + 10, 150);
                                float msgHeight = message.height() + 28;

                                message.setPos(5, 5);
                                okButton.setPos((msgWidth - okButton.width()) / 2, message.height() + 10);

                                resize((int)msgWidth, (int)msgHeight);
                            }
                        });
                    }
                } else {
                    // Failed to create output mob
                    GameScene.show(new Window() {
                        {
                            Text message = PixelScene.createText("Failed to execute recipe:\nCould not create output mob!", GuiProperties.regularFontSize());
                            message.hardlight(0xFF4444); // Red color for error
                            add(message);

                            RedButton okButton = new RedButton("OK") {
                                @Override
                                protected void onClick() {
                                    hide();
                                }
                            };
                            okButton.setSize(40, 18);
                            add(okButton);

                            float msgWidth = Math.max(message.width() + 10, 150);
                            float msgHeight = message.height() + 28;

                            message.setPos(5, 5);
                            okButton.setPos((msgWidth - okButton.width()) / 2, message.height() + 10);

                            resize((int)msgWidth, (int)msgHeight);
                        }
                    });
                }
            } else {
                // Unknown output type
                GameScene.show(new Window() {
                    {
                        Text message = PixelScene.createText("Failed to execute recipe:\nUnknown output type!", GuiProperties.regularFontSize());
                        message.hardlight(0xFF4444); // Red color for error
                        add(message);

                        RedButton okButton = new RedButton("OK") {
                            @Override
                            protected void onClick() {
                                hide();
                            }
                        };
                        okButton.setSize(40, 18);
                        add(okButton);

                        float msgWidth = Math.max(message.width() + 10, 150);
                        float msgHeight = message.height() + 28;

                        message.setPos(5, 5);
                        okButton.setPos((msgWidth - okButton.width()) / 2, message.height() + 10);

                        resize((int)msgWidth, (int)msgHeight);
                    }
                });
            }
        }
    }

    @Override
    public void update() {
        super.update();
        // Update the layout
        mainLayout.layout();
        float windowHeight = PixelScene.uiCamera.height - 20; // 10px margin on each side
        resize((int)(PixelScene.uiCamera.width - 20), (int)windowHeight);
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
        resize((int)windowWidth, (int)windowHeight);
    }
}