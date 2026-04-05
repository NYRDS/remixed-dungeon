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
        if caster:hp() >= caster:ht() then
            RPD.glog('BloodTransfusion_FullHP')
            return false
        end

        if target and target:getOwnerId() == caster:getId() then
            -- Calculate exact heal needed to fully restore the doctor
            local missingHp = caster:ht() - caster:hp()
            -- Cap drain at target's current HP (can't drain more than they have)
            local drainAmount = math.min(missingHp, target:hp())
            -- Transfer rate scales with doctor skill level (55% at lvl 1, 100% at lvl 10)
            local transferRate = math.min(1.0, 0.5 + caster:skillLevel() * 0.05)
            local healAmount = drainAmount * transferRate

            -- Apply the drain to target and heal to caster
            target:damage(drainAmount, caster)
            caster:heal(healAmount, caster)

            -- Visual effects - more dramatic blood effect
            target:getSprite():emitter():start(RPD.Sfx.BloodParticle.FACTORY, 0.02, 30)
            caster:getSprite():emitter():start(RPD.Sfx.ElmoParticle.FACTORY, 0.05, 20)

            -- Sound effect
            RPD.playSound("snd_heal.mp3")

            RPD.glog('BloodTransfusion_Drained')
            return true

        else
            RPD.glog('BloodTransfusion_WontAgreed')
        end

        return false
        end
}