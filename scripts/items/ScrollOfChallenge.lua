---
--- ScrollOfChallenge: wakes and challenges every mob on the level
--- (was items/scrolls/ScrollOfChallenge.java).
---
local RPD    = require "scripts/lib/commonClasses"
local scroll = require "scripts/lib/scroll"

return scroll.init{
    desc = function()
        return {}
    end,
    doRead = function(self, item, reader)
        RPD.CharUtils:challengeAllMobs(reader, "snd_challenge")
        RPD.glogw(RPD.textById("ScrollOfChallenge_Info1"))
        scroll.identifyTail(self, item, reader)
    end,
}
