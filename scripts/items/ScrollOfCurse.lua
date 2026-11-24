---
--- ScrollOfCurse: curses the reader's gear and afflicts them
--- (was items/scrolls/ScrollOfCurse.java).
---
local RPD    = require "scripts/lib/commonClasses"
local scroll = require "scripts/lib/scroll"

local badBuffs = { "Blindness", "Charm", "Roots", "Slow", "Vertigo", "Weakness" }

return scroll.init{
    desc = function()
        return { price = 300 }
    end,
    doRead = function(self, item, reader)
        RPD.CharUtils:dispelInvisibility(reader)
        reader:getSprite():emitter():burst(RPD.Sfx.ShadowParticle.CURSE, 6)
        RPD.playSound("snd_cursed")
        RPD.affectBuff(reader, badBuffs[RPD.Random:Int(#badBuffs) + 1], 10)
        reader:getBelongings():curseEquipped()
        scroll.identifyTail(self, item, reader)
    end,
}
