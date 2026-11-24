---
--- ScrollOfMirrorImage: three clone images appear beside the reader
--- (was items/scrolls/ScrollOfMirrorImage.java).
---
local RPD    = require "scripts/lib/commonClasses"
local scroll = require "scripts/lib/scroll"

local NIMAGES = 3

return scroll.init{
    desc = function()
        return {}
    end,
    doRead = function(self, item, reader)
        local level = RPD.Dungeon.level
        local nImages = NIMAGES
        while nImages > 0 do
            local cell = level:getEmptyCellNextTo(reader:getPos())
            if not level:cellValid(cell) then
                break
            end
            local image = reader:makeClone()
            RPD.WandOfBlink:appear(image, cell)
            nImages = nImages - 1
        end

        if nImages < NIMAGES then
            item:setKnown()
        end

        RPD.playSound("snd_read")
        RPD.CharUtils:dispelInvisibility(reader)

        reader:spend(1)
    end,
}
