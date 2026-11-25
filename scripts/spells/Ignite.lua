---
--- Ignite: seeds a Fire blob at the aimed cell.
--- Ported from java Ignite (data-spells migration).
---

local RPD = require "scripts/lib/commonClasses"

local Ballistica = RPD.Ballistica

local spell = require "scripts/lib/spell"

return spell.init{
    desc  = function ()
        return {
            image         = 1,
            imageFile     = "spellsIcons/elemental.png",
            name          = "Ignite_Name",
            info          = "Ignite_Info",
            magicAffinity = "Elemental",
            targetingType = "cell",
            level         = 2,
            castTime      = 1,
            spellCost     = 2,
            cooldown      = 1
        }
    end,

    castOnCell = function(self, spell, chr, cell)
        if chr:level():cellValid(cell) then
            local target = Ballistica:cast(chr:getPos(), cell, true, true)

            RPD.Sfx.CellEmitter:center(target):burst(RPD.Sfx.FlameParticle.FACTORY, chr:skillLevel())

            RPD.placeBlob(RPD.Blobs.Fire, target, 5)
            return true
        end
        return false
    end
}
