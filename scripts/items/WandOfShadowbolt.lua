---
--- WandOfShadowbolt: shadow damage, purple idle particles; always known,
--- outside the wood handler (was common/WandOfShadowbolt.java).
---
local RPD  = require "scripts/lib/commonClasses"
local wand = require "scripts/lib/wand"

return wand.init{
    desc = function()
        return {
            image      = 13,
            identified = true,
        }
    end,
    zap = function(self, item, cell, victim)
        if victim ~= nil then
            local level = item:effectiveLevel()
            victim:damage(RPD.Random:Int(4 + level * 2, 3 + level * 3), item)
            victim:getSprite():burst(0x551A8B, level / 2 + 3)
        end
    end,
    emitter = function(self, item)
        return "PurpleParticle"
    end,
    emitterInterval = function(self, item)
        return 0.1
    end,
}
