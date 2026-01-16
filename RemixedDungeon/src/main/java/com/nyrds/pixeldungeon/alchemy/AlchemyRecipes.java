package com.nyrds.pixeldungeon.alchemy;

import com.nyrds.LuaInterface;
import com.nyrds.lua.LuaEngine;
import com.nyrds.pixeldungeon.items.common.ItemFactory;
import com.nyrds.pixeldungeon.mobs.common.MobFactory;
import com.nyrds.pixeldungeon.items.Carcass;
import com.nyrds.util.JsonHelper;
import com.nyrds.util.ModdingMode;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.utils.Random;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class to manage alchemy recipes, supporting both JSON loading and Lua generation
 */
public class AlchemyRecipes {

    // Recipe structure: input ingredients -> outputs (can be multiple items or mobs)
    private static Map<List<String>, List<String>> recipes = new HashMap<>();

    // Recipe output types
    public enum OutputType {
        ITEM,
        MOB
    }

    // Extended recipe info to track output types for each recipe
    private static Map<List<String>, List<OutputType>> recipeTypes = new HashMap<>();

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

                // Check if output is a single string or an array of outputs
                List<String> outputs = new ArrayList<>();

                if (recipe.has("output")) {
                    // Single output
                    String output = recipe.getString("output");
                    outputs.add(output);
                } else if (recipe.has("outputs")) {
                    // Multiple outputs
                    JSONArray outputsArray = recipe.getJSONArray("outputs");
                    for (int k = 0; k < outputsArray.length(); k++) {
                        outputs.add(outputsArray.getString(k));
                    }
                }

                // Validate all outputs
                List<OutputType> outputTypes = new ArrayList<>();
                boolean allValid = true;

                for (String output : outputs) {
                    OutputType outputType = determineOutputType(output);
                    outputTypes.add(outputType);

                    if (!isEntityValid(output, outputType)) {
                        allValid = false;
                        break;
                    }
                }

                // Validate entities before adding to recipes
                if (areAllEntitiesValid(inputList) && allValid) {
                    recipes.put(inputList, outputs);
                    recipeTypes.put(inputList, outputTypes);
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
        // Check if it's a carcass (which is treated as a mob input)
        if (entityName.startsWith(Carcass.CARCASS_OF)) {
            // Extract the mob name from the carcass
            String mobName = entityName.substring(Carcass.CARCASS_OF.length());
            if (MobFactory.hasMob(mobName)) {
                return OutputType.MOB;
            }
        }

        // Check if it's a valid item class
        if (ItemFactory.isValidItemClass(entityName)) {
            return OutputType.ITEM;
        }

        // Check if it's a valid mob class
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
        // Check if it's a carcass (which is a valid input)
        if (entityName.startsWith(Carcass.CARCASS_OF)) {
            // Extract the mob name from the carcass and check if it's valid
            String mobName = entityName.substring(Carcass.CARCASS_OF.length());
            return MobFactory.hasMob(mobName);
        }

        // Validate based on expected output type
        if (outputType == OutputType.ITEM) {
            return ItemFactory.isValidItemClass(entityName);
        } else if (outputType == OutputType.MOB) {
            return MobFactory.hasMob(entityName);
        }

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
        return addRecipe(input, Arrays.asList(output));
    }

    /**
     * Add a recipe with multiple outputs with validation
     */
    @LuaInterface
    public static boolean addRecipe(List<String> input, List<String> outputs) {
        List<OutputType> outputTypes = new ArrayList<>();

        // Validate all outputs
        for (String output : outputs) {
            OutputType outputType = determineOutputType(output);
            outputTypes.add(outputType);

            if (!isEntityValid(output, outputType)) {
                return false; // Invalid output
            }

            // Additional validation: try to create the output to ensure it's valid
            if (outputType == OutputType.ITEM) {
                Item testItem = ItemFactory.itemByName(output);
                if (testItem == null) {
                    return false; // Invalid item
                }
            } else if (outputType == OutputType.MOB) {
                // For mobs, just validate that the mob exists
                if (!MobFactory.hasMob(output)) {
                    return false; // Invalid mob
                }
            }
        }

        if (areAllEntitiesValid(input)) {
            recipes.put(new ArrayList<>(input), new ArrayList<>(outputs));
            recipeTypes.put(new ArrayList<>(input), outputTypes);
            return true;
        }
        return false; // Recipe not added due to invalid entities
    }

