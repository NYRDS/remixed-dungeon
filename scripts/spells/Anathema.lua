--
-- Anathema: excommunicates a target - it takes half again as much damage from every source
--

local RPD = require "scripts/lib/commonClasses"

local spell = require "scripts/lib/spell"

return spell.init{
    desc = function()
        return {
            image         = 5,
            imageFile     = "spellsIcons/priest.png",
            name          = "Anathema_Name",
            info          = "Anathema_Info",
            magicAffinity = "Priest",
            targetingType = "char_not_self",
            level         = 3,
            castTime      = 1,
            spellCost     = 7,
            cooldown      = 20
        }
    end,

    castOnChar = function(self, spell, caster, target)
        if caster:friendly(target) then
            return false
        end

        local skill = caster:skillLevel()
        local duration = 10 + 5 * skill
        if target:isUndead() or target:isDemon() then
            duration = 15 + 5 * skill
        end

        RPD.affectBuff(target, "Anathema", duration)
        target:getSprite():emitter():burst(RPD.Sfx.ShadowParticle.UP, 6)
        RPD.playSound("snd_cursed")
        return true
    end
}
