---
--- Freeze: frost + slow on the char at the aimed cell.
--- Ported from java FreezeGlobe (data-spells migration).
---

local RPD = require "scripts/lib/commonClasses"

local Ballistica = RPD.Ballistica

local spell = require "scripts/lib/spell"

return spell.init{
    desc  = function ()
        return {
            image         = 3,
            imageFile     = "spellsIcons/elemental.png",
            name          = "FreezeGlobe_Name",
            info          = "FreezeGlobe_Info",
            magicAffinity = "Elemental",
            targetingType = "cell",
            level         = 3,
            castTime      = 1,
            spellCost     = 4,
            cooldown      = 1
        }
    end,

    castOnCell = function(self, spell, chr, cell)
        if not RPD.Dungeon.level:cellValid(cell) then
            return false
        end

        if Ballistica:cast(chr:getPos(), cell, false, true) == cell then
            local ch = RPD.Actor:findChar(cell)
            if ch then
                ch:getSprite():emitter():burst(RPD.Sfx.SnowParticle.FACTORY, 5)
                ch:getSprite():burst(0xFF99FFFF, 3)

                local factor = RPD.CharUtils:durationFactor(ch)
                RPD.affectBuff(ch, "Frost", factor * 5)
                RPD.affectBuff(ch, "Slow", factor * 10)
                RPD.playSound("snd_shatter")

                -- true = spell took effect and the cast is paid for;
                -- a beam that hit empty ground costs nothing (java parity)
                return true
            end
        end
        return false
    end
}
