---
--- MiasmaGas: poison + weakness + vertigo per covered cell, immune-safe (was
--- MiasmaGas.java).
---
local RPD  = require "scripts/lib/commonClasses"
local blob = require "scripts/lib/blobbase"

return blob.init{
    desc = function()
        return {
            mode     = "diffusion",
            tileDesc = "MiasmaGas_Info",
        }
    end,

    affect = function(self, gas, cells)
        for i = 1, cells.length do
            local ch = RPD.Actor:findChar(cells[i])
            if ch ~= nil then
                local immunities = ch:immunities()
                if not immunities:contains("ToxicGas") and not immunities:contains("GasesImmunity") then
                    local factor = RPD.CharUtils:durationFactor(ch)
                    RPD.Buffs.Buff:prolong(ch, "Poison", factor * 3)
                    RPD.Buffs.Buff:prolong(ch, "Weakness", factor * 40)
                    RPD.Buffs.Buff:prolong(ch, "Vertigo", factor * 10)
                end
            end
        end
    end,
}
