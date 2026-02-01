package com.nyrds.pixeldungeon.alchemy;

import com.nyrds.LuaInterface;
import com.nyrds.pixeldungeon.items.Carcass;
import com.nyrds.pixeldungeon.items.common.ItemFactory;
import com.nyrds.pixeldungeon.mobs.common.MobFactory;
import com.nyrds.platform.EventCollector;
import com.nyrds.util.JsonHelper;
import com.nyrds.util.ModdingMode;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.mobs.Mob;
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

    // Recipe structure: input ingredients -> outputs (can be multiple items or mobs)
    private static final Map<List<String>, List<String>> recipes = new HashMap<>();

    // Recipe output types
    public enum EntityType {
        ITEM,
        MOB,
        CARCASS
    }

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
                JSONArray inputsArray = recipe.getJSONArray("input");
                List<String> inputs = new ArrayList<>();
                for (int j = 0; j < inputsArray.length(); j++) {
                    inputs.add(inputsArray.getString(j));
                }

                // Check if output is a single string or an array of outputs
                List<String> outputs = new ArrayList<>();

                if (recipe.has("outputs")) {
                    // Multiple outputs
                    JSONArray outputsArray = recipe.getJSONArray("outputs");
                    for (int k = 0; k < outputsArray.length(); k++) {
                        outputs.add(outputsArray.getString(k));
                    }
                }

                for (String output : outputs) {
                    EntityType entityType = determineEntityType(output);

                    if (!isEntityValid(output, entityType)) {
                        break;
                    }
                }

                for (String input : inputs) {
                    EntityType entityType = determineEntityType(input);

                    if (!isEntityValid(input, entityType)) {
                        break;
                    }
                }

                recipes.put(inputs, outputs);
            }
        } catch (JSONException e) {
            EventCollector.logException(e);
        }
    }

    /**
     * Determine the output type (item or mob) based on the entity name
     */
    public static EntityType determineEntityType(String entityName) {
        // Check if it's a carcass (which is treated as a mob input)
        if (entityName.startsWith(Carcass.CARCASS_OF)) {
            // Extract the mob name from the carcass
            String mobName = entityName.substring(Carcass.CARCASS_OF.length());
            if (MobFactory.hasMob(mobName)) {
                return EntityType.CARCASS;
            }
        }

        // Check if it's a valid item class
        if (ItemFactory.isValidItemClass(entityName)) {
            return EntityType.ITEM;
        }

        // Check if it's a valid mob class
        if (MobFactory.hasMob(entityName)) {
            return EntityType.MOB;
        }

        // Default to item for backward compatibility
        return EntityType.ITEM;
    }

    /**
     * Check if a single entity (item or mob name) is valid
     */
    private static boolean isEntityValid(String entityName, EntityType entityType) {
        // Check if it's a carcass (which is a valid input)
        if (entityName.startsWith(Carcass.CARCASS_OF)) {
            // Extract the mob name from the carcass and check if it's valid
            String mobName = entityName.substring(Carcass.CARCASS_OF.length());
            return MobFactory.hasMob(mobName);
        }

        // Validate based on expected output type
        if (entityType == EntityType.ITEM) {
            return ItemFactory.isValidItemClass(entityName);
        } else if (entityType == EntityType.MOB) {
            return MobFactory.hasMob(entityName);
        }

        return false;
    }

    /**
     * Check if a single entity (item name) is valid - legacy method for backward compatibility
     */
    private static boolean isEntityValid(String entityName) {
        // Default to checking as an item for backward compatibility
        return isEntityValid(entityName, EntityType.ITEM);
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
     * Add a recipe with multiple outputs with validation
     */
    @LuaInterface
    public static boolean addRecipe(List<String> input, List<String> outputs) {
        List<EntityType> entityTypes = new ArrayList<>();

        // Validate all outputs
        for (String output : outputs) {
            EntityType entityType = determineEntityType(output);
            entityTypes.add(entityType);

            if (!isEntityValid(output, entityType)) {
                return false; // Invalid output
            }

            // Additional validation: try to create the output to ensure it's valid
            if (entityType == EntityType.ITEM) {
                Item testItem = ItemFactory.itemByName(output);
                if (testItem == null) {
                    return false; // Invalid item
                }
            } else if (entityType == EntityType.MOB) {
                // For mobs, just validate that the mob exists
                if (!MobFactory.hasMob(output)) {
                    return false; // Invalid mob
                }
            }
        }

        if (areAllEntitiesValid(input)) {
            recipes.put(new ArrayList<>(input), new ArrayList<>(outputs));
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
     * Check if a recipe exists for the given inputs
     */
    public static boolean hasRecipe(List<String> input) {
        return getOutputForInput(input) != null;
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

    /**
     * Register a recipe from Lua with validation
     * This method is called from Lua scripts to register custom recipes at runtime
     */
    @LuaInterface
    public static boolean registerRecipeFromLua(List<String> input, Object output) {
        List<String> outputs = new ArrayList<>();

        // Handle both single output (string) and multiple outputs (table/array)
        if (output instanceof String) {
            outputs.add((String) output);
        } else if (output instanceof List) {
            outputs.addAll((List<String>) output);
        } else {
            return false; // Invalid output type
        }

        // Use the existing addRecipe method which handles validation
        return addRecipe(input, outputs);
    }

    /**
     * Check if a specific item is part of any recipe's input
     * @param itemName The name of the item to check
     * @return A list of recipes that use this item as an ingredient
     */
    @LuaInterface
    public static List<Map.Entry<List<String>, List<String>>> getRecipesWithItem(String itemName) {
        List<Map.Entry<List<String>, List<String>>> matchingRecipes = new ArrayList<>();

        for (Map.Entry<List<String>, List<String>> recipe : recipes.entrySet()) {
            if (recipe.getKey().contains(itemName)) {
                matchingRecipes.add(recipe);
            }
        }

        return matchingRecipes;
    }


    /**
     * Check if the player has all required ingredients for a specific recipe
     * @param inputIngredients The list of ingredients required by the recipe
     * @param playerInventory The player's current inventory
     * @return True if the player has all required ingredients in sufficient quantities
     */
    public static boolean hasRequiredIngredients(List<String> inputIngredients, Map<String, Integer> playerInventory) {
        // Count required quantities for each ingredient
        Map<String, Integer> requiredQuantities = new HashMap<>();
        for (String ingredient : inputIngredients) {
            requiredQuantities.put(ingredient, requiredQuantities.getOrDefault(ingredient, 0) + 1);
        }

        // Check if player has enough of each required ingredient
        for (Map.Entry<String, Integer> required : requiredQuantities.entrySet()) {
            String ingredient = required.getKey();
            int requiredQty = required.getValue();

            int availableQty = playerInventory.getOrDefault(ingredient, 0);

            if (availableQty < requiredQty) {
                return false; // Not enough of this ingredient
            }
        }

        return true;
    }

    public static Map<String, Integer> buildAlchemyInventory(Char hero) {
        Map<String, Integer> inventory = new HashMap<>();
        for (Item item : hero.getBelongings()) {
            if (item.isIdentified()) {
                String itemName = item.getEntityKind();
                inventory.put(itemName, inventory.getOrDefault(itemName, 0) + item.quantity());
            }
        }
        return inventory;
    }

    /**
     * Get all recipes for which the player has all required ingredients
     * @param playerInventory The player's current inventory with quantities
     * @return A list of recipes for which the player has all required ingredients
     */
    public static List<Map.Entry<List<String>, List<String>>> getAvailableRecipes(Map<String, Integer> playerInventory) {
        List<Map.Entry<List<String>, List<String>>> availableRecipes = new ArrayList<>();

        for (var recipe : recipes.entrySet()) {
            if (hasRequiredIngredients(recipe.getKey(), playerInventory)) {
                availableRecipes.add(recipe);
            }
        }

        return availableRecipes;
    }

    /**
     * Get all recipes that contain a specific item as an ingredient
     * @param itemName The name of the item to search for
     * @return A list of recipes that use this item as an ingredient
     */
    @LuaInterface
    public static List<Map.Entry<List<String>, List<String>>> getRecipesContainingItem(String itemName) {
        List<Map.Entry<List<String>, List<String>>> matchingRecipes = new ArrayList<>();

        for (Map.Entry<List<String>, List<String>> recipe : recipes.entrySet()) {
            if (recipe.getKey().contains(itemName)) {
                matchingRecipes.add(recipe);
            }
        }

        return matchingRecipes;
    }

}