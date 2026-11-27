---
--- ConfusionGas: vertigo per covered cell (was ConfusionGas.java).
---
local RPD  = require "scripts/lib/commonClasses"
local blob = require "scripts/lib/blobbase"

return blob.init{
    desc = function()
        return {
            mode     = "diffusion",
            tileDesc = "ConfusionGas_Info",
        }
    end,

    affect = function(self, gas, cells)
        for i = 1, cells.length do
            local ch = RPD.Actor:findChar(cells[i])
            if ch ~= nil then
                RPD.Buffs.Buff:prolong(ch, "Vertigo", RPD.CharUtils:durationFactor(ch) * 10)
            end
        end
    end,
}
