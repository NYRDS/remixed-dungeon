---
--- Health: Sungrass healing - heals while the target stays on the cell,
--- detaches on move or at full hp (was Sungrass.Health inner java buff)
---
local RPD  = require "scripts/lib/commonClasses"
local buff = require "scripts/lib/buff"

return buff.init{
    desc  = function ()
        return {
            icon          = 19, -- BuffIndicator.HEALING
            name          = "SungrassBuff_Name",
            info          = "SungrassBuff_Info",
        }
    end,

    attachTo = function(self, buff, target)
        return true
    end,

    attached = function(self, buff)
        self.data = { pos = buff.target:getPos() }
    end,

    act = function(self, buff)
        local target = buff.target
        local pos = self.data and self.data.pos

        if target:getPos() ~= pos or target:hp() >= target:ht() then
            buff:detach()
        else
            target:heal(math.max(math.floor(target:ht() / 10), 1), buff)

            if RPD.Dungeon:isCellVisible(pos) then
                RPD.Sfx.CellEmitter:get(pos):start(RPD.Sfx.ShaftParticle.FACTORY, 0.2, 3)
            end
        end
        buff:spend(5) -- STEP
    end
}
