---
--- Shared wrapper for data scrolls: Scroll.java (the kind-parameterized java
--- framework) reads the desc table once per kind and dispatches doRead /
--- onItemSelected. self = the per-kind script table, item = the scroll.
--- Identification (random per-run rune image + known set) stays java-side.
---
--- desc(self)                          -> { price }  (price when known)
--- doRead(self, item, reader)          -> the read effect
--- onItemSelected(self, item, selector)-> inventory-scroll target applied
---
--- Identification tail shared by nearly every read effect:
---   scroll.identifyTail(self, item, reader)  -> setKnown + spend(1s)
---

local item = require "scripts/lib/item"

local scroll = {}

scroll.__index = scroll
setmetatable(scroll, item)

scroll.desc = function(self)
    return {}
end

scroll.identifyTail = function(self, scrollItem, reader)
    scrollItem:setKnown()
    reader:spend(1) -- Scroll.TIME_TO_READ
end

scroll.init = function(desc)
    setmetatable(desc, scroll)
    return desc
end

return scroll
