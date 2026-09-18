---
--- Levitation: floats over traps/terrain, presses cell on landing
--- (was actors/buffs/Levitation.java)
---
local RPD  = require "scripts/lib/commonClasses"
local buff = require "scripts/lib/buff"

return buff.init{
    desc  = function ()
        return {
            icon          = 1, -- BuffIndicator.LEVITATION
            name          = "LevitationBuff_Name",
            info          = "LevitationBuff_Info",
        }
    end,

    attached = function(self, buff)
        RPD.removeBuff(buff.target, "Roots")
    end,

    detach = function(self, buff)
        local level = RPD.Dungeon.level
        if level then
            level:press(buff.target:getPos(), buff.target)
        end
    end,

    charSpriteStatus = function(self, buff)
        return "LEVITATING"
    end
}
