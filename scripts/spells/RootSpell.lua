---
--- Root: roots the targeted char for 10 turns.
--- Ported from java RootSpell (data-spells migration).
---

local RPD = require "scripts/lib/commonClasses"

local spell = require "scripts/lib/spell"

return spell.init{
    desc  = function ()
        return {
            image         = 2,
            imageFile     = "spellsIcons/elemental.png",
            name          = "RootSpell_Name",
            info          = "RootSpell_Info",
            magicAffinity = "Elemental",
            targetingType = "char_not_self",
            level         = 2,
            castTime      = 1,
            spellCost     = 2,
            cooldown      = 1
        }
    end,

    castOnChar = function(self, spell, chr, target)
        if target and target:valid() then
            target:getSprite():emitter():burst(RPD.Sfx.EarthParticle.FACTORY, 5)
            target:getSprite():burst(0xFF99FFFF, 3)

            RPD.Buffs.Buff:prolong(target, "Roots", 10)
            RPD.playSound("snd_puff")

            return true
        end
        return false
    end
}
