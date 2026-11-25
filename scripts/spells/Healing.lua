---
--- Healing: restores 30% of the caster's max hp.
--- Ported from java Healing (data-spells migration).
---

local RPD = require "scripts/lib/commonClasses"

local spell = require "scripts/lib/spell"

return spell.init{
    desc  = function ()
        return {
            image         = 1,
            imageFile     = "spellsIcons/common.png",
            name          = "Healing_Name",
            info          = "Healing_Info",
            magicAffinity = "Common",
            targetingType = "self",
            level         = 3,
            castTime      = 1,
            spellCost     = 10,
            cooldown      = 1
        }
    end,

    cast = function(self, spell, chr)
        if chr:isAlive() then
            chr:heal(math.floor(chr:ht() * 0.3), spell)
            return true
        end
        return false
    end
}
