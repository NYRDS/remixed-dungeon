---
--- ScrollOfMagicMapping: maps the whole level and reveals secrets
--- (was items/scrolls/ScrollOfMagicMapping.java - the level-array sweep is
--- the java helper).
---
local scroll = require "scripts/lib/scroll"

return scroll.init{
    desc = function()
        return { price = 25 }
    end,
    doRead = function(self, item, reader)
        item:magicMappingRead(reader)
    end,
}
