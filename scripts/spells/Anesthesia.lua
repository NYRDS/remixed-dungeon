local RPD = require "scripts/lib/commonClasses"

local spell = require "scripts/lib/spell"

return spell.init{
    desc  = function ()
        return {
            image         = 2,
            imageFile     = "spellsIcons/doctor.png",
            name          = "Anesthesia_Name",
            info          = "Anesthesia_Info",
            magicAffinity = "PlagueDoctor",
            targetingType = "char_not_self",
            level         = 1,
            castTime      = 1,
            spellCost     = 6,
            cooldown      = 10
        }
    end,

    castOnChar = function(self, spell, caster, target)
        if target then
            local skill = caster:skillLevel()
            RPD.affectBuff(target, "Sleep", 10 * skill) -- 10 turns of sleep

            -- Apply anesthesia so target won't wake up from damage
            RPD.affectBuff(target, "Anesthesia", 10 * skill) -- 10 turns of anesthesia

            -- Visual effect
            target:getSprite():emitter():burst(RPD.Sfx.ElmoParticle.FACTORY, 6)

            -- Sound effect
            RPD.playSound("snd_meld")

            return true
        end
        return false
    end
}