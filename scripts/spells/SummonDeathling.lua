---
--- Summon Deathling: one undead pet, its stats grow with hero level
--- (Deathling.lua handles the growth). Summon limit scales with the
--- caster's magic skill level.
--- Ported from java SummonDeathling/SummoningSpell (data-spells migration).
---

local RPD = require "scripts/lib/commonClasses"

local spell = require "scripts/lib/spell"

local mobKind = "Deathling"

return spell.init{
    desc  = function ()
        return {
            image         = 0,
            imageFile     = "spellsIcons/necromancy.png",
            name          = "SummonDeathling_Name",
            info          = "SummonDeathling_Info",
            magicAffinity = "Necromancy",
            targetingType = "none",
            level         = 1,
            castTime      = 3,
            spellCost     = 5,
            cooldown      = 1
        }
    end,

    -- script veto: false denies the cast, anything else falls through to the
    -- java base checks (cooldown, SP) — java SummoningSpell.canCast parity
    canCast = function(self, spell, chr, reallyCast)
        local limit = 1 + spell:getLevelModifier(chr)

        local count = 0
        local pets = chr:getPets_l()
        for _, pet in pairs(pets) do
            if pet:valid() and pet:isAlive() and pet:getEntityKind() == mobKind then
                count = count + 1
            end
        end

        if count >= limit then
            if reallyCast then
                RPD.glogw(RPD.JavaUtils:format(RPD.textById("Spells_SummonLimitReached"), { spell:name(), limit }))
            end
            return false
        end

        return true
    end,

    -- dynamic desc: the Info string carries the level-scaled summon limit
    descText = function(self, spell)
        local hero = RPD.Dungeon.hero
        if not hero then
            return RPD.textById("SummonDeathling_Info")
        end
        local limit = 1 + spell:getLevelModifier(hero)
        return RPD.JavaUtils:format(RPD.textById("SummonDeathling_Info"), { limit })
    end,

    cast = function(self, spell, caster)
        local level    = caster:level()
        local casterPos = caster:getPos()
        local spawnPos  = level:getEmptyCellNextTo(casterPos)

        RPD.Sfx.Wound:hit(caster)
        caster:detachBuff("Health")

        if level:cellValid(spawnPos) then
            local pet = RPD.MobFactory:mobByName(mobKind)
            pet = pet:makePet(caster)
            pet:setPos(spawnPos)
            level:spawnMob(pet, 0, casterPos)
        end

        return true
    end
}
