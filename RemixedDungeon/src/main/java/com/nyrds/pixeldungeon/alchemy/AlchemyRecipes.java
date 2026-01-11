package com.nyrds.pixeldungeon.alchemy;

import com.nyrds.LuaInterface;
import com.nyrds.lua.LuaEngine;
import com.nyrds.pixeldungeon.items.common.ItemFactory;
import com.nyrds.pixeldungeon.mobs.common.MobFactory;
import com.nyrds.util.JsonHelper;
import com.nyrds.util.ModdingMode;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.utils.Random;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class to manage alchemy recipes, supporting both JSON loading and Lua generation
 */
public class AlchemyRecipes {

    // Recipe structure: input ingredients -> output (can be item or mob)
    private static Map<List<String>, String> recipes = new HashMap<>();

    // Recipe output types
    public enum OutputType {
        ITEM,
        MOB
    }

    // Extended recipe info to track output type
    private static Map<List<String>, OutputType> recipeTypes = new HashMap<>();

    // Static initialization to load recipes
    static {
        loadRecipesFromJson();
    }

    /**
     * Load recipes from JSON file
     */
    private static void loadRecipesFromJson() {
        try {
            String jsonContent = ModdingMode.getResource("scripts/alchemy_recipes.json");
            if (jsonContent != null) {
                JSONObject json = JsonHelper.readJsonFromString(jsonContent);
                JSONArray recipesArray = json.getJSONArray("recipes");

                for (int i = 0; i < recipesArray.length(); i++) {
                    JSONObject recipe = recipesArray.getJSONObject(i);

                    // Parse input ingredients
                    JSONArray inputs = recipe.getJSONArray("input");
                    List<String> inputList = new ArrayList<>();
                    for (int j = 0; j < inputs.length(); j++) {
                        inputList.add(inputs.getString(j));
                    }

                    // Parse output item
                    String output = recipe.getString("output");

                    // Determine output type (item or mob)
                    OutputType outputType = determineOutputType(output);

                    // Validate entities before adding to recipes
                    if (areAllEntitiesValid(inputList) && isEntityValid(output, outputType)) {
                        recipes.put(inputList, output);
                        recipeTypes.put(inputList, outputType);
                    }
                }
            }
        } catch (JSONException e) {
            // If JSON file has errors, continue with empty recipes
            // Recipes can be added later via Lua
        }
    }

    /**
     * Determine the output type (item or mob) based on the entity name
     */
    private static OutputType determineOutputType(String entityName) {
        // Check if it's a valid item class first
        if (ItemFactory.isValidItemClass(entityName)) {
            return OutputType.ITEM;
        }

        // Check if it's a seed (special case)
        if (entityName.endsWith(".Seed") ||
            entityName.equals("Sungrass.Seed") ||
            entityName.equals("Firebloom.Seed") ||
            entityName.equals("Icecap.Seed") ||
            entityName.equals("Sorrowmoss.Seed") ||
            entityName.equals("Dreamweed.Seed") ||
            entityName.equals("Earthroot.Seed") ||
            entityName.equals("Fadeleaf.Seed") ||
            entityName.equals("Moongrace.Seed") ||
            entityName.equals("Rotberry.Seed")) {
            return OutputType.ITEM;
        }

        // Check for other special cases that might be valid in alchemy
        // For example, some items might be referenced by their full class name
        if (entityName.startsWith("com.watabou.pixeldungeon.items.") ||
            entityName.startsWith("com.nyrds.pixeldungeon.items.")) {
            // Extract the simple class name and check if it's valid
            String[] parts = entityName.split("\\.");
            String className = parts[parts.length - 1];
            if (ItemFactory.isValidItemClass(className)) {
                return OutputType.ITEM;
            }
        }

        // If it's not an item, check if it's a mob
        if (MobFactory.hasMob(entityName)) {
            return OutputType.MOB;
        }

        // Default to item for backward compatibility
        return OutputType.ITEM;
    }

    /**
     * Check if a single entity (item or mob name) is valid
     */
    private static boolean isEntityValid(String entityName, OutputType outputType) {
        if (outputType == OutputType.ITEM) {
            // Check if it's a valid item class
            if (ItemFactory.isValidItemClass(entityName)) {
                return true;
            }

            // Check if it's a seed (special case)
            if (entityName.endsWith(".Seed") ||
                entityName.equals("Sungrass.Seed") ||
                entityName.equals("Firebloom.Seed") ||
                entityName.equals("Icecap.Seed") ||
                entityName.equals("Sorrowmoss.Seed") ||
                entityName.equals("Dreamweed.Seed") ||
                entityName.equals("Earthroot.Seed") ||
                entityName.equals("Fadeleaf.Seed") ||
                entityName.equals("Moongrace.Seed") ||
                entityName.equals("Rotberry.Seed")) {
                return true;
            }

            // Check for other special cases that might be valid in alchemy
            // For example, some items might be referenced by their full class name
            if (entityName.startsWith("com.watabou.pixeldungeon.items.") ||
                entityName.startsWith("com.nyrds.pixeldungeon.items.")) {
                // Extract the simple class name and check if it's valid
                String[] parts = entityName.split("\\.");
                String className = parts[parts.length - 1];
                return ItemFactory.isValidItemClass(className);
            }
        } else if (outputType == OutputType.MOB) {
            // Check if it's a valid mob class
            return MobFactory.hasMob(entityName);
        }

        // Additional validation could be added here for other entity types
        return false;
    }

