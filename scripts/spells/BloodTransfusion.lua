local RPD = require "scripts/lib/commonClasses"

local spell = require "scripts/lib/spell"

return spell.init{
    desc  = function ()
        return {
            image         = 0,
            imageFile     = "spellsIcons/doctor.png",
            name          = "BloodTransfusion_Name",
            info          = "BloodTransfusion_Info",
            magicAffinity = "PlagueDoctor",
            targetingType = "char_not_self",
            level         = 2,
            castTime      = 1,
            spellCost     = 7,
            cooldown      = 8
        }
    end,

    castOnChar = function(self, spell, caster, target)
        if target and target:getOwnerId() == caster:getId() then
            -- Drain ALL blood from the target, killing them
            local drainAmount = target:hp() -- Drain all HP, killing the target
            local healAmount = drainAmount * 0.95 -- Nearly perfect transfer rate

            -- Apply the drain to target and heal to caster
            target:damage(drainAmount, caster) -- Damage the target (draining blood)
            caster:heal(healAmount, caster) -- Heal the caster (receiving blood)

            -- Visual effects - more dramatic blood effect
            target:getSprite():emitter():start(RPD.Sfx.BloodParticle.FACTORY, 0.02, 30)
            caster:getSprite():emitter():start(RPD.Sfx.ElmoParticle.FACTORY, 0.05, 20)

            -- Connect the two characters with blood particles
            RPD.Sfx.MagicMissile:bleeding(caster:getSprite().parent, target:getPos(), caster:getPos(), nil)

            -- Sound effect
            RPD.playSound("snd_heal.mp3")

            return true

        else
            RPD.glog('BloodTransfusion_WontAgreed')
        end

        return false
        end
}