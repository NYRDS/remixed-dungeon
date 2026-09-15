--[[
    mob lua migration batch 16b: replaces java RatKing
    Sleeps in its sewer room; wakes on first talk, gets angrier on further
    talks (any damage angers it instantly), then turns hostile and hunts.
    The anger gates (damage immunity, buff immunity while friendly) run here
    via the onBlockDamage/onAllowBuff CustomMob hooks.
]]

local RPD = require "scripts/lib/commonClasses"

local mob = require "scripts/lib/mob"

local ANGER_LIMIT = 2

local function angerOf(mobRef)
    local data = mob.restoreData(mobRef)
    return data.anger or 0
end

return mob.init({
    interact = function(self, chr)
        local data  = mob.restoreData(self)
        local anger = data.anger or 0

        if anger >= ANGER_LIMIT then
            return false
        end

        if not data.awake then
            self:say("RatKing_Info1")
            RPD.setAi(self, "Wandering")
            data.awake = true
        else
            anger = anger + 1
            data.anger = anger

            if anger < ANGER_LIMIT then
                self:say("RatKing_Info2")
            else
                self:setFraction(RPD.Fraction.DUNGEON)
                RPD.setAi(self, "Hunting")
                self:yell("RatKing_Info3")
            end
        end

        mob.storeData(self, data)

        return true
    end,

    blockDamage = function(self, dmg, src)
        if angerOf(self) < ANGER_LIMIT then
            local data = mob.restoreData(self)
            data.anger = ANGER_LIMIT
            mob.storeData(self, data)
            return true
        end
        return false
    end,

    allowBuff = function(self, buff)
        return angerOf(self) >= ANGER_LIMIT
    end,

    die = function(self, cause)
        self:say("RatKing_Died")
        RPD.Dungeon.level:drop(RPD.item("RatKingCrown"), self:getPos())
    end
})
