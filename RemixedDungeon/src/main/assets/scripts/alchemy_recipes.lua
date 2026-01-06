-- Sample alchemy recipes defined in Lua
-- This demonstrates how recipes can be dynamically generated at runtime

local RPD = require "scripts/lib/commonClasses"

-- Define some custom alchemy recipes
local function registerCustomRecipes()
    -- Recipe: 2 Sungrass seeds + 1 Dew Vial = Potion of Healing
    local sungrassRecipe = {}
    table.insert(sungrassRecipe, "Sungrass.Seed")
    table.insert(sungrassRecipe, "Sungrass.Seed")
    table.insert(sungrassRecipe, "DewVial")
    RPD.AlchemyRecipes.registerRecipeFromLua(sungrassRecipe, "PotionOfHealing")
    
    -- Recipe: 1 Firebloom seed + 1 Mystery Meat = Potion of Liquid Flame
    local firebloomRecipe = {}
    table.insert(firebloomRecipe, "Firebloom.Seed")
    table.insert(firebloomRecipe, "MysteryMeat")
    RPD.AlchemyRecipes.registerRecipeFromLua(firebloomRecipe, "PotionOfLiquidFlame")
    
    -- Recipe: 1 Icecap seed + 1 Water = Potion of Frost
    local icecapRecipe = {}
    table.insert(icecapRecipe, "Icecap.Seed")
    table.insert(icecapRecipe, "Dewdrop") -- Dewdrop represents water
    RPD.AlchemyRecipes.registerRecipeFromLua(icecapRecipe, "PotionOfFrost")
    
    -- Recipe: 1 Sorrowmoss seed + 1 Corpse Dust = Potion of Toxic Gas
    local sorrowmossRecipe = {}
    table.insert(sorrowmossRecipe, "Sorrowmoss.Seed")
    table.insert(sorrowmossRecipe, "CorpseDust")
    RPD.AlchemyRecipes.registerRecipeFromLua(sorrowmossRecipe, "PotionOfToxicGas")
    
    -- Recipe: 1 Dreamweed seed + 1 Rat Hide = Potion of Invisibility
    local dreamweedRecipe = {}
    table.insert(dreamweedRecipe, "Dreamweed.Seed")
    table.insert(dreamweedRecipe, "RatHide")
    RPD.AlchemyRecipes.registerRecipeFromLua(dreamweedRecipe, "PotionOfInvisibility")
    
    RPD.glog("Custom alchemy recipes registered from Lua")
end

-- Call the function to register recipes
registerCustomRecipes()

-- Optionally, you can also define more complex recipes based on conditions
local function registerConditionalRecipes()
    -- Only register advanced recipes if player has reached a certain depth
    if RPD.Dungeon.level.level > 5 then
        local advancedRecipe = {}
        table.insert(advancedRecipe, "Earthroot.Seed")
        table.insert(advancedRecipe, "DwarfToken")
        RPD.AlchemyRecipes.registerRecipeFromLua(advancedRecipe, "PotionOfStrength")
        RPD.glog("Advanced recipe registered based on dungeon level")
    end
end

-- Register conditional recipes
registerConditionalRecipes()