package com.nyrds.pixeldungeon.windows;

import com.nyrds.pixeldungeon.alchemy.AlchemyRecipes;
import com.nyrds.pixeldungeon.items.common.ItemFactory;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.util.GuiProperties;
import com.watabou.noosa.BitmapText;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.Text;
import com.watabou.noosa.TouchArea;
import com.watabou.noosa.ui.Component;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.sprites.ItemSprite;
import com.watabou.pixeldungeon.ui.ItemSlot;
import com.watabou.pixeldungeon.ui.ScrollPane;
import com.watabou.pixeldungeon.ui.Window;
import com.watabou.pixeldungeon.ui.RedButton;
import com.nyrds.pixeldungeon.windows.HBox;
import com.nyrds.pixeldungeon.windows.VBox;
import com.watabou.utils.GameMath;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.nyrds.platform.input.Touchscreen.Touch;

/**
 * Window for displaying and executing alchemy recipes
 */
public class WndAlchemy extends Window {

    private static final int SLOT_SIZE = 24;
    private static final int MARGIN = 2;
    private static final int TITLE_COLOR = 0xFFFF44;
    private final VBox mainLayout;

    // Track selected recipe
    private Entry<List<String>, String> selectedRecipe;
    private HBox selectedRow;
    private ColorBlock selectedBackground;
    private RedButton executeButton;

    // Track recipe rows and backgrounds for selection
    private java.util.ArrayList<HBox> recipeRows = new java.util.ArrayList<>();
    private java.util.ArrayList<ColorBlock> recipeBackgrounds = new java.util.ArrayList<>();

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
                HBox recipeRow = new HBox(windowWidth - 4 * MARGIN);
                recipeRow.setGap(2);

                // Add input ingredients as text
                for (String input : recipeEntry.getKey()) {
                    // Try to create item if possible, otherwise just show text
                    String substring = input.substring(0, Math.min(input.length(), 6));
                    try {
                        Item dummyItem = ItemFactory.itemByName(input);
                        if (dummyItem != null) {
                            ItemSlot inputSlot = new ItemSlot(dummyItem);
                            inputSlot.setSize(SLOT_SIZE, SLOT_SIZE);
                            recipeRow.add(inputSlot);
                        } else {
                            // If item creation fails, show text representation
                            Text inputText = PixelScene.createText(substring, GuiProperties.smallFontSize());
                            inputText.hardlight(TITLE_COLOR);
                            recipeRow.add(inputText);
                        }
                    } catch (Exception e) {
                        // Show text representation if item creation fails
                        Text inputText = PixelScene.createText(substring, GuiProperties.smallFontSize());
                        inputText.hardlight(TITLE_COLOR);
                        recipeRow.add(inputText);
                    }
                }

                // Add arrow symbol
                Text arrow = PixelScene.createText("->", GuiProperties.regularFontSize());
                arrow.hardlight(TITLE_COLOR);
                recipeRow.add(arrow);

                // Add output item as text
                try {
                    Item outputItem = ItemFactory.itemByName(recipeEntry.getValue());
                    if (outputItem != null) {
                        ItemSlot outputSlot = new ItemSlot(outputItem);
                        outputSlot.setSize(SLOT_SIZE, SLOT_SIZE);
                        recipeRow.add(outputSlot);
                    } else {
                        // If item creation fails, show text representation
                        Text outputText = PixelScene.createText(recipeEntry.getValue().substring(0, Math.min(recipeEntry.getValue().length(), 6)), GuiProperties.smallFontSize());
                        outputText.hardlight(TITLE_COLOR);
                        recipeRow.add(outputText);
                    }
                } catch (Exception e) {
                    // Show text representation if item creation fails
                    Text outputText = PixelScene.createText(recipeEntry.getValue().substring(0, Math.min(recipeEntry.getValue().length(), 6)), GuiProperties.smallFontSize());
                    outputText.hardlight(TITLE_COLOR);
                    recipeRow.add(outputText);
                }

                // Create a custom container for the recipe row to handle clicks
                final Entry<List<String>, String> currentRecipe = recipeEntry;

                // Add the recipe row to the recipes container first to ensure proper layout
                recipesContainer.add(recipeRow);

                // Add a background color block that will serve as the clickable area
                ColorBlock background = new ColorBlock(recipeRow.width(), recipeRow.height(), 0x00000000); // Transparent initially
                // Position the background to match the recipe row
                background.setPos(recipeRow.getX(), recipeRow.getY());

                // Add the background to the recipes container
                recipesContainer.add(background);

                // Track this recipe row and background for selection
                recipeRows.add(recipeRow);
                recipeBackgrounds.add(background);
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
        ScrollPane recipeScrollPane = new ScrollPane(recipesContainer) {
            @Override
            public void onClick(float x, float y) {
                // Iterate through the recipe rows to find which one was clicked
                for (int i = 0; i < recipeRows.size(); i++) {
                    HBox recipeRow = recipeRows.get(i);
                    ColorBlock background = recipeBackgrounds.get(i);

                    // Check if the click is within the bounds of this recipe row
                    if (recipeRow.inside(x, y)) {
                        // Deselect previous selection
                        if (selectedRow != null && selectedBackground != null) {
                            selectedBackground.color(0x00000000); // Transparent
                        }

                        // Select current row
                        selectedRow = recipeRow;
                        selectedBackground = background;

                        // Find the corresponding recipe entry for this row
                        // We need to iterate through the original allRecipes to find the right one
                        int currentIndex = 0;
                        for (Entry<List<String>, String> recipeEntry : allRecipes.entrySet()) {
                            if (currentIndex == i) {
                                selectedRecipe = recipeEntry;
                                break;
                            }
                            currentIndex++;
                        }

                        // Highlight the selected row
                        background.color(0x88888888); // Gray tint for selection

                        // Update button to reflect selection
                        updateExecuteButton();
                        break;
                    }
                }
            }
        };
        recipeScrollPane.scrollTo(0,0);
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

    private void updateExecuteButton() {
        // Find the execute button and enable/disable it based on selection
        // We'll store a reference to the execute button instead of searching each time
        executeButton.enable(selectedRecipe != null);
    }

    private void executeSelectedRecipe() {
        if (selectedRecipe != null) {
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