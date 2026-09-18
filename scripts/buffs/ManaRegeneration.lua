---
--- ManaRegeneration: skill point tick, rate scaled by manaRegenerationBonus
--- buffs (was actors/buffs/ManaRegeneration.java)
---
local RPD  = require "scripts/lib/commonClasses"
local buff = require "scripts/lib/buff"

local Facilitations = luajava.bindClass("com.watabou.pixeldungeon.Facilitations")

local REGENERATION_DELAY = 20

return buff.init{
    desc  = function ()
        return {
            icon          = -1, -- no indicator icon
            name          = "ManaRegenerationBuff_Name",
            info          = "ManaRegenerationBuff_Info",
        }
    end,

    act = function(self, buff)
        local target = buff.target
        if not target:isAlive() then
            buff:deactivateActor()
            return
        end

        if not target:level():isSafe() then
            target:accumulateSkillPoints(1)
        end

        local bonus = target:manaRegenerationBonusSum()
        if RPD.Dungeon:isFacilitated(Facilitations.FAST_MANA_REGENERATION) then
            bonus = bonus + 10
        end

        buff:spend(REGENERATION_DELAY / math.pow(1.2, bonus))
    end
}
