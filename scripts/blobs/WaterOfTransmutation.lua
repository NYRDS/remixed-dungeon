---
--- The well of transmutation: swaps an item for its transmuted counterpart
--- (weapon pairs, or a reroll within the same family, level/cursed state
--- carried over). Items without a counterpart are rejected - the water
--- stays and the heap is tossed away. Was WaterOfTransmutation.java.
---
local RPD   = require "scripts/lib/commonClasses"
local well  = require "scripts/lib/wellbase"

local WEAPON_PAIRS = {
    { "Knuckles", "Dagger" },
    { "Spear", "Quarterstaff" },
    { "Sword", "Mace" },
    { "Longsword", "BattleAxe" },
    { "Glaive", "WarHammer" },
    { "Claymore", "Halberd" },
    { "SacrificialSword", "GoldenSword" },
}

local function pairOf(kind)
    for _, pair in ipairs(WEAPON_PAIRS) do
        if pair[1] == kind then
            return pair[2]
        end
        if pair[2] == kind then
            return pair[1]
        end
    end
    return nil
end

local function rerollDifferent(category, item)
    local treasury = RPD.Treasury:getLevelTreasury()
    local n
    repeat
        n = treasury:random(category)
    until n:getEntityKind() ~= item:getEntityKind()
    return n
end

-- caveman: loop the no-arg upgrade/degrade - luaj resolves weapon:upgrade(n)
-- to the boolean overload (one level, not n)
local function copyUpgradeLevel(n, old)
    local level = old:level()
    if level > 0 then
        for _ = 1, level do
            n:upgrade()
        end
    elseif level < 0 then
        for _ = 1, -level do
            n:degrade()
        end
    end
end

local function copyKnownCursed(n, old)
    n:setLevelKnown(old:isLevelKnown())
    n:setCursedKnown(old:isCursedKnown())
    n:setCursed(old:isCursed())
end

return well.init{
    desc = function()
        return {
            mode     = "well",
            tileDesc = "WaterOfTransmutation_Info",
        }
    end,

    affectItem = function(self, water, item)
        local kind = item:getEntityKind()
        local transmuted

        if kind == "SpellBook" then
            transmuted = RPD.ItemFactory:itemByName("SpellBook")
        else
            local cls = RPD.ItemUtils:transmutationClass(item)

            if cls == "MeleeWeapon" then
                local pairKind = pairOf(kind)
                if pairKind then
                    transmuted = RPD.ItemFactory:itemByName(pairKind)
                    copyUpgradeLevel(transmuted, item)
                    if item:isEnchanted() then
                        transmuted:enchant(RPD.Enchantment:random())
                    end
                    copyKnownCursed(transmuted, item)
                end
            elseif cls == "Scroll" then
                if kind == "ScrollOfUpgrade" then
                    transmuted = RPD.ItemFactory:itemByName("ScrollOfWeaponUpgrade")
                elseif kind == "ScrollOfWeaponUpgrade" then
                    transmuted = RPD.ItemFactory:itemByName("ScrollOfUpgrade")
                else
                    transmuted = rerollDifferent(RPD.Treasury.Category.SCROLL, item)
                end
            elseif cls == "Potion" then
                if kind == "PotionOfStrength" then
                    transmuted = RPD.ItemFactory:itemByName("PotionOfMight")
                else
                    transmuted = rerollDifferent(RPD.Treasury.Category.POTION, item)
                end
            elseif cls == "Ring" then
                transmuted = rerollDifferent(RPD.Treasury.Category.RING, item)
                transmuted:level(0)
                copyUpgradeLevel(transmuted, item)
                copyKnownCursed(transmuted, item)
            elseif cls == "Wand" then
                transmuted = rerollDifferent(RPD.Treasury.Category.WAND, item)
                transmuted:level(0)
                -- java upgrade(negative) was a no-op, the loop replicates it
                for _ = 1, item:level() do
                    transmuted:upgrade()
                end
                copyKnownCursed(transmuted, item)
            elseif cls == "Seed" then
                transmuted = rerollDifferent(RPD.Treasury.Category.SEED, item)
            end
        end

        return transmuted
    end,
}
