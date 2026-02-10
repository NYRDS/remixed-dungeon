local RPD = require "scripts/lib/commonClasses"

local spell = require "scripts/lib/spell"

return spell.init{
    desc  = function ()
        return {
            image         = 2,
            imageFile     = "spellsicons/plaguedoctor.png",
            name          = "BloodTransfusion_Name",
            info          = "BloodTransfusion_Info",
            magicAffinity = "PlagueDoctor",
            targetingType = "char_not_self",
            level         = 2,
            castTime      = 1,
            spellCost     = 8,
            cooldown      = 10
        }
    end,

    castOnChar = function(self, spell, caster, target)
        if target then
            -- Calculate amount to drain from target and heal caster
            local drainAmount = math.max(1, target:ht() * 0.15 + caster:skillLevel()) -- Drain ~15% of target's max HP plus skill bonus
            local healAmount = drainAmount * 0.8 -- Caster gets slightly less than what was drained (some lost in transfer)
            
            -- Check if target has enough HP to drain
            if target:hp() <= drainAmount then
                RPD.glogn("BloodTransfusion_TargetLowHp")
                return false
            end
            
            -- Apply the drain to target and heal to caster
            target:damage(drainAmount, caster) -- Damage the target (draining blood)
            caster:heal(healAmount, caster) -- Heal the caster (receiving blood)
            
            -- Visual effects
            target:getSprite():emitter():start(RPD.Sfx.ElmoParticle.FACTORY, 0.05, 10)
            caster:getSprite():emitter():start(RPD.Sfx.ElmoParticle.FACTORY, 0.05, 10)
            
            -- Connect the two characters with particles
            RPD.Sfx.MagicMissile:bleeding(caster:getSprite().parent, target:getPos(), caster:getPos(), nil)
            
            -- Sound effect
            RPD.playSound("snd_heal.mp3")
            
            return true
        end
        return false
    end
}