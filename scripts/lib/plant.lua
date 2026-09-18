---
--- This file is part of Remixed Pixel Dungeon.
---
--- Shared wrapper for data plants (batch 20): Plant.java dispatches
--- effect/name/info to scripts/plants/<Kind>.lua modules built with
--- plant.init{}. self = the per-instance script table (per-plant lua
--- state rides self.data, serpent-persisted as LUA_DATA).
---

local serpent = require "scripts/lib/serpent"
local RPD = require "scripts/lib/commonClasses"

local plant = {}

plant.__index = plant

-- pos: plant cell, presser: what pressed the plant (Char, seed item, heap...),
-- activator: the char that caused the presser (nil when presser is not a char)
function plant.effect(self, plantObject, pos, presser, activator)
end

-- no default name/info hooks here: a nil-returning hook would shadow the
-- java fallback (<Kind>_Name / <Kind>_Desc string ids). Only override
-- name/info in the plant module when it must NOT use the defaults.

function plant.saveData(self)
    return serpent.dump(self.data or {})
end

function plant.loadData(self, _, str)
    local _,data = serpent.load(str)
    self.data = data or {}
end

function plant.storeData(self, data)
    self.data = data or {}
end

function plant.restoreData(self)
    return self.data or {}
end

plant.init = function(desc)
    setmetatable(desc, plant)
    return desc
end

return plant
