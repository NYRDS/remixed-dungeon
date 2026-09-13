local RPD = require "scripts/lib/commonClasses"
local mob = require "scripts/lib/mob"

-- java Acidic (batch 7): a Scorpio whose carapace splashes acid back at the
-- attacker (defenseProc reflect on top of the inherited Scorpio kit).
return mob.init{
    stats = function(self)
        local data = mob.restoreData(self)
        if data.rolled then
            return
        end
        if not self:getBelongings():isBackpackEmpty() then
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

    defenceProc = function(self, enemy, dmg)
        -- Random.IntRange(0, damage) is inclusive on both ends
        if enemy ~= nil then
            local reflect = math.random(0, dmg)
            if reflect > 0 then
                enemy:damage(reflect, self)
            end
        end
        return dmg
    end,
}
