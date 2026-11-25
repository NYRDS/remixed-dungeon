---
--- WandOfBlink: teleport the caster to the aimed cell (clamped to the wand
--- level); hitObjects targeting, no SHAMAN auto-zap (was wands/WandOfBlink.java).
---
local RPD  = require "scripts/lib/commonClasses"
local wand = require "scripts/lib/wand"

return wand.init{
    desc = function()
        return {
            fx         = "blink",
            hitObjects = true,
        }
    end,
    destCell = function(self, item, src, target)
        -- was the WandOfBlink.getDestinationCell override, adjusting the base cast
        local wandLevel = item:effectiveLevel()
        if RPD.Ballistica.distance > wandLevel + 4 then
            return RPD.Ballistica.trace[wandLevel + 3]
        elseif not RPD.Dungeon.level:isCellNonOccupied(target) and RPD.Ballistica.distance > 1 then
            return RPD.Ballistica.trace[RPD.Ballistica.distance - 2]
        end
        return nil -- fall through to the base cast result
    end,
    zap = function(self, item, cell, victim)
        item:getOwner():getSprite():setVisible(true)
        RPD.CharUtils:appear(item:getOwner(), cell)
        item:getOwner():observe()
    end,
    affectTarget = function(self, item)
        return false
    end,
}
