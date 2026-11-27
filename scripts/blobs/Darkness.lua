---
--- Darkness: a persistent visual dim - no per-cell mechanics (was
--- Darkness.java).
---
local blob = require "scripts/lib/blobbase"

return blob.init{
    desc = function()
        return {
            mode     = "persist",
            tileDesc = "Darkness_Info",
        }
    end,
}
