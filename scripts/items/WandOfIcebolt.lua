---
--- WandOfIcebolt: frost damage + frost/slow; snow idle particles; always
--- known, outside the wood handler (was common/../icecaves/WandOfIcebolt.java).
---
local RPD  = require "scripts/lib/commonClasses"
local wand = require "scripts/lib/wand"

return wand.init{
    desc = function()
        return {
            image      = 12,
            imageFile  = "items/wands.png",
            identified = true,
        }
    end,
    zap = function(self, item, cell, victim)
        if victim ~= nil then
            local level = item:effectiveLevel()
            victim:damage(RPD.Random:Int(3 + level, 4 + level * 2), item)
            victim:getSprite():burst(0xFF99FFFF, level / 2 + 3)
            RPD.affectBuff(victim, "Frost", RPD.CharUtils:durationFactor(victim) * 5 / 2 + level)
            RPD.affectBuff(victim, "Slow", RPD.CharUtils:durationFactor(victim) * 10 / 2 + level)
            wand.selfKillReport(self, item, "WandOfIcebolt_Info1")
        end
    end,
    emitter = function(self, item)
        return "SnowParticle"
    end,
    emitterInterval = function(self, item)
        return 0.1
    end,
}
