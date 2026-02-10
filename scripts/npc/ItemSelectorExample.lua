--
-- Example script demonstrating how to use the itemSelector library
-- This script creates an NPC that allows the player to select an item from their backpack
--

local RPD = require "scripts/lib/commonClasses"
local itemSelector = require "scripts/lib/itemSelector"
local mob = require "scripts/lib/mob"

return mob.init({
    spawn = function(self, level)
        -- Set the NPC's AI state
        RPD.setAi(self, "NpcDefault")
    end,

    interact = function(self, chr)
        -- Show a message to the player
        RPD.glog("Hello! I can help you select an item from your backpack.")
        
        -- Show item selection window
        itemSelector.selectItem(function(item, selector)
            if item then
                RPD.glog("You selected: " .. item:name())
                -- Do something with the selected item
                -- For example, identify it
                if not item:isIdentified() then
                    item:identify()
                    RPD.glog("I've identified your " .. item:name() .. "!")
                else
                    RPD.glog("That item is already identified.")
                end
            else
                RPD.glog("You didn't select any item.")
            end
        end, RPD.BackpackMode.ALL, "Select an item for me to examine")
    end
})