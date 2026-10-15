--
-- Holy water splash: scalds and slows the undead on and around the target cell
--

local RPD = require "scripts/lib/commonClasses"

local spell = require "scripts/lib/spell"

return spell.init{
    desc = function()
        return {
            image         = 1,
            imageFile     = "spellsIcons/priest.png",
            name          = "HolyWater_Name",
            info          = "HolyWater_Info",
            magicAffinity = "Priest",
            targetingType = "cell",
            level         = 2,
            castTime      = 1,
            spellCost     = 5,
            cooldown      = 3
        }
    end,

    castOnCell = function(self, spell, caster, cell)
        local level = RPD.Dungeon.level
        if not level:cellValid(cell) then
            return false
        end

        local skill = caster:skillLevel()

        local splash = function(c)
            local victim = RPD.Actor:findChar(c)
            if victim ~= nil and victim ~= caster and not caster:friendly(victim) then
                if victim:isUndead() then
                    victim:damage(4 + 3 * skill, caster)
                    RPD.affectBuff(victim, RPD.Buffs.Slow, 3)
                end
            end
        end

        splash(cell)
        RPD.forCellsAround(cell, splash)

        RPD.Sfx.CellEmitter:center(cell):burst(RPD.Sfx.Speck:factory(RPD.Sfx.Speck.STEAM), 8)
        RPD.playSound("snd_water")
        return true
    end
}
