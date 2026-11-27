---
--- Web: sticky roots per covered cell (was Web.java). Seeding raises cells to
--- the amount (seedMode = "max"), the decay walk rides the java executor.
---
local RPD  = require "scripts/lib/commonClasses"
local blob = require "scripts/lib/blobbase"

return blob.init{
    desc = function()
        return {
            mode     = "decay",
            seedMode = "max",
            tileDesc = "Web_Info",
        }
    end,

    affect = function(self, gas, cells)
        for i = 1, cells.length do
            local ch = RPD.Actor:findChar(cells[i])
            if ch ~= nil then
                local rootable = not ch:immunities():contains("Web")
                if "SpiderArmor" == ch:getItemFromSlot(RPD.Slots.armor):getEntityKind() then
                    rootable = false
                end
                if rootable then
                    RPD.Buffs.Buff:prolong(ch, "Roots", RPD.Actor.TICK)
                end
            end
        end
    end,
}
