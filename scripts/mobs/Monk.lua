--[[
  Monk - City monk (batch 17b, was actors/mobs/Monk.java).
  Halftime attacks; disarms on hit (1/6 per slot, knuckles and cursed
  gear stay). The java "kick" actMeleeAttack override was dead code -
  mob AI attacks via doAttack - so it is not migrated.
  Amok/Terror immunity + FOOD loot live in the json.
]]
local RPD = require "scripts/lib/commonClasses"
local mob = require "scripts/lib/mob"

return mob.init{
    attackProc = function(self, enemy, dmg)
        if enemy ~= nil then
            RPD.CharUtils:disarm(self, enemy)
        end
        return dmg
    end
}
