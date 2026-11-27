---
--- WandOfPoison: poisons the victim (was wands/WandOfPoison.java).
---
local RPD  = require "scripts/lib/commonClasses"
local wand = require "scripts/lib/wand"

return wand.init{
    desc = function()
        return {
            fx = "poison",
        }
    end,
    zap = function(self, item, cell, victim)
        if victim ~= nil then
            RPD.affectBuff(victim, "Poison", RPD.CharUtils:durationFactor(victim) * (5 + item:effectiveLevel()))
        else
            RPD.GLog:i(RPD.textById("WandOfPoison_Info1"), {})
        end
    end,
}
