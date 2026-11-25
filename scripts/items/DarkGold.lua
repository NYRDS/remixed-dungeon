---
--- DarkGold: raw dark ore, smelted by the troll blacksmith (was
--- items/quest/DarkGold.java). On the town level (depth 0) dropping or
--- throwing it melts it back into the heap instead of landing it.
---
local RPD     = require "scripts/lib/commonClasses"
local itemLib = require "scripts/lib/item"

local function melt(cell)
    RPD.playSound("snd_puff")
    RPD.Sfx.Splash:at(cell, 0xa38d1c, 8)
end

return itemLib.init{
    desc = function()
        return {
            image     = 102, -- ItemSpriteSheet.ORE
            imageFile = "items.png",
            name      = "DarkGold_Name",
            info      = "DarkGold_Info",
            stackable = true,
            price     = 1, -- per quantity
        }
    end,

    -- handled=true keeps the java default from running
    doDrop = function(self, item, chr)
        if RPD.Dungeon.depth > 0 then
            return false
        end

        item:detachAll(chr:getBelongings().backpack)
        RPD.ItemUtils:dropItemSprite(item, chr:getPos())
        melt(chr:getPos())
        return true
    end,

    onThrow = function(self, item, cell, thrower)
        if RPD.Dungeon.depth > 0 then
            item:dropTo(cell, thrower)
        else
            melt(cell)
        end
    end,
}
