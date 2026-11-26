---
--- Shared wrapper for data seeds: Seed.java (the kind-parameterized java
--- framework) reads the desc table once per kind and dispatches the optional
--- hooks. self = the per-instance script table; hooks are stateless (seeds
--- are stackable, so no per-instance lua state).
---
--- desc(self)                  -> { plantName, descText, image, alchemyKind }
---                               maybeId'd string ids + sprite index in
---                               items/seeds.png; missing fields keep java
---                               defaults
--- onEat(self, item, chr)      -> effects after the eat plumbing (detach,
---                               time, sprite, sound), same shape as the old
---                               _execute AC_EAT overrides
--- onCollect(self, item, bag)  -> post-collect notification (Rotberry
---                               challenge), runs only when collect succeeded
--- inflammable(self,item,cell) -> true keeps the seed out of the fire
---                               (Rotberry), absent burns it to nothing
---
--- Constants referenced by the migrated java classes, kept as explicit data:
--- buff durations in turns, CharUtils duration factors.
---

local item = require "scripts/lib/item"

local seed = {}

seed.__index = seed
setmetatable(seed, item)

seed.desc = function(self)
    return {}
end

seed.init = function(desc)
    setmetatable(desc, seed)
    return desc
end

return seed
