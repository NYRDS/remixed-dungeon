---
--- GoldenKey: data key kind (was keys/GoldenKey.java). The levelId binding, depth status
--- and the iron-key HUD counter stay in the Key framework.
---
local weapon = require "scripts/lib/weapon"

return weapon.init{
    desc = function()
        return {
            image     = 11,
        }
    end,
}
