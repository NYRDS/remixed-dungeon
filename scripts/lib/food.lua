---
--- Shared wrapper for data foods: Food.java (the kind-parameterized java
--- framework) reads the desc table once per kind and dispatches the optional
--- hooks. self = the per-kind script table; hooks are stateless (foods are
--- stackable, so no per-instance lua state).
---
--- desc(self)                      -> { image, imageFile, energy, message,
---                                     price }; missing fields keep the java
---                                     defaults (Food_Message, price 0 =
---                                     not sellable)
--- eat(self, item, chr)            -> replaces the java eat entirely
---                                    (mimic traps); no onExecute tail runs
--- onExecute(self,item,chr,action) -> post-action effects, same shape as the
---                                    old _execute overrides
--- pick(self, item, ch, pos)       -> replaces Item.pick
--- burn/freeze/poison(self,item,cell) -> kind string to morph into,
---                                      nil = unchanged
--- purify(self, item)              -> kind string of the fresh food,
---                                    nil = itself
--- molder(self, item, cell)        -> rotten drop burst, returns "gas
---                                    spawned?"; presence marks the kind
---                                    rotten for the java UX
---
--- Constants referenced by the migrated java classes, kept as explicit data:
--- Hunger.HUNGRY = 280, Hunger.STARVING = 400, ItemSpriteSheet food indices.
---

local item = require "scripts/lib/item"

local food = {}

food.__index = food
setmetatable(food, item)

food.desc = function(self)
    return {}
end

food.init = function(desc)
    setmetatable(desc, food)
    return desc
end

return food
