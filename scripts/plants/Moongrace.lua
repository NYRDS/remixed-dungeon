---
--- Moongrace: moonlight - Moongrace buff on the pressing char, undead pets
--- are healed back to life, living mobs spawn a copy
--- (was plants/Moongrace.java)
---
local RPD = require "scripts/lib/commonClasses"
local plant = require "scripts/lib/plant"

return plant.init{
    effect = function(self, plantObject, pos, presser, activator)
        if presser ~= nil and RPD.CharUtils:isChar(presser) then
            RPD.affectBuff(presser, "Moongrace")
        end

        -- Mob check: everything Char that is not the hero
        if presser ~= nil and RPD.CharUtils:isChar(presser) and presser:getEntityKind() ~= "Hero" then
            -- necromanced mobs don't multiply - moonlight returns them to life instead;
            -- natural undead (skeleton and such) just are that way and clone like the living
            if presser.undead and not presser.naturalUndead then
                presser:heal(presser:ht(), presser)
                presser:setUndead(false)
            else
                local cell = RPD.Dungeon.level:getEmptyCellNextTo(pos)
                if RPD.Dungeon.level:cellValid(cell) then
                    -- stepping on the plant (own activation) clones the pet as usual;
                    -- moonlight forced by a hostile activation leaves a feral copy
                    if activator ~= nil and not presser:friendly(activator) then
                        presser:splitHostile(cell, 0)
                    else
                        presser:split(cell, 0)
                    end
                    if RPD.Dungeon:isCellVisible(cell) then
                        RPD.Sfx.CellEmitter:get(cell):start(RPD.Sfx.ShaftParticle.FACTORY, 0.2, 6)
                    end
                end
            end
        end

        if RPD.Dungeon:isCellVisible(pos) then
            RPD.Sfx.CellEmitter:get(pos):start(RPD.Sfx.ShaftParticle.FACTORY, 0.2, 3)
        end
    end
}
