---
--- ScrollOfPsionicBlast: blinds and damages every visible mob, and the reader
--- (was items/scrolls/ScrollOfPsionicBlast.java).
---
local RPD    = require "scripts/lib/commonClasses"
local scroll = require "scripts/lib/scroll"

return scroll.init{
    desc = function()
        return { price = 80 }
    end,
    doRead = function(self, item, reader)
        RPD.GameScene:flash(0xFFFFFF)

        RPD.playSound("snd_blast")
        RPD.CharUtils:dispelInvisibility(reader)

        local level = RPD.Dungeon.level
        for _, mob in pairs(level:getMobs()) do
            if level.fieldOfView[mob:getPos() + 1] then
                RPD.affectBuff(mob, "Blindness", RPD.Random:Int(3, 6))
                mob:damage(RPD.Random:IntRange(1, math.floor(mob:ht() * 2 / 3)), item)
            end
        end

        RPD.affectBuff(reader, "Blindness", RPD.Random:Int(3, 6))
        reader:observe()

        scroll.identifyTail(self, item, reader)
    end,
}
