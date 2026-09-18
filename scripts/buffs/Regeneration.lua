---
--- Regeneration: hp tick, rate scaled by regenerationBonus buffs
--- (was actors/buffs/Regeneration.java; bonus sum crosses via
--- Char:regenerationBonusSum since lua cannot drive forEachBuff)
---
local RPD  = require "scripts/lib/commonClasses"
local buff = require "scripts/lib/buff"

local Facilitations = luajava.bindClass("com.watabou.pixeldungeon.Facilitations")

local REGENERATION_DELAY = 10

return buff.init{
    desc  = function ()
        return {
            icon          = -1, -- no indicator icon
            name          = "RegenerationBuff_Name",
            info          = "RegenerationBuff_Info",
        }
    end,

    attachTo = function(self, buff, target)
        return target:buffLevel("Regeneration") <= 0
    end,

    act = function(self, buff)
        local target = buff.target
        if not target:isAlive() then
            buff:deactivateActor()
            return
        end

        local bonus = target:regenerationBonusSum()
        if target:getEntityKind() == "Hero"
            and RPD.Dungeon:isFacilitated(Facilitations.FAST_REGENERATION) then
            bonus = bonus + 10
        end

        local healPoints = 1
        local healRate = math.pow(1.2, bonus)

        if healRate > REGENERATION_DELAY * 5 then
            healPoints = healPoints + math.floor(healRate / 5)
            healRate = REGENERATION_DELAY * 5
        end

        if not target:isStarving() and not target:level():isSafe() then
            target:heal(healPoints, buff)
        end

        buff:spend(REGENERATION_DELAY / healRate)
    end
}
