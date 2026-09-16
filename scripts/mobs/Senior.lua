--[[
  Senior - monk elder, City boss floor trash (batch 17b, was
  actors/mobs/Senior.java). Monk stats with a heavier hit and a 1/10
  stun proc on top of the disarm.
]]
local RPD = require "scripts/lib/commonClasses"
local mob = require "scripts/lib/mob"

return mob.init{
    attackProc = function(self, enemy, dmg)
        if enemy ~= nil then
            if math.random(10) == 1 then
                RPD.Buffs.Buff:prolong(enemy, "Stun", 1.1)
            end
            RPD.CharUtils:disarm(self, enemy)
        end
        return dmg
    end
}
