-- Sample alchemy recipes defined in Lua
-- This demonstrates how recipes can be dynamically generated at runtime

local RPD = require "scripts/lib/commonClasses"

-- Define some custom alchemy recipes
local function registerCustomRecipes()
    -- Recipe: 2 Sungrass seeds + 1 Dew Vial = Potion of Healing
    local sungrassRecipe = {}
    table.insert(sungrassRecipe, {name = "Sungrass.Seed", count = 2})
    table.insert(sungrassRecipe, {name = "DewVial", count = 1})
    RPD.AlchemyRecipes.registerRecipeFromLua(sungrassRecipe, "PotionOfHealing")

    -- Recipe: 1 Firebloom seed + 1 Mystery Meat = Potion of Liquid Flame
    local firebloomRecipe = {}
    table.insert(firebloomRecipe, {name = "Firebloom.Seed", count = 1})
    table.insert(firebloomRecipe, {name = "MysteryMeat", count = 1})
    RPD.AlchemyRecipes.registerRecipeFromLua(firebloomRecipe, "PotionOfLiquidFlame")

    -- Recipe: 1 Icecap seed + 1 Water = Potion of Frost
    local icecapRecipe = {}
    table.insert(icecapRecipe, {name = "Icecap.Seed", count = 1})
    table.insert(icecapRecipe, {name = "Dewdrop", count = 1}) -- Dewdrop represents water
    RPD.AlchemyRecipes.registerRecipeFromLua(icecapRecipe, "PotionOfFrost")

    -- Recipe: 1 Sorrowmoss seed + 1 Corpse Dust = Potion of Toxic Gas
    local sorrowmossRecipe = {}
    table.insert(sorrowmossRecipe, {name = "Sorrowmoss.Seed", count = 1})
    table.insert(sorrowmossRecipe, {name = "CorpseDust", count = 1})
    RPD.AlchemyRecipes.registerRecipeFromLua(sorrowmossRecipe, "PotionOfToxicGas")

    -- Recipe: 1 Dreamweed seed + 1 Rat Hide = Potion of Invisibility
    local dreamweedRecipe = {}
    table.insert(dreamweedRecipe, {name = "Dreamweed.Seed", count = 1})
    table.insert(dreamweedRecipe, {name = "RatHide", count = 1})
    RPD.AlchemyRecipes.registerRecipeFromLua(dreamweedRecipe, "PotionOfInvisibility")

    -- NEW RECIPES WITH MOB OUTPUT:

    -- Recipe: 3 Rat Skulls + 1 Skeleton Key = Skeleton
    local skeletonRecipe = {}
    table.insert(skeletonRecipe, {name = "RatSkull", count = 3})
    table.insert(skeletonRecipe, {name = "SkeletonKey", count = 1})
    RPD.AlchemyRecipes.registerRecipeFromLua(skeletonRecipe, "Skeleton")

    -- Recipe: 2 Corpse Dust + 1 Dew Vial = Fetid Rat
    local fetidRatRecipe = {}
    table.insert(fetidRatRecipe, {name = "CorpseDust", count = 2})
    table.insert(fetidRatRecipe, {name = "DewVial", count = 1})
    RPD.AlchemyRecipes.registerRecipeFromLua(fetidRatRecipe, "FetidRat")

    -- Recipe: 1 Gold + 1 Iron Key = Mimic
    local mimicRecipe = {}
    table.insert(mimicRecipe, {name = "Gold", count = 1})
    table.insert(mimicRecipe, {name = "IronKey", count = 1})
    RPD.AlchemyRecipes.registerRecipeFromLua(mimicRecipe, "Mimic")

    -- Recipe: 5 Mystery Meat + 1 Dewdrop = Piranha
    local piranhaRecipe = {}
    table.insert(piranhaRecipe, {name = "MysteryMeat", count = 5})
    table.insert(piranhaRecipe, {name = "Dewdrop", count = 1})
    RPD.AlchemyRecipes.registerRecipeFromLua(piranhaRecipe, "Piranha")

    -- NEW RECIPES USING CARCASSES:

    -- Recipe: 1 Carcass of Rat + 1 Skeleton Key = Skeleton
    local skeletonFromCarcassRecipe = {}
    table.insert(skeletonFromCarcassRecipe, {name = "Carcass of Rat", count = 1})
    table.insert(skeletonFromCarcassRecipe, {name = "SkeletonKey", count = 1})
    RPD.AlchemyRecipes.registerRecipeFromLua(skeletonFromCarcassRecipe, "Skeleton")

    -- Recipe: 1 Carcass of Albino + 1 Corpse Dust = Fetid Rat
    local fetidRatFromCarcassRecipe = {}
    table.insert(fetidRatFromCarcassRecipe, {name = "Carcass of Albino", count = 1})
    table.insert(fetidRatFromCarcassRecipe, {name = "CorpseDust", count = 1})
    RPD.AlchemyRecipes.registerRecipeFromLua(fetidRatFromCarcassRecipe, "FetidRat")

    -- Recipe: 1 Carcass of Thief + 1 Dried Rose = Ghost
    local ghostFromCarcassRecipe = {}
    table.insert(ghostFromCarcassRecipe, {name = "Carcass of Thief", count = 1})
    table.insert(ghostFromCarcassRecipe, {name = "DriedRose", count = 1})
    RPD.AlchemyRecipes.registerRecipeFromLua(ghostFromCarcassRecipe, "Ghost")

    -- NEW RECIPES WITH MULTIPLE OUTPUTS:

    -- Recipe: 1 Rat Meat + 1 Rat Skull = 1 Potion of Healing + 1 Skeleton
    local multiOutputRecipe1 = {}
    table.insert(multiOutputRecipe1, {name = "Rat.meat", count = 1})
    table.insert(multiOutputRecipe1, {name = "Rat.skull", count = 1})
    local outputs1 = {{name = "PotionOfHealing", count = 1}, {name = "Skeleton", count = 1}}
    RPD.AlchemyRecipes.registerRecipeFromLua(multiOutputRecipe1, outputs1)

    -- Recipe: 1 Corpse Dust + 1 Dewdrop + 1 Bone Shard = 1 Fetid Rat + 1 Potion of Might
    local multiOutputRecipe2 = {}
    table.insert(multiOutputRecipe2, {name = "CorpseDust", count = 1})
    table.insert(multiOutputRecipe2, {name = "Dewdrop", count = 1})
    table.insert(multiOutputRecipe2, {name = "BoneShard", count = 1})
    local outputs2 = {{name = "FetidRat", count = 1}, {name = "PotionOfMight", count = 1}}
    RPD.AlchemyRecipes.registerRecipeFromLua(multiOutputRecipe2, outputs2)

    RPD.glog("Custom alchemy recipes registered from Lua")
end

-- Call the function to register recipes
registerCustomRecipes()

-- Optionally, you can also define more complex recipes based on conditions
local function registerConditionalRecipes()
    -- Only register advanced recipes if player has reached a certain depth
    if RPD.Dungeon.level.level > 5 then
        local advancedRecipe = {}
        table.insert(advancedRecipe, {name = "Earthroot.Seed", count = 1})
        table.insert(advancedRecipe, {name = "DwarfToken", count = 1})
        RPD.AlchemyRecipes.registerRecipeFromLua(advancedRecipe, "PotionOfStrength")
        RPD.glog("Advanced recipe registered based on dungeon level")
    end
end

-- Register conditional recipes
registerConditionalRecipes()