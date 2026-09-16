-- BossProbe: permanent debug fixture for the 17a boss groundwork.
-- isBoss json key + every new combat hook, each with an observable side
-- effect so a headless run can verify the whole surface:
--   notice       -> yell "BossProbe noticed you"
--   canAttack    -> reach 3 cells despite attackRange 1 in json
--   doAttack     -> odd attacks become a no-damage PUMP (script took it)
--   attackSkill  -> flat 40 (json says 11)
--   damageRoll   -> flat 3 (json says 5-8)
--   zapProc      -> beamStrike pierces the whole ray
--   die          -> boss die-flow (banner/music/unseal) + yell + key drop
-- Hook convention: arg 1 = the java char itself (Succubus/Scorpio style).

local RPD = require "scripts/lib/commonClasses"
local mob = require "scripts/lib/mob"

local attacks = 0

return mob.init{
    notice = function(self)
        self:yell("BossProbe noticed you")
    end,

    canAttack = function(self, enemy)
        return RPD.Dungeon.level:distance(self:getPos(), enemy:getPos()) <= 3
    end,

    doAttack = function(self, enemy)
        attacks = attacks + 1
        if attacks % 2 == 1 then
            self:showStatus(0xFF0000, "PUMP")
            self:spend(1)
            return true
        end
        return false
    end,

    attackSkill = function(self, target)
        return 40
    end,

    damageRoll = function(self)
        return 3
    end,

    zapProc = function(self, enemy, damage)
        RPD.CharUtils:beamStrike(self, enemy, self:getPos(), "Eye_Kill", 2)
        return damage
    end,

    die = function(self, cause)
        self:yell("BossProbe dies")
        RPD.CharUtils:validateBossSlain("BOSS_SLAIN_1")
        return nil
    end,
}
