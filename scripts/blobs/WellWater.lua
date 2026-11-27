---
--- The plain well water: no hero effect, nothing is transmuted - any heap
--- dropped in is swallowed, the water stays. Vanilla never seeds this kind;
--- it exists for save/mod compat (the legacy WellWater class).
---

local well = require "scripts/lib/wellbase"

return well.init{
    desc = function()
        return {
            mode = "well",
        }
    end,
}
