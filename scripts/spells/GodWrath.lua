--
-- God's Wrath: a bolt of divine judgement - heavy holy damage to any single target
--

local RPD = require "scripts/lib/commonClasses"

local spell = require "scripts/lib/spell"

return spell.init{
    desc = function()
        return {
            image         = 4,
            imageFile     = "spellsIcons/priest.png",
            name          = "GodWrath_Name",
            info          = "GodWrath_Info",
            magicAffinity = "Priest",
            targetingType = "char_not_self",
            level         = 4,
            castTime      = 1.5,
            spellCost     = 9,
            cooldown      = 15
        }
    end,

    castOnChar = function(self, spell, caster, target)
        if caster:friendly(target) then
            return false
        end

        local skill = caster:skillLevel()
        local damage = 10 + 5 * skill

        -- bolt visual only (damage 0): the smite itself is holy, applied below
        RPD.CharUtils:lightningProc(caster, target:getPos(), 0)

        target:getSprite():emitter():burst(RPD.Sfx.ShaftParticle.FACTORY, 6)
        target:damage(damage, caster)
        RPD.playSound("snd_lightning")
        return true
    end
}
