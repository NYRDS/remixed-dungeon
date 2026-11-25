---
--- Wind gust: pushes the first char on the ballistica ray.
--- Ported from java WindGust (data-spells migration).
---

local RPD = require "scripts/lib/commonClasses"

local Ballistica = RPD.Ballistica

local spell = require "scripts/lib/spell"

return spell.init{
    desc  = function ()
        return {
            image         = 0,
            imageFile     = "spellsIcons/elemental.png",
            name          = "WindGust_Name",
            info          = "WindGust_Info",
            magicAffinity = "Elemental",
            targetingType = "cell",
            level         = 1,
            castTime      = 1,
            spellCost     = 1,
            cooldown      = 1
        }
    end,

    castOnCell = function(self, spell, chr, cell)
        local level = chr:level()

        if not level:cellValid(cell) then
            return false
        end

        local skill = chr:skillLevel()

        Ballistica:cast(chr:getPos(), cell, true, false)

        -- java trace is 0-based: java trace[i] == trace[i+1] here.
        -- the gust stops at the first char on the ray
        local bound = math.min(skill + 2, Ballistica.distance - 1)
        for i = 1, bound - 1 do
            local ch = RPD.Actor:findChar(Ballistica.trace[i + 1])
            if ch then
                ch:getSprite():emitter():burst(RPD.Sfx.WindParticle.FACTORY, 5 + skill * 2)
                ch:getSprite():burst(0xFF99FFFF, 3 + skill)
                RPD.playSound("snd_meld")

                if ch:push(chr) then
                    ch:spend(1)
                else
                    ch:damage(skill * 2, spell)
                end
                return true
            end
        end

        return false
    end
}
