---
--- Anathema: excommunication - the marked target takes half again as much damage from all sources
---
local buff = require "scripts/lib/buff"

return buff.init{
    desc  = function ()
        return {
            icon          = 27, -- BuffIndicator.MARK (crosshair tile, shared with SnipersMark)
            name          = "Anathema_Name",
            info          = "Anathema_Info",
        }
    end,

    damage = function(self, buff, dmg, src)
        -- never return nil here: nil coerces to 0 damage (total immunity)
        return dmg + dmg / 2
    end
}
