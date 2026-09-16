--[[
  Undead - skeleton warrior the Dwarf King raises at the pedestals
  (batch 17b, was King$Undead). No XP on kill, wanders until it spots
  the hero, rattle of bones when it falls. The java ToxicGas
  clearBlob-on-damage is dropped per Mike's ruling.
]]
local RPD = require "scripts/lib/commonClasses"
local mob = require "scripts/lib/mob"

return mob.init{
    attackProc = function(self, enemy, dmg)
        if enemy ~= nil and math.random(5) == 1 then
            RPD.Buffs.Buff:prolong(enemy, "Stun", 1)
        end
        return dmg
    end,
    die = function(self, cause)
        if RPD.CharUtils.isVisible(self) then
            RPD.playSound("snd_bones")
        end
    end
}
