--
-- Last rites: a funeral service that burns and cripples the undead in a wide circle
--

local RPD = require "scripts/lib/commonClasses"

local spell = require "scripts/lib/spell"

return spell.init{
    desc = function()
        return {
            image         = 3,
            imageFile     = "spellsIcons/priest.png",
            name          = "LastRites_Name",
            info          = "LastRites_Info",
            magicAffinity = "Priest",
            targetingType = "cell",
            level         = 4,
            castTime      = 2,
            spellCost     = 10,
            cooldown      = 12
        }
    end,

    castOnCell = function(self, spell, caster, cell)
        local level = RPD.Dungeon.level
        if not level:cellValid(cell) then
            return false
        end

        local skill = caster:skillLevel()
        local damage = 5 + 5 * skill

        local mobs = level:getCopyOfMobsArray()
        for i = 1, #mobs do
            local mob = mobs[i]
            if mob:isUndead() and not caster:friendly(mob) and level:distance(mob:getPos(), cell) <= 2 then
                mob:damage(damage, caster)
                RPD.affectBuff(mob, RPD.Buffs.Cripple, 5)
                mob:getSprite():emitter():burst(RPD.Sfx.ShaftParticle.FACTORY, 4)
            end
        end

        RPD.Sfx.CellEmitter:center(cell):burst(RPD.Sfx.Speck:factory(RPD.Sfx.Speck.LIGHT), 10)
        RPD.playSound("snd_bones")
        return true
    end
}