    /**
     * Get output items for given input ingredients
     */
    public static List<String> getOutputForInput(List<String> input) {
        // Normalize the input to handle carcasses
        List<String> normalizedInput = normalizeInput(input);

        // Try to find exact match
        for (Map.Entry<List<String>, List<String>> entry : recipes.entrySet()) {
            if (entry.getKey().size() == normalizedInput.size() &&
                entry.getKey().containsAll(normalizedInput) &&
                normalizedInput.containsAll(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null; // No recipe found
    }

    /**
     * Normalize input to handle carcasses by extracting the mob name from carcass items
     */
    private static List<String> normalizeInput(List<String> input) {
        List<String> normalized = new ArrayList<>();
        for (String item : input) {
            if (item.startsWith(Carcass.CARCASS_OF)) {
                // Extract the mob name from the carcass
                String mobName = item.substring(Carcass.CARCASS_OF.length());
                normalized.add(mobName);
            } else {
                normalized.add(item);
            }
        }
        return normalized;
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
        List<String> outputKinds = getOutputForInput(input);
        if (outputKinds != null) {
            List<OutputType> outputTypes = getOutputTypeForInput(input);
            // Look for the first item output in the list
            for (int i = 0; i < outputKinds.size(); i++) {
                if (outputTypes.get(i) == OutputType.ITEM) {
                    return ItemFactory.itemByName(outputKinds.get(i));
                }
            }
        }
        return null;
    }

    /**
     * Create a mob based on the recipe output
     */
    public static com.watabou.pixeldungeon.actors.mobs.Mob createOutputMob(List<String> input) {
        List<String> outputKinds = getOutputForInput(input);
        if (outputKinds != null) {
            List<OutputType> outputTypes = getOutputTypeForInput(input);
            // Look for the first mob output in the list
            for (int i = 0; i < outputKinds.size(); i++) {
                if (outputTypes.get(i) == OutputType.MOB) {
                    return MobFactory.mobByName(outputKinds.get(i));
                }
            }
        }
        return null;
    }

    /**
     * Get the output types for the given input
     */
    public static List<OutputType> getOutputTypeForInput(List<String> input) {
        // Normalize the input to handle carcasses
        List<String> normalizedInput = normalizeInput(input);

        // Try to find exact match
        for (Map.Entry<List<String>, List<OutputType>> entry : recipeTypes.entrySet()) {
            if (entry.getKey().size() == normalizedInput.size() &&
                entry.getKey().containsAll(normalizedInput) &&
                normalizedInput.containsAll(entry.getKey())) {
                return entry.getValue();
            }
        }
        // Return default type list for backward compatibility
        List<OutputType> defaultTypes = new ArrayList<>();
        defaultTypes.add(OutputType.ITEM);
        return defaultTypes;
    }

    /**
     * Get a random valid recipe for testing purposes
     */
    public static Map.Entry<List<String>, List<String>> getRandomRecipe() {
        if (recipes.isEmpty()) {
            return null;
        }

        int index = Random.Int(recipes.size());
        int i = 0;
        for (Map.Entry<List<String>, List<String>> entry : recipes.entrySet()) {
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
    public static Map<List<String>, List<String>> getAllRecipes() {
        return new HashMap<>(recipes);
    }

    /**
     * Get the number of loaded recipes
     */
    public static int getRecipeCount() {
        return recipes.size();
    }
}