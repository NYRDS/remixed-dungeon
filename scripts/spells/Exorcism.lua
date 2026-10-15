--
-- Exorcism: heavy single-target holy damage, doubled against the undead
--

local RPD = require "scripts/lib/commonClasses"

local spell = require "scripts/lib/spell"

return spell.init{
    desc = function()
        return {
            image         = 2,
            imageFile     = "spellsIcons/priest.png",
            name          = "Exorcism_Name",
            info          = "Exorcism_Info",
            magicAffinity = "Priest",
            targetingType = "char_not_self",
            level         = 3,
            castTime      = 1,
            spellCost     = 7,
            cooldown      = 6
        }
    end,

    castOnChar = function(self, spell, caster, target)
        local skill = caster:skillLevel()
        local damage = 6 + 4 * skill

        if target:isUndead() then
            damage = damage * 2
            RPD.affectBuff(target, RPD.Buffs.Slow, 5)
        end

        target:getSprite():emitter():burst(RPD.Sfx.ShadowParticle.UP, 6)
        target:damage(damage, caster)
        RPD.playSound("snd_ghost")
        return true
    end
}
