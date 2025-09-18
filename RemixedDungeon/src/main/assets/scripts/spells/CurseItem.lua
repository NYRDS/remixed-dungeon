--
-- Test spell to demonstrate item selection functionality
-- This spell allows the player to select an item from their backpack and curse it
--

local RPD = require "scripts/lib/commonClasses"
local itemSelector = require "scripts/lib/itemSelector"
local spell = require "scripts/lib/spell"

return spell.init{
    desc = function()
        return {
            image = 0,  -- Using a generic spell icon
            imageFile = "spellsIcons/necromancy.png",
            name = "Curse Item",
            info = "Select an item from your backpack to curse it.",
            magicAffinity = "Necromancy",
            targetingType = "self",
            level = 1,
            castTime = 0,
            spellCost = 3,
            cooldown = 5
        }
    end,
    
    cast = function(self, spell, chr)
        -- Show item selection window
        itemSelector.selectItem(function(item, selector)
            if item then
                -- Check if the item is already cursed
                if item:isCursed() then
                    RPD.glog("That item is already cursed!")
                else
                    -- Curse the selected item
                    item:setCursed(true);
                    item:setCursedKnown(true);

                    RPD.glog("You have cursed your " .. item:name() .. "!")
                    
                    -- Apply a visual effect
                    RPD.zapEffect(chr:getPos(), chr:getPos(), "ShadowParticle")
                    
                    -- Play a sound effect
                    RPD.playSound("snd_cursed.mp3")
                end
            else
                RPD.glog("You decided not to curse anything.")
            end
        end, RPD.BackpackMode.ALL, "Select an item to curse")
        
        return true
    end
}