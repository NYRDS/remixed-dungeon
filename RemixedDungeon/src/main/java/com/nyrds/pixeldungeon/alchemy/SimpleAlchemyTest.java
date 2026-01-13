package com.nyrds.pixeldungeon.alchemy;

import com.nyrds.pixeldungeon.items.common.ItemFactory;
import com.watabou.pixeldungeon.items.Item;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple test to verify the AlchemyRecipes functionality
 */
public class SimpleAlchemyTest {
    public static void main(String[] args) {
        System.out.println("Testing AlchemyRecipes implementation...");
        
        // Check initial recipe count (should include recipes from JSON)
        System.out.println("Initial recipe count: " + AlchemyRecipes.getRecipeCount());
        
        // Test adding a recipe
        List<String> input = new ArrayList<>();
        input.add("Sungrass.Seed");
        input.add("DewVial");
        
        boolean added = AlchemyRecipes.addRecipe(input, "PotionOfHealing");
        System.out.println("Recipe added successfully: " + added);
        System.out.println("New recipe count: " + AlchemyRecipes.getRecipeCount());
        
        // Test finding the recipe
        List<String> outputs = AlchemyRecipes.getOutputForInput(input);
        System.out.println("Output for input: " + (outputs != null ? String.join(", ", outputs) : "null"));

        // Test creating an item
        Item item = AlchemyRecipes.createOutputItem(input);
        System.out.println("Created item: " + (item != null ? item.getEntityKind() : "null"));
        
        // Test validation with invalid input
        List<String> invalidInput = new ArrayList<>();
        invalidInput.add("InvalidItem");
        boolean invalidAdded = AlchemyRecipes.addRecipe(invalidInput, "PotionOfHealing");
        System.out.println("Invalid recipe added: " + invalidAdded);
        
        System.out.println("Test completed.");
    }
}