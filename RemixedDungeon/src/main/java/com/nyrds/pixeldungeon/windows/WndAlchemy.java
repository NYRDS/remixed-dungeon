package com.nyrds.pixeldungeon.windows;

import com.nyrds.pixeldungeon.alchemy.AlchemyRecipes;
import com.nyrds.pixeldungeon.items.common.ItemFactory;
import com.nyrds.util.GuiProperties;
import com.watabou.noosa.Text;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.ui.ItemSlot;
import com.watabou.pixeldungeon.ui.RedButton;
import com.watabou.pixeldungeon.ui.ScrollPane;
import com.watabou.pixeldungeon.ui.Window;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Simple window for displaying alchemy recipes
 */
public class WndAlchemy extends Window {

    private static final int WIDTH = 120;
    private static final int SLOT_SIZE = 24;
    private static final int MARGIN = 2;
    private static final int TITLE_COLOR = 0xFFFF44;
    private final VBox mainLayout;

    public WndAlchemy() {
        super();

        // Main layout
        mainLayout = new VBox();
        mainLayout.setRect(0, 0, WIDTH, 0);
        add(mainLayout);

        // Title
        Text title = PixelScene.createText("Alchemy Recipes", GuiProperties.titleFontSize());
        title.hardlight(TITLE_COLOR);
        title.setX(MARGIN);
        mainLayout.add(title);

        // Recipes container
        VBox recipesContainer = new VBox();
        recipesContainer.setRect(0, 0, WIDTH - 2 * MARGIN, 0);
        recipesContainer.setGap(2);

        Map<List<String>, String> allRecipes = AlchemyRecipes.getAllRecipes();
        if (!allRecipes.isEmpty()) {
            for (Entry<List<String>, String> recipeEntry : allRecipes.entrySet()) {
                HBox recipeRow = new HBox(WIDTH - 2 * MARGIN);
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

                recipesContainer.add(recipeRow);
            }
        } else {
            // If no recipes are available, show a message
            Text noRecipesText = PixelScene.createText("No recipes available", GuiProperties.regularFontSize());
            noRecipesText.hardlight(TITLE_COLOR);
            recipesContainer.add(noRecipesText);
        }

        // Add scroll pane for recipes
        ScrollPane recipeScrollPane = new ScrollPane(recipesContainer);
        recipeScrollPane.scrollTo(0,0);
        recipeScrollPane.setRect(0, 0, WIDTH - 2 * MARGIN, 80); // Fixed height for scrollable area
        recipeScrollPane.measure();
        mainLayout.add(recipeScrollPane);

        // Close button
        RedButton closeButton = new RedButton("Close") {
            @Override
            protected void onClick() {
                hide();
            }
        };
        closeButton.setSize(40, 18);
        mainLayout.add(closeButton);

        // Update the layout
        mainLayout.layout();
        resize(WIDTH, (int) mainLayout.bottom() + 2);
        //mainLayout.layout();

    }


}