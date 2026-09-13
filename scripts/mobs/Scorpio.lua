local RPD = require "scripts/lib/commonClasses"
local mob = require "scripts/lib/mob"

-- java Scorpio (batch 7): ranged-only hunter that never closes in
-- (canDoOnlyRangedAttack gate + getCloser inverted to getFurther while Hunting),
-- zap half the time cripples, ctor rolls a potion/meat carry.
-- NB: callback args named self/mob are the java Char, `mob` in upvalue position
-- is the script library (mob.restoreData needs the java Char as its argument).
return mob.init{
    stats = function(self)
        -- ctor-only roll: 1/8 potion of healing, else 1/6 mystery meat.
        -- stats re-runs on every restore, so the roll is persisted in lua data.
        local data = mob.restoreData(self)
        if data.rolled then
            return
        end
        if not self:getBelongings():isBackpackEmpty() then
            -- pre-migration saves already carry the item their java rolled
            data.rolled = true
            return
        end
        data.rolled = true
        if math.random(8) == 1 then
            self:collect(RPD.item("PotionOfHealing"))
        elseif math.random(6) == 1 then
            self:collect(RPD.item("MysteryMeat"))
        end
    end,

    spawn = function(self, level)
        self:setViewDistance(level:getViewDistance() + 1)
    end,

    act = function(self)
        -- retreat unless the enemy is in a clean ranged line; resume hunting
        -- when one opens up (the Hunting/Fleeing pair replaces the java
        -- Hunting-always-getFurther inversion)
        local enemy = self:getEnemy()
        if enemy == nil then
            return
        end
        local tag = self:getState():getTag()
        if tag == "HUNTING" then
            if not self.enemySeen or not RPD.CharUtils:canDoOnlyRangedAttack(self, enemy) then
                RPD.setAi(self, "Fleeing")
            end
        elseif tag == "FLEEING" then
            if self.enemySeen and RPD.CharUtils:canDoOnlyRangedAttack(self, enemy) then
                RPD.setAi(self, "Hunting")
            end
        end
    end,

    zapProc = function(self, enemy, dmg)
        if enemy ~= nil and math.random(2) == 1 then
            RPD.Buffs.Buff:prolong(enemy, "Cripple", RPD.Buffs.Cripple.DURATION)
        end
        return dmg
    end,
}
