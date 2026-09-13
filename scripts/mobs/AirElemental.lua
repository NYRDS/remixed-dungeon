local RPD = require "scripts/lib/commonClasses"
local mob = require "scripts/lib/mob"

-- java AirElemental: flying ranged pusher. Can only strike through a clear
-- ballistica within its skill band (json attackRange 3) and the java
-- getCloser inversion becomes the Scorpio-style state pair: too close
-- while hunting -> flee, far enough -> hunt again (fleeing also keeps it
-- from blasting an adjacent enemy, which the java canAttack forbade).
-- Its WindGust zap pushes the first char on the trace. Stats scale with
-- the spawn depth (json holds the depth-1 defaults).
return mob.init{
    stats = function(self)
        local d = RPD.Dungeon.depth
        local ht = d * 3 + 1
        self:ht(ht)
        self:hp(ht)
        self:STR(14)
        self:setBaseDefenseSkill(d * 2 + 1)
        self:setBaseAttackSkill((d * 2 + 1) * 2 + 1)
        self:setExpForKill(d + 1)
        self:setMaxLvl(d + 2)
        self:setDr(math.floor((d + 1) / 5))
        self:setDmgMax(math.floor(ht / 4))
        -- java ctor: setSkillLevel(3 + lvl/10)
        self:setSkillLevel(3 + math.floor(self:lvl() / 10))
    end,

    act = function(self)
        local enemy = self:getEnemy()
        if enemy == nil then
            return
        end
        local tag = self:getState():getTag()
        local dist = RPD.Dungeon.level:distance(self:getPos(), enemy:getPos())
        if tag == "HUNTING" then
            if dist < 2 then
                RPD.setAi(self, "Fleeing")
            end
        elseif tag == "FLEEING" then
            if self.enemySeen and dist >= 2 then
                RPD.setAi(self, "Hunting")
            end
        end
    end,

    zapProc = function(self, enemy, dmg)
        if enemy ~= nil then
            RPD.SpellFactory:getSpellByName("WindGust"):cast(self, enemy:getPos())
        end
        return dmg
    end,
}
