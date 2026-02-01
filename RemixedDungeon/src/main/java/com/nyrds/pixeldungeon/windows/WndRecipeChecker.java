package com.nyrds.pixeldungeon.windows;

import com.nyrds.pixeldungeon.alchemy.AlchemyRecipe;
import com.nyrds.pixeldungeon.alchemy.AlchemyRecipes;
import com.watabou.noosa.ui.Component;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.ui.ScrollPane;
import com.watabou.pixeldungeon.ui.Window;
import com.watabou.noosa.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WndRecipeChecker extends Window {

    private static final int WIDTH_MIN = 120;
    private static final int MARGIN = 2;
    private static final int ITEM_HEIGHT = 25;

    public WndRecipeChecker() {
        super();

        // Calculate player's inventory
        Map<String, Integer> playerInventory = new HashMap<>();
        // Access the hero's belongings through the proper public method
        for (Item item : Dungeon.hero.getBelongings().backpack.items) {
            String itemName = item.getClass().getSimpleName();
            playerInventory.put(itemName, playerInventory.getOrDefault(itemName, 0) + item.quantity());
        }

        // Get available recipes
        List<AlchemyRecipe> availableRecipes = AlchemyRecipes.getAvailableRecipes(playerInventory);

        // Create title
        Text title = PixelScene.createMultiline("Available Recipes", 9);
        title.hardlight(TITLE_COLOR);
        add(title);

        // Position title
        title.x = MARGIN;
        title.y = MARGIN;

        float pos = title.y + title.height() + MARGIN;

        if (availableRecipes.isEmpty()) {
            Text noRecipes = PixelScene.createMultiline("No recipes available with your current inventory.", 8);
            noRecipes.maxWidth(WIDTH_MIN - MARGIN * 2);
            add(noRecipes);

            noRecipes.x = MARGIN;
            noRecipes.y = pos;
            pos += noRecipes.height() + MARGIN;

            resize(Math.max(WIDTH_MIN, (int) noRecipes.width() + MARGIN * 2),
                    (int) (pos + MARGIN));
        } else {
            // Create list of available recipes
            Component content = new Component();
            float listHeight = 0;

            for (AlchemyRecipe recipe : availableRecipes) {
                // Format input ingredients
                StringBuilder inputStr = new StringBuilder();
                Map<String, Integer> inputCounts = new HashMap<>();
                for (String input : recipe.getInput()) {
                    inputCounts.put(input, inputCounts.getOrDefault(input, 0) + 1);
                }

                for (Map.Entry<String, Integer> entry : inputCounts.entrySet()) {
                    if (inputStr.length() > 0) inputStr.append(", ");
                    inputStr.append(entry.getValue()).append("x ").append(entry.getKey());
                }

                // Format output items
                StringBuilder outputStr = new StringBuilder();
                Map<String, Integer> outputCounts = new HashMap<>();
                for (String output : recipe.getOutput()) {
                    outputCounts.put(output, outputCounts.getOrDefault(output, 0) + 1);
                }

                for (Map.Entry<String, Integer> entry : outputCounts.entrySet()) {
                    if (outputStr.length() > 0) outputStr.append(", ");
                    outputStr.append(entry.getValue()).append("x ").append(entry.getKey());
                }

                // Create recipe display
                RecipeDisplayItem recipeItem = new RecipeDisplayItem(inputStr.toString(), outputStr.toString());
                recipeItem.setRect(0, listHeight, WIDTH_MIN, ITEM_HEIGHT);
                content.add(recipeItem);
                listHeight += ITEM_HEIGHT + 2;
            }

            ScrollPane list = new ScrollPane(content);
            list.setRect(MARGIN, pos, WIDTH_MIN, Math.min(150, listHeight)); // Limit height to prevent oversized windows
            add(list);

            resize(WIDTH_MIN + MARGIN * 2, (int) Math.min(PixelScene.uiCamera.height * 0.8f, pos + Math.min(150, listHeight) + MARGIN));
        }
    }

    // Inner class to display a single recipe
    private static class RecipeDisplayItem extends Component {
        private Text inputText;
        private Text outputText;

        public RecipeDisplayItem(String input, String output) {
            super();

            inputText = PixelScene.createMultiline(input, 6);
            inputText.maxWidth(100);
            add(inputText);

            outputText = PixelScene.createMultiline("→ " + output, 6);
            outputText.maxWidth(100);
            outputText.hardlight(0x4CAF50); // Green color for outputs
            add(outputText);
        }

        @Override
        public void layout() {
            super.layout();

            inputText.x = 0;
            inputText.y = 0;

            outputText.x = 0;
            outputText.y = inputText.height() + 2;
        }
    }
}