---
--- Generated by EmmyLua(https://github.com/EmmyLua)
--- Created by mike.
--- DateTime: 21.03.19 22:57
---

local RPD = require "scripts/lib/commonClasses"

local spell = require "scripts/lib/spell"

return spell.init{
    desc  = function ()
        return {
            image         = 0,
            imageFile     = "spellsIcons/rogue.png",
            name          = "CloakSpell_Name",
            info          = "CloakSpell_Info",
            magicAffinity = "Rogue",
            targetingType = "self",
            level         = 1,
            spellCost     = 1,
            cooldown      = 1,
            castTime      = 0.5
        }
    end,
    cast = function(self, spell, caster, cell)
        local duration = 20 --caster:skillLevel() * 20

        if caster:visibleEnemies() > 0 then
            RPD.glogn("CloakSpell_EnemiesNearby")
            return false
        end

        RPD.affectBuff(caster,"Cloak", duration)

        return true
    end}
