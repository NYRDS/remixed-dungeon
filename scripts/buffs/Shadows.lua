---
--- Shadows: short invisibility renewed by Foliage (RPD.CharUtils:refreshShadows),
--- broken by visible enemies (was actors/buffs/Shadows.java)
---
local RPD  = require "scripts/lib/commonClasses"
local buff = require "scripts/lib/buff"

return buff.init{
    desc  = function ()
        return {
            icon          = 13, -- BuffIndicator.SHADOWS
            name          = "ShadowsBuff_Name",
            info          = "ShadowsBuff_Info",
        }
    end,

    attachTo = function(self, buff, target)
        target:adjustInvisibility(1)
        return true
    end,

    attached = function(self, buff)
        RPD.playSound("snd_meld")
        buff.target:observe()
    end,

    detach = function(self, buff)
        buff.target:adjustInvisibility(-1)
        buff.target:observe()
    end,

    act = function(self, buff)
        buff:spend(2) -- Actor.TICK * 2
        local target = buff.target
        if target:isAlive() then
            self.data.left = (self.data.left or 0) - 1
            if self.data.left <= 0 or target:visibleEnemies() > 0 then
                buff:detach()
            end
        else
            buff:detach()
        end
    end,

    -- java entry: CharUtils.refreshShadows
    prolong = function(self, buff)
        self.data.left = 2
    end,

    charSpriteStatus = function(self, buff)
        return "INVISIBLE"
    end
}
