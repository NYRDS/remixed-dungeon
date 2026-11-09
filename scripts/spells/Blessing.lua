--
-- Orthodox blessing: grace for any soul - but the unholy cannot receive it,
-- undead and demons are instead weighed down by the holy word
--

local RPD = require "scripts/lib/commonClasses"

local spell = require "scripts/lib/spell"

return spell.init{
    desc = function()
        return {
            image         = 0,
            imageFile     = "spellsIcons/priest.png",
            name          = "BlessingSpell_Name",
            info          = "BlessingSpell_Info",
            magicAffinity = "Priest",
            targetingType = "char",
            level         = 1,
            castTime      = 0.5,
            spellCost     = 4,
            cooldown      = 15
        }
    end,

    castOnChar = function(self, spell, caster, target)
        local skill = caster:skillLevel()

        if target:isUndead() or target:isDemon() then
            -- the unholy cannot receive grace
            RPD.affectBuff(target, RPD.Buffs.Slow, 3)
            target:getSprite():emitter():burst(RPD.Sfx.ShaftParticle.FACTORY, 6)
            RPD.playSound("snd_ray")
            return true
        end

        local buff = RPD.affectBuff(target, RPD.Buffs.Blessed, 20 + 10 * skill)
        buff:level(1 + math.floor(skill / 3))

        target:getSprite():emitter():burst(RPD.Sfx.ShaftParticle.FACTORY, 6)
        RPD.playSound("snd_ray")
        RPD.glogp("BlessingSpell_Cast", target:getName())
        return true
    end
}
