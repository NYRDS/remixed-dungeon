local RPD = require "scripts/lib/commonClasses"
local mob = require "scripts/lib/mob"

-- java Shaman (batch 9): lightning bolt - double damageRoll through
-- CharUtils.lightningProc, no base zap damage; flees twice (at 2/3 and 1/3
-- hp, halving the hit that pushed it) and resumes hunting once >2 cells from
-- the enemy. Carries a scroll 33% of the time.
return mob.init{
    stats = function(self)
        -- ctor-only roll, persisted (stats re-runs on every restore)
        local data = mob.restoreData(self)
        if data.rolled then
            return
        end
        if not self:getBelongings():isBackpackEmpty() then
            -- pre-migration saves already carry the java-rolled scroll
            data.rolled = true
            return
        end
        data.rolled = true

        -- java loot() hero-level gate
        local hero = RPD.Dungeon.hero
        if not self:isBoss() and hero:lvl() > self:getMaxLvl() + 2 + self:lvl() then
            return
        end

        if math.random(100) <= 33 then
            self:collect(RPD.Treasury:getLevelTreasury():random("SCROLL"))
        end
    end,

    zap = function(self, enemy)
        if enemy == nil or not enemy:valid() then
            return false
        end
        if self:zapHit(enemy) then
            RPD.CharUtils:lightningProc(self, enemy:getPos(), self:damageRoll() * 2)
            RPD.CharUtils:checkDeathReport(self, enemy, RPD.textById("Shaman_Killed"))
        elseif math.random() < 0.1 then
            self:yell("Shaman_ZapMiss")
        end
        return true
    end,

    defenceProc = function(self, enemy, dmg)
        local data = mob.restoreData(self)
        if self:hp() > 2 * self:ht() / 3 and (data.fleeState or 0) < 1 then
            RPD.setAi(self, "Fleeing")
            data.fleeState = 1
            return math.floor(dmg / 2)
        end

        if self:hp() > self:ht() / 3 and (data.fleeState or 0) < 2 then
            RPD.setAi(self, "Fleeing")
            data.fleeState = 2
            return math.floor(dmg / 2)
        end
        return dmg
    end,

    act = function(self)
        -- java getFurther override: stop fleeing once >2 cells from the enemy
        local enemy = self:getEnemy()
        if enemy ~= nil
                and self:getState():getTag() == "FLEEING"
                and self:distance(enemy) > 2 then
            RPD.setAi(self, "Hunting")
        end
    end
}
