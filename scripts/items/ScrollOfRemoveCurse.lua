---
--- ScrollOfRemoveCurse: uncurses everything the reader carries
--- (was items/scrolls/ScrollOfRemoveCurse.java).
---
local RPD    = require "scripts/lib/commonClasses"
local scroll = require "scripts/lib/scroll"

return scroll.init{
    desc = function()
        return { price = 30 }
    end,
    doRead = function(self, item, reader)
        item:showFlare(reader, 6, 32, 0, 2)
        RPD.playSound("snd_read")
        RPD.CharUtils:dispelInvisibility(reader)
        local procced = RPD.Scroll:uncurseAll(reader)
        RPD.removeBuff(reader, "Weakness")
        if procced then
            RPD.glogp(RPD.textById("ScrollOfRemoveCurse_Proced"))
            reader:getSprite():emitter():start(RPD.Sfx.ShadowParticle.UP, 0.05, 10)
        else
            RPD.glog(RPD.textById("ScrollOfRemoveCurse_NoProced"))
        end
        scroll.identifyTail(self, item, reader)
    end,
}
