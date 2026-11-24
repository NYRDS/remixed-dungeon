---
--- ScrollOfDomination: turns one visible, pettable mob into a pet
--- (was items/scrolls/ScrollOfDomination.java).
---
local RPD    = require "scripts/lib/commonClasses"
local scroll = require "scripts/lib/scroll"

return scroll.init{
    desc = function()
        return { price = 80 }
    end,
    doRead = function(self, item, reader)
        RPD.Sfx.SpellSprite:show(reader, RPD.Sfx.SpellSprite.DOMINATION)
        RPD.playSound("snd_dominance")
        RPD.CharUtils:dispelInvisibility(reader)

        local candidates = {}
        local level = RPD.Dungeon.level
        for _, mob in pairs(level:getMobs()) do
            if level.fieldOfView[mob:getPos() + 1]
                    and not mob:isBoss() and not mob:isPet() and not mob:isNpc() then
                candidates[#candidates + 1] = mob
            end
        end

        while #candidates > 0 do
            local idx = RPD.Random:Int(#candidates)
            local pet = candidates[idx + 1]
            if pet:canBePet() then
                RPD.Mob:makePet(pet, reader:getId())
                RPD.new(RPD.Sfx.Flare, 3, 32):show(pet:getSprite(), 2)
                break
            end
            table.remove(candidates, idx + 1)
        end

        reader:observe()

        scroll.identifyTail(self, item, reader)
    end,
}
