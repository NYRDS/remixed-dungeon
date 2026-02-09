local RPD = require "scripts/lib/commonClasses"

local spell = require "scripts/lib/spell"

return spell.init{
    desc  = function ()
        return {
            image         = 2,
            imageFile     = "spellsicons/doctor.png",
            name          = "Anesthesia_Name",
            info          = "Anesthesia_Info",
            magicAffinity = "PlagueDoctor",
            targetingType = "char_not_self",
            level         = 3,
            castTime      = 2,
            spellCost     = 8,
            cooldown      = 15
        }
    end,

    castOnChar = function(self, spell, caster, target)
        if target and target ~= caster then
            RPD.affectBuff(target, "Sleep", 10) -- 10 turns of sleep

            -- Apply anesthesia so target won't wake up from damage
            RPD.affectBuff(target, "Anesthesia", 10) -- 10 turns of anesthesia

            -- Visual effect
            target:getSprite():emitter():burst(RPD.Sfx.ElmoParticle.FACTORY, 6)

            -- Sound effect
            RPD.playSound("snd_meld")

            return true
        end
        return false
    end
}