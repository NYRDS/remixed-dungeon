local RPD = require "scripts/lib/commonClasses"
local mob = require "scripts/lib/mob"

-- java Warlock (batch 9): shadow bolt - base damage flow plus 1/2 Weakness,
-- blinks away when first pushed under 2/3 then 1/3 hp (halving the hit),
-- carries a potion 83% of the time.
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
            local eff = enemy:defenseProc(self, self:damageRoll())
            enemy:damage(eff, self)
            if math.random(2) == 1 then
                    RPD.Buffs.Buff:prolong(enemy, "Weakness", RPD.Buffs.Weakness:duration(enemy))
            end
            RPD.CharUtils:checkDeathReport(self, enemy, RPD.textById("Warlock_Killed"))
        end
        return true
    end,

    defenceProc = function(self, enemy, dmg)
        if enemy == nil then
            return dmg
        end
        if self:hp() > 2 * self:ht() / 3 and self:hp() - dmg / 2 < 2 * self:ht() / 3 then
            RPD.CharUtils:blinkAwayFrom(self, enemy, 2)
            return math.floor(dmg / 2)
        end

        if self:hp() > self:ht() / 3 and self:hp() - dmg / 2 < self:ht() / 3 then
            RPD.CharUtils:blinkAwayFrom(self, enemy, 3)
            return math.floor(dmg / 2)
        end
        return dmg
    end
}
