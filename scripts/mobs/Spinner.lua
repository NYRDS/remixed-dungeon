local RPD = require "scripts/lib/commonClasses"
local mob = require "scripts/lib/mob"

-- java Spinner (batch 7): poisons on hit then breaks off, leaving a web trail
-- while fleeing; resumes the hunt once the victim is no longer poisoned.
return mob.init{
    act = function(self)
        -- java Spinner.act ran after the AI step; this pre-AI check flips the
        -- state one tick earlier (same conditions)
        local enemy = self:getEnemy()
        if enemy == nil then
            return
        end
        if self:getState():getTag() == "FLEEING"
           and not self:hasBuff("Terror")
           and self.enemySeen
           and not enemy:hasBuff("Poison") then
            RPD.setAi(self, "Hunting")
        end
    end,

    attackProc = function(self, enemy, dmg)
        -- 1/2 chance to poison for 7-8 turns (Random.Int(7,9) is [7,9)) and flee
        if enemy ~= nil and math.random(2) == 1 then
            local factor = RPD.Buffs.Poison:durationFactor(enemy)
            RPD.Buffs.Buff:affect(enemy, "Poison", math.random(7, 8) * factor)
            RPD.setAi(self, "Fleeing")
        end
        return dmg
    end,

    move = function(self, cell)
        -- web at the cell being left (the hook runs before the position changes)
        if self:getState():getTag() == "FLEEING" then
            RPD.placeBlob(RPD.Blobs.Web, self:getPos(), math.random(5, 6))
        end
    end,
}
