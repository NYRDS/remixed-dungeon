---
--- ToxicGas: the diffusion gas - poison damage per covered cell, the
--- paralytic cancel pair rides the java executor (cancelsWith).
---
local RPD  = require "scripts/lib/commonClasses"
local blob = require "scripts/lib/blobbase"

return blob.init{
    desc = function()
        return {
            mode        = "diffusion",
            cancelsWith = "ParalyticGas",
            tileDesc    = "ToxicGas_Info",
        }
    end,

    affect = function(self, gas, cells)
        local depth = RPD.Dungeon.depth
        local levelDamage = 5 + depth * 5

        for i = 1, cells.length do
            local pos = cells[i]

            local ch = RPD.Actor:findChar(pos)
            if ch ~= nil then
                local damage = math.floor((ch:ht() + levelDamage) / 40)
                if RPD.Random:Int(40) < (ch:ht() + levelDamage) % 40 then
                    damage = damage + 1
                end
                ch:damage(damage, gas)
            end

            local heap = RPD.Dungeon.level:getHeap(pos)
            if heap ~= nil then
                heap:poison()
            end
        end
    end,

    onHeroDeath = function(self, gas)
        RPD.Badges:validateDeathFromGas()
        RPD.Dungeon:fail(RPD.JavaUtils:format(
            RPD.ResultDescriptions:getDescription(RPD.ResultReason.GAS), { RPD.Dungeon.depth }))
        RPD.glogn(RPD.textById("ToxicGas_Info1"))
    end,
}
