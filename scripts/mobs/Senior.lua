--[[
  Senior - monk elder, City boss floor trash (batch 17b, was
  actors/mobs/Senior.java). Monk stats with a heavier hit and a 1/10
  stun proc on top of the disarm. Kick anim on 3/10 attacks - same
  story as Monk: the java override sat on the hero-only
  actMeleeAttack path; here it rides doAttack and plays.
]]
local RPD = require "scripts/lib/commonClasses"
local mob = require "scripts/lib/mob"

local function kickAttack(self, enemy, chance)
    if enemy == nil or math.random() >= chance then
        return false
    end

    self:setEnemy(enemy)
    self:spend(self:attackDelay())
    self:getSprite():turnTo(self:getPos(), enemy:getPos())

    if RPD.Dungeon:isCellVisible(enemy:getPos()) and not RPD.Dungeon:realtime() then
        RPD.CharUtils:extraAttack(self, "kick")
    else
        self:onAttackComplete()
    end
    return true
end

return mob.init{
    doAttack = function(self, enemy)
        return kickAttack(self, enemy, 0.3)
    end,
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
