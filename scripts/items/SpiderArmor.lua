---
--- SpiderArmor: data armor kind (was armor/SpiderArmor.java / common/armor).
---
local RPD    = require "scripts/lib/commonClasses"
local weapon = require "scripts/lib/weapon"

return weapon.init{
    desc = function()
        return {
            tier      = 3,
            image     = 21,
            hasHelmet = true,
        }
    end,
    defenceProc = function(self, item, attacker, defender, damage)
        -- 50%: seed a web blob under the attacker (was SpiderArmor.defenceProc)
        if RPD.Random:Int(2) == 0 then
            RPD.placeBlob(RPD.Blobs.Web, attacker:getPos(), 1)
        end
        return damage
    end,
}
