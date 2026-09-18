---
--- Armor: Earthroot armor - absorbs damage into the buff level pool while
--- the target stays on the cell (was Earthroot.Armor inner java buff).
--- Pool rides buffLevel (raw level()/level(x), raise-only callers guard
--- themselves - Entanglement glyph).
---
local RPD  = require "scripts/lib/commonClasses"
local buff = require "scripts/lib/buff"

return buff.init{
    desc  = function ()
        return {
            icon          = 20, -- BuffIndicator.ARMOR
            name          = "EarthrootBuff_Name",
            info          = "EarthrootBuff_Info",
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

        if target:getPos() ~= pos then
            buff:detach()
        end
        buff:spend(1) -- STEP
    end,

    defenceProc = function(self, buff, enemy, damage)
        local lvl = buff:level()
        if damage >= lvl then
            buff:detach()
            return damage - lvl
        else
            buff:level(lvl - damage)
            return 0
        end
    end
}
