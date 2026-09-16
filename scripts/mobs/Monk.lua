--[[
  Monk - City monk (batch 17b, was actors/mobs/Monk.java).
  Halftime attacks; disarms on hit (1/6 per slot, knuckles and cursed
  gear stay). Kick extra anim on half the attacks - the java override
  lived on actMeleeAttack, a hero-only path, so it never fired for the
  mob; here it rides the doAttack hook and actually plays.
  Amok/Terror immunity + FOOD loot live in the json.
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
        return kickAttack(self, enemy, 0.5)
    end,
    attackProc = function(self, enemy, dmg)
        if enemy ~= nil then
            RPD.CharUtils:disarm(self, enemy)
        end
        return dmg
    end
}
