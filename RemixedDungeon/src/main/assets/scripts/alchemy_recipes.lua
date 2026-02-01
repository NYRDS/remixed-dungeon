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

    -- NEW RECIPES WITH MOB OUTPUT:
    
    -- Recipe: 3 Rat Skulls + 1 Skeleton Key = Skeleton
    local skeletonRecipe = {}
    table.insert(skeletonRecipe, "RatSkull")
    table.insert(skeletonRecipe, "RatSkull")
    table.insert(skeletonRecipe, "RatSkull")
    table.insert(skeletonRecipe, "SkeletonKey")
    RPD.AlchemyRecipes.registerRecipeFromLua(skeletonRecipe, "Skeleton")
    
    -- Recipe: 2 Corpse Dust + 1 Dew Vial = Fetid Rat
    local fetidRatRecipe = {}
    table.insert(fetidRatRecipe, "CorpseDust")
    table.insert(fetidRatRecipe, "CorpseDust")
    table.insert(fetidRatRecipe, "DewVial")
    RPD.AlchemyRecipes.registerRecipeFromLua(fetidRatRecipe, "FetidRat")
    
    -- Recipe: 1 Gold + 1 Iron Key = Mimic
    local mimicRecipe = {}
    table.insert(mimicRecipe, "Gold")
    table.insert(mimicRecipe, "IronKey")
    RPD.AlchemyRecipes.registerRecipeFromLua(mimicRecipe, "Mimic")
    
    -- Recipe: 5 Mystery Meat + 1 Dewdrop = Piranha
    local piranhaRecipe = {}
    table.insert(piranhaRecipe, "MysteryMeat")
    table.insert(piranhaRecipe, "MysteryMeat")
    table.insert(piranhaRecipe, "MysteryMeat")
    table.insert(piranhaRecipe, "MysteryMeat")
    table.insert(piranhaRecipe, "MysteryMeat")
    table.insert(piranhaRecipe, "Dewdrop")
    RPD.AlchemyRecipes.registerRecipeFromLua(piranhaRecipe, "Piranha")

    -- NEW RECIPES USING CARCASSES:

    -- Recipe: 1 Carcass of Rat + 1 Skeleton Key = Skeleton
    local skeletonFromCarcassRecipe = {}
    table.insert(skeletonFromCarcassRecipe, "Carcass of Rat")
    table.insert(skeletonFromCarcassRecipe, "SkeletonKey")
    RPD.AlchemyRecipes.registerRecipeFromLua(skeletonFromCarcassRecipe, "Skeleton")

    -- Recipe: 1 Carcass of Albino + 1 Corpse Dust = Fetid Rat
    local fetidRatFromCarcassRecipe = {}
    table.insert(fetidRatFromCarcassRecipe, "Carcass of Albino")
    table.insert(fetidRatFromCarcassRecipe, "CorpseDust")
    RPD.AlchemyRecipes.registerRecipeFromLua(fetidRatFromCarcassRecipe, "FetidRat")

    -- Recipe: 1 Carcass of Thief + 1 Dried Rose = Ghost
    local ghostFromCarcassRecipe = {}
    table.insert(ghostFromCarcassRecipe, "Carcass of Thief")
    table.insert(ghostFromCarcassRecipe, "DriedRose")
    RPD.AlchemyRecipes.registerRecipeFromLua(ghostFromCarcassRecipe, "Ghost")

    -- NEW RECIPES WITH MULTIPLE OUTPUTS:

    -- Recipe: 1 Rat Meat + 1 Rat Skull = 1 Potion of Healing + 1 Skeleton
    local multiOutputRecipe1 = {}
    table.insert(multiOutputRecipe1, "Rat.meat")
    table.insert(multiOutputRecipe1, "Rat.skull")
    local outputs1 = {"PotionOfHealing", "Skeleton"}
    RPD.AlchemyRecipes.registerRecipeFromLua(multiOutputRecipe1, outputs1)

    -- Recipe: 1 Corpse Dust + 1 Dewdrop + 1 Bone Shard = 1 Fetid Rat + 1 Potion of Might
    local multiOutputRecipe2 = {}
    table.insert(multiOutputRecipe2, "CorpseDust")
    table.insert(multiOutputRecipe2, "Dewdrop")
    table.insert(multiOutputRecipe2, "BoneShard")
    local outputs2 = {"FetidRat", "PotionOfMight"}
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
        table.insert(advancedRecipe, "Earthroot.Seed")
        table.insert(advancedRecipe, "DwarfToken")
        RPD.AlchemyRecipes.registerRecipeFromLua(advancedRecipe, "PotionOfStrength")
        RPD.glog("Advanced recipe registered based on dungeon level")
    end
end

-- Register conditional recipes
registerConditionalRecipes()