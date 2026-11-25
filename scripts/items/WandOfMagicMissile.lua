---
--- WandOfMagicMissile: plain force damage; always known, 3 starting charges,
--- can disenchant another wand for an upgrade (was wands/WandOfMagicMissile.java).
---
local RPD  = require "scripts/lib/commonClasses"
local wand = require "scripts/lib/wand"

return wand.init{
    desc = function()
        return {
            image          = 7,  -- ItemSpriteSheet.WAND_MAGIC_MISSILE
            identified     = true,
            initialCharges = 3,
            disenchants    = true,
        }
    end,
    zap = function(self, item, cell, victim)
        if victim ~= nil then
            local level = item:effectiveLevel()
            wand.damageBurst(self, item, victim, RPD.Random:Int(1, 6 + level * 2), 0xFF99CCFF, level / 2 + 2)
        end
    end,
}
