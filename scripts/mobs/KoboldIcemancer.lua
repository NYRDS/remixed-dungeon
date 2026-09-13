local RPD = require "scripts/lib/commonClasses"
local mob = require "scripts/lib/mob"

-- java KoboldIcemancer (batch 9): ice bolt deals NO damage - 1/2 Slow(1) on
-- hit is the whole effect. Carries a potion 83% of the time.
return mob.init{
    stats = function(self)
        -- ctor-only roll, persisted (stats re-runs on every restore)
        local data = mob.restoreData(self)
        if data.rolled then
            return
        end
        if not self:getBelongings():isBackpackEmpty() then
            -- pre-migration saves already carry the java-rolled potion
            data.rolled = true
            return
        end
        data.rolled = true

        -- java loot() hero-level gate
        local hero = RPD.Dungeon.hero
        if not self:isBoss() and hero:lvl() > self:getMaxLvl() + 2 + self:lvl() then
            return
        end

        if math.random(100) <= 83 then
            self:collect(RPD.Treasury:getLevelTreasury():random("POTION"))
        end
    end,

    zap = function(self, enemy)
        if enemy == nil or not enemy:valid() then
            return false
        end
        if self:zapHit(enemy) then
            if math.random(2) == 1 then
                RPD.Buffs.Buff:prolong(enemy, "Slow", 1)
            end
            RPD.CharUtils:checkDeathReport(self, enemy, RPD.textById("KoboldIcemancer_Killed"))
        end
        return true
    end
}
