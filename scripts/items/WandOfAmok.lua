---
--- WandOfAmok: victims turn on their allies; the hero gets vertigo instead
--- (was wands/WandOfAmok.java).
---
local RPD  = require "scripts/lib/commonClasses"
local wand = require "scripts/lib/wand"

return wand.init{
    desc = function()
        return {
            fx = "purple",
        }
    end,
    zap = function(self, item, cell, victim)
        if victim ~= nil then
            if victim:getEntityKind() == "Hero" then
                RPD.affectBuff(victim, "Vertigo", RPD.CharUtils:durationFactor(victim) * 10)
            else
                RPD.affectBuff(victim, "Amok", 3 + item:effectiveLevel())
            end
        else
            RPD.GLog:i(RPD.textById("WandOfAmok_Info1"), {})
        end
    end,
}
