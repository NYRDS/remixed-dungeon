local RPD = require "scripts/lib/commonClasses"

local spell = require "scripts/lib/spell"

return spell.init{
    desc  = function ()
        return {
            image         = 1,
            imageFile     = "spellsIcons/doctor.png",
            name          = "CorpseExplosion_Name",
            info          = "CorpseExplosion_Info",
            magicAffinity = "PlagueDoctor",
            targetingType = "cell",
            level         = 3,
            castTime      = 2,
            spellCost     = 9,
            cooldown      = 10
        }
    end,

    castOnCell = function(self, spell, caster, cell)
        local level = RPD.Dungeon.level
        local heap = level:getHeap(cell)

        if heap and heap:peekByPrefix("Carcass"):valid() then
            -- Get the corpse's health value before consuming it
            local corpse = heap:peekByPrefix("Carcass")
            local corpseHealth = corpse:price() * 6

            -- Consume the corpse
            heap:pickUp()

            -- Create a toxic gas cloud at the location, scaled by corpse health and caster skill
            local gasStrength = math.floor(corpseHealth * 0.5 * caster:skillLevel())
            RPD.placeBlob(RPD.Blobs.ToxicGas, cell, gasStrength)

            -- Spread various gases to adjacent cells based on corpse health
            local level = RPD.Dungeon.level
            local adjacentCells = {}
            
            -- Get all adjacent cells (excluding diagonals)
            RPD.forCellsAround(cell, function(adjacentCell)
                if adjacentCell ~= cell and level:cellValid(adjacentCell) then
                    table.insert(adjacentCells, adjacentCell)
                end
            end)

            -- Distribute different types of gas to adjacent cells
            for i, adjCell in ipairs(adjacentCells) do
                local gasType = (i % 3) + 1 -- Cycle through 3 gas types
                
                if gasType == 1 then
                    -- Create Paralytic Gas
                    RPD.placeBlob(RPD.Blobs.ParalyticGas, adjCell, math.floor(gasStrength * 0.3))
                elseif gasType == 2 then
                    -- Create Confusion Gas
                    RPD.placeBlob(RPD.Blobs.ConfusionGas, adjCell, math.floor(gasStrength * 0.3))
                else
                    -- Create Flammable Gas
                    RPD.placeBlob(RPD.Blobs.FlammableGas, adjCell, math.floor(gasStrength * 0.3))
                end
            end

            -- Visual effect
            RPD.Sfx.CellEmitter:center(cell):burst(RPD.Sfx.ShadowParticle.CURSE, 10)

            -- Sound effect
            RPD.playSound("snd_cursed")

            return true
        else
            RPD.glogn("CorpseExplosion_NoCorpse")
            return false
        end
    end
}