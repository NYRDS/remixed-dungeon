package com.nyrds.pixeldungeon.alchemy;

import com.nyrds.pixeldungeon.items.common.ItemFactory;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.potions.PotionOfHealing;
import com.watabou.pixeldungeon.plants.Seed;

import java.util.ArrayList;
import java.util.List;

/**
 * Test class for AlchemyRecipes functionality
 */
public class AlchemyRecipesTest {

    public static void runTests() {
        System.out.println("Running AlchemyRecipes tests...");
        
        // Test 1: Check if recipes are initially loaded
        System.out.println("Initial recipe count: " + AlchemyRecipes.getRecipeCount());
        
        // Test 2: Add a valid recipe
        List<String> validInput = new ArrayList<>();
        validInput.add("Sungrass.Seed");
        validInput.add("Sungrass.Seed");
        validInput.add("DewVial");
        boolean result = AlchemyRecipes.addRecipe(validInput, "PotionOfHealing");
        System.out.println("Adding valid recipe result: " + result);
        System.out.println("Recipe count after adding: " + AlchemyRecipes.getRecipeCount());
        
        // Test 3: Try to add an invalid recipe
        List<String> invalidInput = new ArrayList<>();
        invalidInput.add("Sungrass.Seed");
        invalidInput.add("InvalidItem"); // This should be invalid
        boolean invalidResult = AlchemyRecipes.addRecipe(invalidInput, "PotionOfHealing");
        System.out.println("Adding invalid recipe result: " + invalidResult);
        
        // Test 4: Try to add a recipe with invalid output
        List<String> validInput2 = new ArrayList<>();
        validInput2.add("Sungrass.Seed");
        validInput2.add("DewVial");
        boolean invalidOutputResult = AlchemyRecipes.addRecipe(validInput2, "InvalidOutputItem");
        System.out.println("Adding recipe with invalid output result: " + invalidOutputResult);
        
        // Test 5: Check if we can find the recipe we added
        String output = AlchemyRecipes.getOutputForInput(validInput);
        System.out.println("Output for valid input: " + output);
        
        // Test 6: Check if we can create an item from the recipe
        Item createdItem = AlchemyRecipes.createOutputItem(validInput);
        System.out.println("Created item: " + (createdItem != null ? createdItem.getClass().getSimpleName() : "null"));
        
        // Test 7: Check if recipe exists
        boolean hasRecipe = AlchemyRecipes.hasRecipe(validInput);
        System.out.println("Has recipe for valid input: " + hasRecipe);
        
        // Test 8: Try to find non-existent recipe
        List<String> nonExistentInput = new ArrayList<>();
        nonExistentInput.add("Firebloom.Seed");
        String nonExistentOutput = AlchemyRecipes.getOutputForInput(nonExistentInput);
        System.out.println("Output for non-existent input: " + (nonExistentOutput == null ? "null (expected)" : nonExistentOutput));
        
        // Test 9: Clear recipes and check count
        AlchemyRecipes.clearRecipes();
        System.out.println("Recipe count after clearing: " + AlchemyRecipes.getRecipeCount());
        
        // Re-add the recipe for further testing
        AlchemyRecipes.addRecipe(validInput, "PotionOfHealing");
        
        System.out.println("AlchemyRecipes tests completed.");
    }
    
    public static void main(String[] args) {
        runTests();
    }
}