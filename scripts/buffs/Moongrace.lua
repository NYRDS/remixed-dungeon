---
--- Moongrace: the pressed plant's skill-point trickle (was
--- mechanics/buffs/Moongrace.java). The anchored cell rides the script data.
---
local RPD  = require "scripts/lib/commonClasses"
local buff = require "scripts/lib/buff"

return buff.init{
    desc  = function ()
        return {
            icon          = 51, -- BuffIndicator.MOONGRACE
            name          = "MoongraceBuff_Name",
            info          = "MoongraceBuff_Info",
        }
    end,

    attachTo = function(self, buff, target)
        self.data = { pos = target:getPos() }
        return true
    end,

    act = function(self, buff)
        local target = buff.target
        local pos = self.data and self.data.pos
        if target:getPos() ~= pos or target:getSkillPoints() >= target:getSkillPointsMax() then
            buff:detach()
            return
        end
        target:accumulateSkillPoints(math.max(math.floor(target:getSkillPointsMax() / 10), 1))
        if RPD.Dungeon:isCellVisible(pos) then
            RPD.Sfx.CellEmitter:get(pos):start(RPD.Sfx.ShaftParticle.FACTORY, 0.2, 3)
        end
        buff:spend(5) -- STEP
    end,
}
