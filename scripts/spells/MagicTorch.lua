---
--- Magic torch: light + a small flame burst on the caster.
--- Ported from java MagicTorch (data-spells migration).
---

local RPD = require "scripts/lib/commonClasses"

local spell = require "scripts/lib/spell"

return spell.init{
    desc  = function ()
        return {
            image         = 0,
            imageFile     = "spellsIcons/common.png",
            name          = "MagicTorch_Name",
            info          = "MagicTorch_Info",
            magicAffinity = "Common",
            targetingType = "self",
            level         = 1,
            castTime      = 1,
            spellCost     = 1,
            cooldown      = 1
        }
    end,

    cast = function(self, spell, chr)
        -- the java base targeting/spend already ran in CustomSpell.cast
        RPD.affectBuff(chr, "Light", 80)

        chr:getSprite():centerEmitter():start(RPD.Sfx.FlameParticle.FACTORY, 0.2, 3)
        return true
    end
}
