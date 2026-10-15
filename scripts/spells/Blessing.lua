--
-- Orthodox blessing: grants the Blessed buff to the caster
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
            targetingType = "self",
            level         = 1,
            castTime      = 0.5,
            spellCost     = 4,
            cooldown      = 15
        }
    end,

    cast = function(self, spell, caster)
        local skill = caster:skillLevel()
        local buff = RPD.affectBuff(caster, RPD.Buffs.Blessed, 20 + 10 * skill)
        buff:level(1 + math.floor(skill / 3))

        caster:getSprite():emitter():burst(RPD.Sfx.ShaftParticle.FACTORY, 6)
        RPD.playSound("snd_ray")
        RPD.glogp("BlessingSpell_Cast", caster:getName())
        return true
    end
}
