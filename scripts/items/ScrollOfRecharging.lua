---
--- ScrollOfRecharging: recharges the reader's wands
--- (was items/scrolls/ScrollOfRecharging.java).
---
local RPD    = require "scripts/lib/commonClasses"
local scroll = require "scripts/lib/scroll"

return scroll.init{
    desc = function()
        return { price = 40 }
    end,
    doRead = function(self, item, reader)
        local count = reader:getBelongings():charge(true)
        RPD.Scroll:chargeFx(reader)

        RPD.playSound("snd_read")
        RPD.CharUtils:dispelInvisibility(reader)

        if count > 0 then
            local txt = count > 1 and RPD.textById("ScrollOfRecharging_Info1b")
                                   or RPD.textById("ScrollOfRecharging_Info1a")
            RPD.glog(txt)
            RPD.Sfx.SpellSprite:show(reader, RPD.Sfx.SpellSprite.CHARGE)
        else
            RPD.glog(RPD.textById("ScrollOfRecharging_Info2"))
        end
        scroll.identifyTail(self, item, reader)
    end,
}