    /**
     * Check if a single entity (item name) is valid - legacy method for backward compatibility
     */
    private static boolean isEntityValid(String entityName) {
        // Default to checking as an item for backward compatibility
        return isEntityValid(entityName, OutputType.ITEM);
    }

    /**
     * Check if all entities in a list are valid
     */
    private static boolean areAllEntitiesValid(List<String> entities) {
        for (String entity : entities) {
            if (!isEntityValid(entity)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Add a recipe with validation
     */
    @LuaInterface
    public static boolean addRecipe(List<String> input, String output) {
        OutputType outputType = determineOutputType(output);

        if (areAllEntitiesValid(input) && isEntityValid(output, outputType)) {
            // Additional validation: try to create the output to ensure it's valid
            if (outputType == OutputType.ITEM) {
                Item testItem = ItemFactory.itemByName(output);
                if (testItem != null) {
                    recipes.put(new ArrayList<>(input), output);
                    recipeTypes.put(new ArrayList<>(input), outputType);
                    return true;
                }
            } else if (outputType == OutputType.MOB) {
                // For mobs, just validate that the mob exists
                if (MobFactory.hasMob(output)) {
                    recipes.put(new ArrayList<>(input), output);
                    recipeTypes.put(new ArrayList<>(input), outputType);
                    return true;
                }
            }
        }
        return false; // Recipe not added due to invalid entities
    }

    /**
     * Get output item for given input ingredients
     */
    public static String getOutputForInput(List<String> input) {
        // Try to find exact match
        for (Map.Entry<List<String>, String> entry : recipes.entrySet()) {
            if (entry.getKey().size() == input.size() &&
                entry.getKey().containsAll(input) &&
                input.containsAll(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null; // No recipe found
    }

    /**
     * Generate recipes using Lua script
     */
    @LuaInterface
    public static void generateRecipesWithLua(String luaScript) {
        try {
            // Execute the Lua script to generate recipes
            // Since LuaEngine doesn't have a runScript method for strings,
            // we'll use the globals to load and execute the string directly
            LuaEngine.getGlobals().load(luaScript).call();
        } catch (Exception e) {
            // Log the error but don't crash the game
            e.printStackTrace();
        }
    }

    /**
     * Register a recipe from Lua
     */
    @LuaInterface
    public static boolean registerRecipeFromLua(List<String> input, String output) {
        return addRecipe(input, output);
    }

    /**
     * Check if a recipe exists for the given inputs
     */
    public static boolean hasRecipe(List<String> input) {
        return getOutputForInput(input) != null;
    }

    /**
     * Create an item based on the recipe output
     */
    public static Item createOutputItem(List<String> input) {
        String outputKind = getOutputForInput(input);
        if (outputKind != null) {
            OutputType outputType = getOutputTypeForInput(input);
            if (outputType == OutputType.ITEM) {
                return ItemFactory.itemByName(outputKind);
            }
        }
        return null;
    }

    /**
     * Create a mob based on the recipe output
     */
    public static com.watabou.pixeldungeon.actors.mobs.Mob createOutputMob(List<String> input) {
        String outputKind = getOutputForInput(input);
        if (outputKind != null) {
            OutputType outputType = getOutputTypeForInput(input);
            if (outputType == OutputType.MOB) {
                return MobFactory.mobByName(outputKind);
            }
        }
        return null;
    }

    /**
     * Get the output type for the given input
     */
    public static OutputType getOutputTypeForInput(List<String> input) {
        // Try to find exact match
        for (Map.Entry<List<String>, OutputType> entry : recipeTypes.entrySet()) {
            if (entry.getKey().size() == input.size() &&
                entry.getKey().containsAll(input) &&
                input.containsAll(entry.getKey())) {
                return entry.getValue();
            }
        }
        return OutputType.ITEM; // Default to item for backward compatibility
    }

    /**
     * Get a random valid recipe for testing purposes
     */
    public static Map.Entry<List<String>, String> getRandomRecipe() {
        if (recipes.isEmpty()) {
            return null;
        }

        int index = Random.Int(recipes.size());
        int i = 0;
        for (Map.Entry<List<String>, String> entry : recipes.entrySet()) {
            if (i == index) {
                return entry;
            }
            i++;
        }
        return null;
    }

    /**
     * Clear all recipes (useful for testing or reloading)
     */
    @LuaInterface
    public static void clearRecipes() {
        recipes.clear();
        recipeTypes.clear();
    }

    /**
     * Get all recipes
     */
    public static Map<List<String>, String> getAllRecipes() {
        return new HashMap<>(recipes);
    }

    /**
     * Get the number of loaded recipes
     */
    public static int getRecipeCount() {
        return recipes.size();
    }
}