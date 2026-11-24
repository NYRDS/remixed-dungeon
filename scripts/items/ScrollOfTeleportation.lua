---
--- ScrollOfTeleportation: random teleport (was items/scrolls/ScrollOfTeleportation.java).
---
local RPD    = require "scripts/lib/commonClasses"
local scroll = require "scripts/lib/scroll"

return scroll.init{
    desc = function()
        return { price = 40 }
    end,
    doRead = function(self, item, reader)
        RPD.playSound("snd_read")
        RPD.CharUtils:dispelInvisibility(reader)
        RPD.CharUtils:teleportRandom(reader)
        scroll.identifyTail(self, item, reader)
    end,
}
