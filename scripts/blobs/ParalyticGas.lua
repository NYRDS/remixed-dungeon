---
--- ParalyticGas: paralysis per covered cell (was ParalyticGas.java).
---
local RPD  = require "scripts/lib/commonClasses"
local blob = require "scripts/lib/blobbase"

return blob.init{
    desc = function()
        return {
            mode     = "diffusion",
            tileDesc = "ParalyticGas_Info",
        }
    end,

    affect = function(self, gas, cells)
        for i = 1, cells.length do
            local ch = RPD.Actor:findChar(cells[i])
            if ch ~= nil then
                RPD.Buffs.Buff:prolong(ch, "Paralysis", RPD.CharUtils:durationFactor(ch) * 10)
            end
        end
    end,
}
