---
--- Shared wrapper for data blobs: ScriptBlob.java (the kind-parameterized
--- java framework) reads the desc table once per kind and dispatches the
--- optional hooks. The O(area) executor walk stays java; the scripts get one
--- batched affect(self, blob, cells) call per tick with the covered cells.
--- self = the per-kind script table, blob = the ScriptBlob instance.
---
--- desc(self) -> { mode, cancelsWith, seedMode, tileDesc }
---   mode: "diffusion" (gases, regrowth) | "decay" (web) | "persist"
---         (darkness, foliage, alchemy) | "fire" | "liquidFlame" | "well"
---         (the magic wells ride scripts/lib/wellbase.lua instead)
--- affect(self, blob, cells)  -> per-covered-cell mechanics (cells = java
---                               int[], iterate 1-based via cells.length)
--- after(self, blob, cells)   -> once per tick, covered cells passed
--- onHeroDeath(self, blob)    -> Doom death report
---

local blob = {}

blob.__index = blob

blob.desc = function(self)
    return {}
end

blob.init = function(desc)
    setmetatable(desc, blob)
    return desc
end

return blob
