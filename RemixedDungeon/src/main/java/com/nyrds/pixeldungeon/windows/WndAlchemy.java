package com.nyrds.pixeldungeon.windows;

import com.nyrds.pixeldungeon.alchemy.AlchemyRecipes;
import com.nyrds.util.GuiProperties;
import com.watabou.noosa.Text;
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

        // Check Available Recipes button
        RedButton checkRecipesButton = new RedButton("Check Available") {
            @Override
            protected void onClick() {
                hide();
                GameScene.show(new WndRecipeChecker());
            }
        };
        checkRecipesButton.setSize(Math.min(100, windowWidth/3), 18);
        buttonsContainer.add(checkRecipesButton);

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
        setupTransmutationCircle(windowWidth, windowHeight);

        addAfter(transmutationCircle,chrome);
        resize((int)windowWidth, (int)windowHeight);
    }

    private void setupTransmutationCircle(float windowWidth, float windowHeight) {
        // Create the transmutation circle
        transmutationCircle = new TransmutationCircle();
        float scale = 0.4f;

        transmutationCircle.alpha(0.3f);
        //transmutationCircle.brightness(0.5f);

        transmutationCircle.setScale(scale);

        transmutationCircle.setPos(windowWidth/2 - transmutationCircle.width/2, windowHeight/2 - transmutationCircle.height/2);


        // Add it to the window first so it appears behind other elements
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
                transmutationCircle.setRecipe(recipeEntry.getKey(), recipeEntry.getValue());
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
                            transmutationCircle.setRecipe(selectedRecipe.getKey(), selectedRecipe.getValue());
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
        //TODO - implement
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

        resize((int)windowWidth, (int)windowHeight);
    }
}