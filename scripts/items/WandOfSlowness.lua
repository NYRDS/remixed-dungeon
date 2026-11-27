---
--- WandOfSlowness: slows the victim (was wands/WandOfSlowness.java).
---
local RPD  = require "scripts/lib/commonClasses"
local wand = require "scripts/lib/wand"

return wand.init{
    desc = function()
        return {
            fx = "slowness",
        }
    end,
    zap = function(self, item, cell, victim)
        if victim ~= nil then
            RPD.affectBuff(victim, "Slow", RPD.CharUtils:durationFactor(victim) * 10 / 3 + item:effectiveLevel())
        else
            RPD.GLog:i(RPD.textById("WandOfSlowness_Info1"), {})
        end
    end,
}
