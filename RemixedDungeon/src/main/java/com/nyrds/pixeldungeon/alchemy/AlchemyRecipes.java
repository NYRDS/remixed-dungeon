package com.nyrds.pixeldungeon.alchemy;

import com.nyrds.LuaInterface;
import com.nyrds.pixeldungeon.items.Carcass;
import com.nyrds.pixeldungeon.items.common.ItemFactory;
import com.nyrds.pixeldungeon.mobs.common.MobFactory;
import com.nyrds.platform.EventCollector;
import com.nyrds.util.JsonHelper;
import com.nyrds.util.ModdingMode;
import com.watabou.pixeldungeon.actors.Char;
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
    private static final List<AlchemyRecipe> recipes = new ArrayList<>();

    // Recipe output types
    public enum EntityType {
        ITEM,
        MOB,
        CARCASS
    }

    // Static initialization to load recipes
    static {
        loadRecipesFromJson();
        generateMobResurrectionRecipes();
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
                List<InputItem> inputs = new ArrayList<>();
                
                if (recipe.has("input")) {
                    JSONArray inputsArray = recipe.getJSONArray("input");
                    for (int j = 0; j < inputsArray.length(); j++) {
                        Object inputObj = inputsArray.get(j);
                        if (inputObj instanceof JSONObject) {
                            JSONObject inputJson = (JSONObject) inputObj;
                            String name = inputJson.getString("name");
                            int count = inputJson.optInt("count", 1); // Default to 1 if count not specified
                            inputs.add(new InputItem(name, count));
                        }
                    }
                }

                // Parse output ingredients
                List<OutputItem> outputs = new ArrayList<>();
                
                if (recipe.has("outputs")) {
                    JSONArray outputsArray = recipe.getJSONArray("outputs");
                    for (int k = 0; k < outputsArray.length(); k++) {
                        Object outputObj = outputsArray.get(k);
                        if (outputObj instanceof JSONObject) {
                            JSONObject outputJson = (JSONObject) outputObj;
                            String name = outputJson.getString("name");
                            int count = outputJson.optInt("count", 1); // Default to 1 if count not specified
                            outputs.add(new OutputItem(name, count));
                        }
                    }
                }

                for (OutputItem output : outputs) {
                    EntityType entityType = determineEntityType(output.getName());

                    if (!isEntityValid(output.getName(), entityType)) {
                        break;
                    }
                }

                for (InputItem input : inputs) {
                    EntityType entityType = determineEntityType(input.getName());

                    if (!isEntityValid(input.getName(), entityType)) {
                        break;
                    }
                }

                recipes.add(new AlchemyRecipe(inputs, outputs));
            }
        } catch (JSONException e) {
            EventCollector.logException(e);
        }
    }

    /**
     * Generate mob resurrection recipes programmatically
     * Creates recipes: 5x mob carcass + varying amounts of VileEssence based on mob power = mob
     */
    private static void generateMobResurrectionRecipes() {
        // Get all available mob names from the factory
        for (String mobName : MobFactory.getAllMobNames()) {
            // Skip NPCs and special mobs that shouldn't be resurrectable
            if (isNonResurrectableMob(mobName)) {
                continue;
            }

            // Calculate VileEssence requirement based on mob power/level
            int vileEssenceReq = calculateVileEssenceRequirement(mobName);
            
            // Create the recipe: 5x carcass + Xx VileEssence = mob
            List<InputItem> inputs = new ArrayList<>();
            inputs.add(new InputItem(Carcass.CARCASS_OF + mobName, 5)); // 5x mob carcass
            inputs.add(new InputItem("VileEssence", vileEssenceReq));   // Variable VileEssence based on mob power
            
            List<OutputItem> outputs = new ArrayList<>();
            outputs.add(new OutputItem(mobName, 1)); // 1x the original mob
            
            // Add the recipe if it's valid
            if (addRecipe(inputs, outputs)) {
                // Optionally log the generated recipe for debugging
                // GLog.debug("Generated resurrection recipe: 5x %s + %dx VileEssence = 1x %s", 
                //           Carcass.CARCASS_OF + mobName, vileEssenceReq, mobName);
            }
        }
    }

    /**
     * Check if a mob should not be resurrectable (NPCs, special mobs, etc.)
     */
    private static boolean isNonResurrectableMob(String mobName) {
        // Skip NPCs
        if (mobName.endsWith("NPC")) {
            return true;
        }
        
        // Skip special mobs that shouldn't be resurrectable
        String[] nonResurrectableMobs = {
            "MirrorImage", "Wraith", "Skeleton", "FetidRat", "SuspiciousRat", 
            "PseudoRat", "Ghost", "Undead", "Shopkeeper", "TownShopkeeper",
            "Sheep", "Mimic", "MimicPie", "MimicAmulet", "Statue", "ArmoredStatue", "GoldenStatue"
        };
        
        for (String nonResurrectable : nonResurrectableMobs) {
            if (mobName.equals(nonResurrectable)) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * Calculate VileEssence requirement based on mob power/level
     * More powerful mobs require more VileEssence
     */
    private static int calculateVileEssenceRequirement(String mobName) {
        try {
            // Create a temporary instance to get mob stats
            com.watabou.pixeldungeon.actors.mobs.Mob mob = MobFactory.mobByName(mobName);
            
            // Base calculation on expForKill (a measure of mob strength) and HP
            int expFactor = Math.max(1, mob.expForKill); // Higher exp reward = stronger mob
            int hpFactor = Math.max(1, mob.ht() / 10); // Factor in based on HP
            
            // Calculate requirement (higher for stronger mobs)
            int requirement = Math.max(3, (expFactor + hpFactor) / 3);
            
            return requirement;
            
        } catch (Exception e) {
            // If we can't instantiate the mob, use a default value
            // Base level mobs get 3-5 essence, higher level mobs get more
            if (mobName.contains("King") || mobName.contains("Boss") || mobName.contains("Yog")) {
                return 20; // High value for bosses
            } else if (mobName.contains("Elemental") || mobName.contains("Golem") || 
                       mobName.contains("Scorpio") || mobName.contains("Tengu")) {
                return 10; // Medium-high value for strong mobs
            } else {
                return 5; // Default for regular mobs
            }
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
    public static boolean addRecipe(List<InputItem> input, List<OutputItem> outputs) {
        List<EntityType> entityTypes = new ArrayList<>();

        // Validate all outputs
        for (OutputItem output : outputs) {
            EntityType entityType = determineEntityType(output.getName());
            entityTypes.add(entityType);

            if (!isEntityValid(output.getName(), entityType)) {
                return false; // Invalid output
            }

            // Additional validation: try to create the output to ensure it's valid
            if (entityType == EntityType.ITEM) {
                Item testItem = ItemFactory.itemByName(output.getName());
                if (!testItem.valid()) {
                    return false; // Invalid item
                }
            } else if (entityType == EntityType.MOB) {
                // For mobs, just validate that the mob exists
                if (!MobFactory.hasMob(output.getName())) {
                    return false; // Invalid mob
                }
            }
        }

        // Validate all inputs
        for (InputItem inputItem : input) {
            if (!isEntityValid(inputItem.getName())) {
                return false; // Invalid input
            }
        }

        recipes.add(new AlchemyRecipe(new ArrayList<>(input), new ArrayList<>(outputs)));
        return true;
    }

    /**
     * Get output items for given input ingredients
     */
    public static List<OutputItem> getOutputForInput(List<InputItem> input) {
        // Normalize the input to handle carcasses
        List<InputItem> normalizedInput = normalizeInput(input);

        // Try to find exact match
        for (AlchemyRecipe recipe : recipes) {
            List<InputItem> recipeInput = recipe.getInput();
            if (recipeInput.size() == normalizedInput.size() &&
                recipeInput.containsAll(normalizedInput) &&
                normalizedInput.containsAll(recipeInput)) {
                return recipe.getOutput();
            }
        }
        return null; // No recipe found
    }

    /**
     * Normalize input to handle carcasses by extracting the mob name from carcass items
     */
    private static List<InputItem> normalizeInput(List<InputItem> input) {
        List<InputItem> normalized = new ArrayList<>();
        for (InputItem item : input) {
            if (item.getName().startsWith(Carcass.CARCASS_OF)) {
                // Extract the mob name from the carcass
                String mobName = item.getName().substring(Carcass.CARCASS_OF.length());
                normalized.add(new InputItem(mobName, item.getCount()));
            } else {
                normalized.add(item);
            }
        }
        return normalized;
    }

    /**
     * Check if a recipe exists for the given inputs
     */
    public static boolean hasRecipe(List<InputItem> input) {
        return getOutputForInput(input) != null;
    }

    /**
     * Get a random valid recipe for testing purposes
     */
    public static AlchemyRecipe getRandomRecipe() {
        if (recipes.isEmpty()) {
            return null;
        }

        int index = Random.Int(recipes.size());
        return recipes.get(index);
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
    public static List<AlchemyRecipe> getAllRecipes() {
        return new ArrayList<>(recipes);
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
    public static boolean registerRecipeFromLua(List<Object> input, Object output) {
        List<InputItem> inputs = new ArrayList<>();
        List<OutputItem> outputs = new ArrayList<>();

        // Process input items - handle both string format (backward compatibility) and object format
        for (Object inputObj : input) {
            if (inputObj instanceof String) {
                // Backward compatibility: single string means count of 1
                inputs.add(new InputItem((String) inputObj));
            } else if (inputObj instanceof Map) {
                // New format: object with name and count properties
                Map<?, ?> inputMap = (Map<?, ?>) inputObj;
                String name = (String) inputMap.get("name");
                Integer count = (Integer) inputMap.get("count");
                if (name != null) {
                    if (count != null) {
                        inputs.add(new InputItem(name, count));
                    } else {
                        inputs.add(new InputItem(name)); // Default count to 1
                    }
                } else {
                    return false; // Invalid input format
                }
            } else {
                return false; // Invalid input type
            }
        }

        // Handle both single output (string) and multiple outputs (table/array)
        if (output instanceof String) {
            outputs.add(new OutputItem((String) output)); // Default count to 1
        } else if (output instanceof List) {
            for (Object obj : (List<?>) output) {
                if (obj instanceof String) {
                    outputs.add(new OutputItem((String) obj)); // Default count to 1
                } else if (obj instanceof Map) {
                    // New format: object with name and count properties
                    Map<?, ?> outputMap = (Map<?, ?>) obj;
                    String name = (String) outputMap.get("name");
                    Integer count = (Integer) outputMap.get("count");
                    if (name != null) {
                        if (count != null) {
                            outputs.add(new OutputItem(name, count));
                        } else {
                            outputs.add(new OutputItem(name)); // Default count to 1
                        }
                    } else {
                        return false; // Invalid output format
                    }
                }
            }
        } else {
            return false; // Invalid output type
        }

        // Use the existing addRecipe method which handles validation
        return addRecipe(inputs, outputs);
    }

    /**
     * Check if a specific item is part of any recipe's input
     * @param itemName The name of the item to check
     * @return A list of recipes that use this item as an ingredient
     */
    @LuaInterface
    public static List<AlchemyRecipe> getRecipesWithItem(String itemName) {
        List<AlchemyRecipe> matchingRecipes = new ArrayList<>();

        for (AlchemyRecipe recipe : recipes) {
            for (InputItem inputItem : recipe.getInput()) {
                if (inputItem.getName().equals(itemName)) {
                    matchingRecipes.add(recipe);
                    break; // Found in this recipe, move to next recipe
                }
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
    public static boolean hasRequiredIngredients(List<InputItem> inputIngredients, Map<String, Integer> playerInventory) {
        // Check if player has enough of each required ingredient
        for (InputItem ingredient : inputIngredients) {
            String ingredientName = ingredient.getName();
            int requiredQty = ingredient.getCount();

            int availableQty = playerInventory.getOrDefault(ingredientName, 0);

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
    public static List<AlchemyRecipe> getAvailableRecipes(Map<String, Integer> playerInventory) {
        List<AlchemyRecipe> availableRecipes = new ArrayList<>();

        for (AlchemyRecipe recipe : recipes) {
            if (hasRequiredIngredients(recipe.getInput(), playerInventory)) {
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
    public static List<AlchemyRecipe> getRecipesContainingItem(String itemName) {
        List<AlchemyRecipe> matchingRecipes = new ArrayList<>();

        for (AlchemyRecipe recipe : recipes) {
            for (InputItem inputItem : recipe.getInput()) {
                if (inputItem.getName().equals(itemName)) {
                    matchingRecipes.add(recipe);
                    break; // Found in this recipe, move to next recipe
                }
            }
        }

        return matchingRecipes;
    }

}