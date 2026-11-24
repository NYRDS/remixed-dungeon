---
--- ScrollOfSummoning: a random (pettable) mob appears beside the reader
--- (was items/scrolls/ScrollOfSummoning.java - the summon sweep is the java
--- helper).
---
local scroll = require "scripts/lib/scroll"

return scroll.init{
    desc = function()
        return {}
    end,
    doRead = function(self, item, reader)
        item:summoningRead(reader)
    end,
}
