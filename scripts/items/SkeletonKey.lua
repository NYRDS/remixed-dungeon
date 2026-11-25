---
--- SkeletonKey: data key kind (was keys/SkeletonKey.java). The levelId binding, depth status
--- and the iron-key HUD counter stay in the Key framework.
---
local weapon = require "scripts/lib/weapon"

return weapon.init{
    desc = function()
        return {
            image     = 12,
        }
    end,
}
