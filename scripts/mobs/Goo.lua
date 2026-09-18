--[[
  Goo - Sewer boss (batch 17c-1, was actors/mobs/Goo.java).
  Boss die-flow (banner/music/unseal/key) rides the isBoss json key.
  Soaks in water to heal, pumps up (1/3 of adjacent turns) for one
  heavy strike with reach two, leaves ooze on hit. The pumped strike
  at range two is a blank zap that un-pumps - faithful to the java
  flow. pumpedUp rides the script data (serpent round-trips the save).
]]
local RPD = require "scripts/lib/commonClasses"
local mob = require "scripts/lib/mob"

local PUMP_UP_DELAY = 2.2
local function pumped(self)
    return mob.restoreData(self).pumpedUp
end

local function setPumped(self, value)
    mob.restoreData(self).pumpedUp = value
end

return mob.init{
    damageRoll = function(self)
        if pumped(self) then
            return math.random(7, 21)
        end
        return math.random(4, 11)
    end,

    attackSkill = function(self, target)
        if pumped(self) then
            return 26
        end
        return 11
    end,

    act = function(self)
        -- nil return: the java act runs as usual
        if self:hp() < self:ht() and RPD.Dungeon.level.water[self:getPos() + 1] then
            self:heal(1, self)
        end
    end,

    canAttack = function(self, enemy)
        if pumped(self) then
            return self:distance(enemy) <= 2
        end
        return nil
    end,

    attackProc = function(self, enemy, dmg)
        if enemy ~= nil then
            if math.random(3) == 1 then
                RPD.Buffs.Buff:affect(enemy, "Ooze")
                enemy:getSprite():burst(0x000000, 5)
            end
            if pumped(self) then
                RPD.shakeCamera(3, 0.2)
            end
        end
        return dmg
    end,

    doAttack = function(self, enemy)
        if enemy == nil or pumped(self) or math.random(3) > 1 then
            return false
        end

        setPumped(self, true)
        self:spend(PUMP_UP_DELAY)
        self:getSprite():playExtra("pump")

        if RPD.CharUtils:isVisible(self) then
            self:getSprite():showStatus(RPD.CharSprite.NEGATIVE, RPD.textById("Goo_StaInfo1"))
            RPD.GLog:n(RPD.textById("Goo_Info1"), {})
        end
        return true
    end,

    getCloser = function(self, target, ignorePets)
        setPumped(self, false)
        return false
    end,

    zap = function(self, enemy)
        -- pumped reach-two strike is a blank zap that un-pumps
        setPumped(self, false)
        return true
    end,

    die = function(self, cause)
        RPD.CharUtils:validateBossSlain("BOSS_SLAIN_1")
        self:yell(RPD.textById("Goo_Info2"))
    end,

    notice = function(self)
        self:yell(RPD.textById("Goo_Info3"))
    end,
}
